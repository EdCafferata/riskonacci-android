package info.cafferata.riskonacci.networking

import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.model.RiskRound

/**
 * Everything that travels between devices in a room. Mirrors the iOS
 * app's `SessionMessage.swift` — cards travel as their `label` (a plain
 * `String`) rather than a full `PokerCard`, since `PokerCard.tint` isn't
 * serializable and the label is enough to look the card back up from the
 * shared deck definition on the receiving end.
 */
sealed class SessionMessage {
    data class Hello(val participant: SessionParticipant) : SessionMessage()
    data class Roster(val participants: List<SessionParticipant>) : SessionMessage()
    data class DeckChanged(val deck: Deck) : SessionMessage()
    data class SettingsChanged(val twoRoundsEnabled: Boolean) : SessionMessage()
    data class Vote(val participantId: String, val round: RiskRound?, val cardLabel: String) : SessionMessage()
    data class ClearVote(val participantId: String) : SessionMessage()

    /** Stepping back one round (e.g. Impact -> Likelihood) to change an
     * answer, without discarding the other round's vote too. */
    data class ClearRoundVote(val participantId: String, val round: RiskRound) : SessionMessage()
    object Reset : SessionMessage()
    object Reveal : SessionMessage()
}
