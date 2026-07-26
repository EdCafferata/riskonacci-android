package info.cafferata.riskonacci.networking.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.networking.SessionMessage
import info.cafferata.riskonacci.networking.SessionTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Multiplayer transport backed by Firebase — the same Firestore schema
 * as the iOS app's `FirebaseSessionTransport.swift`, so a room works
 * across platforms. "Nearby" vs "Online" is purely a UI label here, not
 * a different transport: one shared backend works for local and remote
 * play alike.
 *
 * Real-time Firestore listeners push state changes to every device
 * immediately. A lightweight heartbeat still runs periodically, but only
 * to broadcast "I'm still here" (`lastSeen`/`hostHeartbeatAt`), not to
 * fetch anything.
 *
 * Host election is the lexicographically smallest participant ID among
 * the currently-active roster, computed independently on each device so
 * it can decide for itself when to (re)claim write ownership of the room
 * document after the original host disappears. Firestore's security
 * rules (see the repo README) let *any* current participant write the
 * room document, so host migration works without needing a
 * creator-only-write workaround.
 */
class FirebaseSessionTransport : SessionTransport {
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 5_000L
        private const val STALE_THRESHOLD_MS = 15_000L
    }

    override var localParticipantId: String = ""
        private set
    private var localNickname = ""
    private var roomId = ""

    private val db = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var roomListener: ListenerRegistration? = null
    private var participantsListener: ListenerRegistration? = null
    private var votesListener: ListenerRegistration? = null
    private var heartbeatJob: Job? = null

    private var hasReceivedInitialRoster = false
    private val activeParticipants = mutableMapOf<String, Date>()
    private var knownDeckRaw: String? = null
    private var knownTwoRoundsEnabled: Boolean? = null
    private var knownEpoch: Int? = null
    private var knownIsRevealed: Boolean? = null
    private val lastSeenVote = mutableMapOf<String, String>()

    override var onReceive: ((SessionMessage, String) -> Unit)? = null
    override var onPeerConnected: ((String, String) -> Unit)? = null
    override var onPeerDisconnected: ((String) -> Unit)? = null

    /** Gated on `hasReceivedInitialRoster` so a just-joined device doesn't
     * briefly see only itself and wrongly conclude it's the sole (and
     * therefore host) participant before the first roster snapshot
     * arrives. */
    private val computedLocalIsHost: Boolean
        get() = hasReceivedInitialRoster && activeParticipants.keys.minOrNull() == localParticipantId

    private val roomRef: DocumentReference get() = db.collection(FirebaseRoomDocument.COLLECTION).document(roomId)

    override fun startHosting(roomId: String, nickname: String) {
        this.roomId = roomId
        localNickname = nickname
        scope.launch {
            ensureSignedIn()
            activeParticipants[localParticipantId] = Date()
            runCatching {
                roomRef.set(FirebaseRoomDocument(hostId = localParticipantId, deckRaw = Deck.RISK.rawValue, twoRoundsEnabled = true)).await()
            }
            upsertOwnParticipantDocument()
            startListening()
            startHeartbeat()
        }
    }

    override fun join(roomId: String, nickname: String) {
        this.roomId = roomId
        localNickname = nickname
        scope.launch {
            ensureSignedIn()
            activeParticipants[localParticipantId] = Date()
            upsertOwnParticipantDocument()
            startListening()
            startHeartbeat()
        }
    }

    override fun send(message: SessionMessage) {
        scope.launch { handleSend(message) }
    }

    override fun stop() {
        roomListener?.remove()
        participantsListener?.remove()
        votesListener?.remove()
        heartbeatJob?.cancel()
        roomListener = null
        participantsListener = null
        votesListener = null
        heartbeatJob = null

        val capturedRoomRef = roomRef
        val participantId = localParticipantId
        scope.launch {
            runCatching { capturedRoomRef.collection(FirebaseParticipantDocument.COLLECTION).document(participantId).delete().await() }
            for (key in FirebaseVoteDocument.RoundKey.entries) {
                runCatching {
                    capturedRoomRef.collection(FirebaseVoteDocument.COLLECTION)
                        .document(FirebaseVoteDocument.documentId(participantId, key))
                        .delete()
                        .await()
                }
            }
        }
        activeParticipants.clear()
        hasReceivedInitialRoster = false
    }

    // MARK: Auth

    private suspend fun ensureSignedIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            localParticipantId = currentUser.uid
            return
        }
        val result = runCatching { FirebaseAuth.getInstance().signInAnonymously().await() }.getOrNull()
            ?: return // no network at all — transport degrades to a harmless no-op
        localParticipantId = result.user?.uid ?: ""
    }

    // MARK: Sending

    private suspend fun handleSend(message: SessionMessage) {
        when (message) {
            is SessionMessage.Hello, is SessionMessage.Roster -> Unit // identity/roster travel via Participant documents

            is SessionMessage.DeckChanged -> mutateRoom { it.copy(deckRaw = message.deck.rawValue) }

            is SessionMessage.SettingsChanged -> mutateRoom { it.copy(twoRoundsEnabled = message.twoRoundsEnabled) }

            is SessionMessage.Reset -> mutateRoom { it.copy(epoch = it.epoch + 1, isRevealed = false) }

            is SessionMessage.Reveal -> mutateRoom { it.copy(isRevealed = true) }

            is SessionMessage.Vote -> {
                if (message.participantId != localParticipantId) return
                val epoch = knownEpoch ?: return
                val roundKey = FirebaseVoteDocument.RoundKey.from(message.round)
                val doc = FirebaseVoteDocument(participantId = message.participantId, roundKey = roundKey.wireValue, cardLabel = message.cardLabel, epoch = epoch)
                runCatching { voteRef(message.participantId, roundKey).set(doc).await() }
            }

            is SessionMessage.ClearVote -> {
                if (message.participantId != localParticipantId) return
                for (key in FirebaseVoteDocument.RoundKey.entries) {
                    runCatching { voteRef(message.participantId, key).delete().await() }
                }
            }

            is SessionMessage.ClearRoundVote -> {
                if (message.participantId != localParticipantId) return
                runCatching { voteRef(message.participantId, FirebaseVoteDocument.RoundKey.from(message.round)).delete().await() }
            }
        }
    }

    /** Fetches the current room document, applies `change`, and writes it
     * back. Only meant to be called by whoever currently believes it's
     * host; a permission failure (not a current participant) or a
     * transient network error is silently dropped rather than fatal. */
    private suspend fun mutateRoom(change: (FirebaseRoomDocument) -> FirebaseRoomDocument) {
        if (!computedLocalIsHost) return
        val snapshot = runCatching { roomRef.get().await() }.getOrNull() ?: return
        val current = runCatching { snapshot.toObject(FirebaseRoomDocument::class.java) }.getOrNull() ?: return
        val updated = change(current).copy(hostId = localParticipantId, hostHeartbeatAt = Date())
        runCatching { roomRef.set(updated).await() }
    }

    private fun voteRef(participantId: String, roundKey: FirebaseVoteDocument.RoundKey): DocumentReference =
        roomRef.collection(FirebaseVoteDocument.COLLECTION).document(FirebaseVoteDocument.documentId(participantId, roundKey))

    // MARK: Real-time listeners

    private fun startListening() {
        roomListener = roomRef.addSnapshotListener { snapshot, _ ->
            val room = snapshot?.toObject(FirebaseRoomDocument::class.java) ?: return@addSnapshotListener
            handleRoomUpdate(room)
        }

        participantsListener = roomRef.collection(FirebaseParticipantDocument.COLLECTION)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val seen = mutableMapOf<String, Pair<Date, String>>()
                for (doc in snapshot.documents) {
                    val participant = doc.toObject(FirebaseParticipantDocument::class.java) ?: continue
                    seen[doc.id] = participant.lastSeen to participant.nickname
                }
                handleParticipantsUpdate(seen)
            }

        votesListener = roomRef.collection(FirebaseVoteDocument.COLLECTION)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val votes = snapshot.documents.mapNotNull { it.toObject(FirebaseVoteDocument::class.java) }
                handleVotesUpdate(votes)
            }
    }

    private fun handleRoomUpdate(room: FirebaseRoomDocument) {
        if (knownDeckRaw != room.deckRaw) {
            knownDeckRaw = room.deckRaw
            Deck.fromRawValue(room.deckRaw)?.let { onReceive?.invoke(SessionMessage.DeckChanged(it), room.hostId) }
        }
        if (knownTwoRoundsEnabled != room.twoRoundsEnabled) {
            knownTwoRoundsEnabled = room.twoRoundsEnabled
            onReceive?.invoke(SessionMessage.SettingsChanged(room.twoRoundsEnabled), room.hostId)
        }
        val previousEpoch = knownEpoch
        if (previousEpoch != null && previousEpoch != room.epoch) {
            onReceive?.invoke(SessionMessage.Reset, room.hostId)
        }
        knownEpoch = room.epoch
        if (knownIsRevealed != room.isRevealed) {
            knownIsRevealed = room.isRevealed
            if (room.isRevealed) {
                onReceive?.invoke(SessionMessage.Reveal, room.hostId)
            }
        }
    }

    private fun handleParticipantsUpdate(seen: Map<String, Pair<Date, String>>) {
        hasReceivedInitialRoster = true
        val now = Date()
        val fresh = seen.filterValues { now.time - it.first.time < STALE_THRESHOLD_MS }

        for ((id, value) in fresh) {
            if (!activeParticipants.containsKey(id)) {
                activeParticipants[id] = value.first
                if (id != localParticipantId) {
                    onPeerConnected?.invoke(id, value.second)
                }
            }
        }
        for (id in activeParticipants.keys.toList()) {
            if (!fresh.containsKey(id)) {
                activeParticipants.remove(id)
                if (id != localParticipantId) {
                    onPeerDisconnected?.invoke(id)
                }
            }
        }
        for ((id, value) in fresh) {
            activeParticipants[id] = value.first
        }
    }

    private fun handleVotesUpdate(votes: List<FirebaseVoteDocument>) {
        val epoch = knownEpoch ?: return
        for (vote in votes) {
            if (vote.epoch != epoch || vote.participantId == localParticipantId) continue
            // Epoch is part of the key so a vote for the same card cast
            // again after a reset isn't mistaken for a stale duplicate of
            // the previous round's vote and dropped.
            val key = "${vote.participantId}_${vote.roundKey}_${vote.epoch}"
            if (lastSeenVote[key] == vote.cardLabel) continue
            lastSeenVote[key] = vote.cardLabel
            val roundKey = FirebaseVoteDocument.RoundKey.from(vote.roundKey)
            onReceive?.invoke(SessionMessage.Vote(vote.participantId, roundKey.round, vote.cardLabel), vote.participantId)
        }
    }

    // MARK: Heartbeat

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                sendHeartbeat()
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private suspend fun sendHeartbeat() {
        upsertOwnParticipantDocument()
        if (computedLocalIsHost) {
            mutateRoom { it } // apply()-equivalent copy() always stamps hostId/hostHeartbeatAt
        }
    }

    private suspend fun upsertOwnParticipantDocument() {
        val doc = FirebaseParticipantDocument(nickname = localNickname)
        runCatching {
            roomRef.collection(FirebaseParticipantDocument.COLLECTION).document(localParticipantId).set(doc).await()
        }
    }
}
