package info.cafferata.riskonacci.networking.firebase

import java.util.Date

/**
 * One document per participant (`rooms/{roomId}/participants/{authUid}`),
 * owned by that participant's own device — the document ID is literally
 * their Firebase Auth (anonymous) UID, which is what makes the Firestore
 * security rule for this collection a simple, exact match rather than a
 * query. `lastSeen` is a heartbeat: other devices treat a participant as
 * gone once it's stale for too long, without needing permission to
 * delete that participant's own record. Field names already match the
 * iOS app's `FirebaseParticipantDocument.swift` without needing any
 * `@PropertyName` overrides.
 */
data class FirebaseParticipantDocument(
    var nickname: String = "",
    var lastSeen: Date = Date(),
) {
    companion object {
        const val COLLECTION = "participants"
    }
}
