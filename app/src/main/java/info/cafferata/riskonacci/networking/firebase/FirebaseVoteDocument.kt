package info.cafferata.riskonacci.networking.firebase

import com.google.firebase.firestore.PropertyName
import info.cafferata.riskonacci.model.RiskRound

/**
 * One document per participant per round
 * (`rooms/{roomId}/votes/{authUid}_{roundKey}`), owned by that
 * participant's own device. `epoch` ties a vote to a specific round of
 * the room (bumped by the host on every reset) — everyone only reads
 * votes matching the room's current epoch, so a stale vote from before a
 * reset is simply ignored rather than needing to be deleted by someone
 * who doesn't have permission to.
 *
 * `roundKey` is stored as the plain lowercase string ("single" /
 * "likelihood" / "impact") to match the iOS app's Swift `RoundKey` enum's
 * raw value exactly — Kotlin enum names default to uppercase
 * (`SINGLE`), which would silently break cross-platform reads if used
 * directly, so this is a hand-mapped string instead of a typed enum
 * property.
 */
data class FirebaseVoteDocument(
    @get:PropertyName("participantID") @set:PropertyName("participantID")
    var participantId: String = "",

    var roundKey: String = RoundKey.SINGLE.wireValue,

    var cardLabel: String = "",

    var epoch: Int = 0,
) {
    enum class RoundKey(val wireValue: String) {
        SINGLE("single"), LIKELIHOOD("likelihood"), IMPACT("impact");

        companion object {
            fun from(round: RiskRound?): RoundKey = when (round) {
                null -> SINGLE
                RiskRound.LIKELIHOOD -> LIKELIHOOD
                RiskRound.IMPACT -> IMPACT
            }

            fun from(wireValue: String): RoundKey = entries.first { it.wireValue == wireValue }
        }

        val round: RiskRound?
            get() = when (this) {
                SINGLE -> null
                LIKELIHOOD -> RiskRound.LIKELIHOOD
                IMPACT -> RiskRound.IMPACT
            }
    }

    companion object {
        const val COLLECTION = "votes"

        fun documentId(participantId: String, roundKey: RoundKey): String = "${participantId}_${roundKey.wireValue}"
    }
}
