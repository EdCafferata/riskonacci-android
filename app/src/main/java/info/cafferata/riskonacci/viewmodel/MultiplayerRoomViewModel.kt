package info.cafferata.riskonacci.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.model.PokerCard
import info.cafferata.riskonacci.model.RiskRound
import info.cafferata.riskonacci.networking.RoomId
import info.cafferata.riskonacci.networking.SessionMessage
import info.cafferata.riskonacci.networking.SessionParticipant
import info.cafferata.riskonacci.networking.SessionTransport
import info.cafferata.riskonacci.networking.firebase.FirebaseSessionTransport

/** Likelihood and Impact are tracked separately — a participant's Impact
 * pick must not overwrite their Likelihood pick, since the combined
 * reveal needs both at once. */
data class VoteState(
    var likelihoodLabel: String? = null,
    var impactLabel: String? = null,
    var singleLabel: String? = null,
) {
    val hasVoted: Boolean get() = singleLabel != null || likelihoodLabel != null || impactLabel != null
}

enum class ConnectionState { IDLE, CONNECTING, CONNECTED }

/**
 * Room state for a live multiplayer session, backed by Firebase — same
 * schema as the iOS app's `MultiplayerRoomViewModel.swift`, so a room
 * works across platforms. "Host" isn't a role assigned at connect time,
 * it's whoever currently has the lexicographically smallest participant
 * ID among everyone still present, recomputed independently by each
 * device from its own view of the participant list.
 */
class MultiplayerRoomViewModel : ViewModel() {
    var roomId by mutableStateOf("")
        private set
    val participants = mutableStateListOf<SessionParticipant>()
    var selectedDeck by mutableStateOf(Deck.RISK)
        private set
    var twoRoundsEnabled by mutableStateOf(true)
    val votes = mutableStateMapOf<String, VoteState>()
    var isRevealed by mutableStateOf(false)
        private set
    var connectionState by mutableStateOf(ConnectionState.IDLE)
        private set

    private var transport: SessionTransport? = null
    private var localNickname = ""

    val localParticipantId: String? get() = transport?.localParticipantId

    /** Lexicographically smallest participant ID currently connected —
     * the same rule evaluated independently on every device, so they
     * always agree without needing to coordinate. */
    val hostId: String? get() = participants.map { it.id }.minOrNull()

    val isHost: Boolean get() = hostId != null && hostId == localParticipantId

    val isTwoRoundFlow: Boolean get() = selectedDeck == Deck.RISK && twoRoundsEnabled

    fun hostRoom(nickname: String) {
        localNickname = nickname
        roomId = RoomId.generate()
        connectionState = ConnectionState.CONNECTING

        val newTransport = FirebaseSessionTransport()
        wire(newTransport)
        transport = newTransport
        newTransport.startHosting(roomId, nickname)

        participants.clear()
        participants.add(SessionParticipant(newTransport.localParticipantId, nickname))
        connectionState = ConnectionState.CONNECTED
    }

    fun joinRoom(roomId: String, nickname: String) {
        localNickname = nickname
        this.roomId = roomId
        connectionState = ConnectionState.CONNECTING

        val newTransport = FirebaseSessionTransport()
        wire(newTransport)
        transport = newTransport
        newTransport.join(roomId, nickname)

        participants.clear()
        participants.add(SessionParticipant(newTransport.localParticipantId, nickname))
    }

    fun leave() {
        transport?.stop()
        transport = null
        participants.clear()
        votes.clear()
        isRevealed = false
        connectionState = ConnectionState.IDLE
    }

    // Host-only actions — enforced by the `isHost` guard, not by only the
    // host being able to reach the network: anyone could technically
    // send these, they just won't take effect locally unless the
    // election agrees they're host, and Firestore's security rules only
    // let a current participant write the shared room document at all.

    fun selectDeck(deck: Deck) {
        if (!isHost) return
        selectedDeck = deck
        resetRound()
        transport?.send(SessionMessage.DeckChanged(deck))
    }

    fun updateTwoRoundsEnabled(enabled: Boolean) {
        if (!isHost) return
        twoRoundsEnabled = enabled
        resetRound()
        transport?.send(SessionMessage.SettingsChanged(enabled))
    }

    fun resetRound() {
        if (!isHost) return
        votes.clear()
        isRevealed = false
        transport?.send(SessionMessage.Reset)
    }

    fun reveal() {
        if (!isHost) return
        isRevealed = true
        transport?.send(SessionMessage.Reveal)
    }

    // Voting (any participant)

    /** The round the participant hasn't voted on yet, or null once both are done. */
    fun currentRound(participantId: String): RiskRound? {
        if (!isTwoRoundFlow) return null
        val vote = votes[participantId]
        if (vote?.likelihoodLabel == null) return RiskRound.LIKELIHOOD
        if (vote.impactLabel == null) return RiskRound.IMPACT
        return null
    }

    fun pick(card: PokerCard) {
        val localId = localParticipantId ?: return
        val state = votes[localId] ?: VoteState()
        val round: RiskRound?

        if (isTwoRoundFlow) {
            round = if (state.likelihoodLabel == null) RiskRound.LIKELIHOOD else RiskRound.IMPACT
            if (round == RiskRound.LIKELIHOOD) state.likelihoodLabel = card.label else state.impactLabel = card.label
        } else {
            round = null
            state.singleLabel = card.label
        }

        votes[localId] = state
        transport?.send(SessionMessage.Vote(localId, round, card.label))
    }

    fun hasVoted(participantId: String): Boolean = votes[participantId]?.hasVoted ?: false

    /** True once the local player has voted at least once in the current
     * two-round flow, i.e. there's something to step back to. */
    val canGoBack: Boolean
        get() {
            val localId = localParticipantId ?: return false
            return isTwoRoundFlow && votes[localId]?.likelihoodLabel != null
        }

    /** Steps back one round (Impact -> Likelihood) so the answer can be
     * changed, without discarding the other round's already-cast vote. */
    fun goBack() {
        val localId = localParticipantId ?: return
        val state = votes[localId] ?: return
        val round: RiskRound
        if (state.impactLabel != null) {
            state.impactLabel = null
            round = RiskRound.IMPACT
        } else if (state.likelihoodLabel != null) {
            state.likelihoodLabel = null
            round = RiskRound.LIKELIHOOD
        } else {
            return
        }
        votes[localId] = state
        transport?.send(SessionMessage.ClearRoundVote(localId, round))
    }

    // Wiring

    private fun wire(transport: SessionTransport) {
        transport.onPeerConnected = { id, nickname -> handlePeerConnected(id, nickname) }
        transport.onPeerDisconnected = { id -> handlePeerDisconnected(id) }
        transport.onReceive = { message, senderId -> handle(message, senderId) }
    }

    private fun handlePeerConnected(id: String, nickname: String) {
        connectionState = ConnectionState.CONNECTED
        if (participants.none { it.id == id }) {
            participants.add(SessionParticipant(id, nickname))
        }
        // Bring a newly-connected peer up to date on room state. Harmless
        // if several existing members all do this — everyone converges on
        // the same values — but only the (now newly re-evaluated) host
        // bothers, to avoid a burst of redundant messages.
        if (isHost) {
            transport?.send(SessionMessage.DeckChanged(selectedDeck))
            transport?.send(SessionMessage.SettingsChanged(twoRoundsEnabled))
        }
    }

    private fun handlePeerDisconnected(id: String) {
        participants.removeAll { it.id == id }
        votes.remove(id)
    }

    private fun handle(message: SessionMessage, senderId: String) {
        when (message) {
            is SessionMessage.Hello, is SessionMessage.Roster -> Unit // no longer used — everyone builds their own roster directly

            is SessionMessage.DeckChanged -> selectedDeck = message.deck

            is SessionMessage.SettingsChanged -> twoRoundsEnabled = message.twoRoundsEnabled

            is SessionMessage.Vote -> {
                val state = votes[message.participantId] ?: VoteState()
                when (message.round) {
                    RiskRound.LIKELIHOOD -> state.likelihoodLabel = message.cardLabel
                    RiskRound.IMPACT -> state.impactLabel = message.cardLabel
                    null -> state.singleLabel = message.cardLabel
                }
                votes[message.participantId] = state
            }

            is SessionMessage.ClearVote -> votes.remove(message.participantId)

            is SessionMessage.ClearRoundVote -> {
                val state = votes[message.participantId] ?: VoteState()
                when (message.round) {
                    RiskRound.LIKELIHOOD -> state.likelihoodLabel = null
                    RiskRound.IMPACT -> state.impactLabel = null
                }
                votes[message.participantId] = state
            }

            SessionMessage.Reset -> {
                votes.clear()
                isRevealed = false
            }

            SessionMessage.Reveal -> isRevealed = true
        }
    }
}
