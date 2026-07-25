package info.cafferata.riskonacci.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.model.PokerCard
import info.cafferata.riskonacci.model.RiskAxis
import info.cafferata.riskonacci.model.RiskRound

/** Mirrors the iOS app's `GameViewModel.swift` — solo single-device play. */
class GameViewModel : ViewModel() {
    var selectedDeck by mutableStateOf<Deck?>(null)
        private set
    var selectedCard by mutableStateOf<PokerCard?>(null)
        private set
    var isRevealed by mutableStateOf(false)
        private set

    /**
     * Two-round Likelihood × Impact mode for the Risk deck. On by
     * default; in a multiplayer room only the host can toggle this — for
     * solo play the local player stands in as host.
     */
    var twoRoundsEnabled by mutableStateOf(true)
    var likelihoodCard by mutableStateOf<PokerCard?>(null)
        private set
    var impactCard by mutableStateOf<PokerCard?>(null)
        private set
    var currentRiskRound by mutableStateOf(RiskRound.LIKELIHOOD)
        private set

    val isTwoRoundFlow: Boolean
        get() = selectedDeck == Deck.RISK && twoRoundsEnabled

    val currentCards: List<PokerCard>
        get() {
            val deck = selectedDeck ?: return emptyList()
            if (!isTwoRoundFlow) return deck.cards
            return if (currentRiskRound == RiskRound.LIKELIHOOD) RiskAxis.LIKELIHOOD.cards else RiskAxis.IMPACT.cards
        }

    val currentTitleRes: Int?
        get() {
            val deck = selectedDeck ?: return null
            if (!isTwoRoundFlow) return deck.nameRes
            return if (currentRiskRound == RiskRound.LIKELIHOOD) RiskAxis.LIKELIHOOD.nameRes else RiskAxis.IMPACT.nameRes
        }

    fun chooseDeck(deck: Deck) {
        selectedDeck = deck
        reset()
    }

    fun isSelected(card: PokerCard): Boolean = if (isTwoRoundFlow) {
        if (currentRiskRound == RiskRound.LIKELIHOOD) likelihoodCard == card else impactCard == card
    } else {
        selectedCard == card
    }

    fun pick(card: PokerCard) {
        if (!isTwoRoundFlow) {
            selectedCard = card
            isRevealed = true
            return
        }

        when (currentRiskRound) {
            RiskRound.LIKELIHOOD -> {
                likelihoodCard = card
                currentRiskRound = RiskRound.IMPACT
            }
            RiskRound.IMPACT -> {
                impactCard = card
                isRevealed = true
            }
        }
    }

    /**
     * True once Likelihood has been picked — i.e. there's a previous
     * round to step back to and change.
     */
    val canGoBack: Boolean
        get() = isTwoRoundFlow && likelihoodCard != null

    /**
     * Steps back one round (Impact → Likelihood) to change an answer,
     * without discarding the other round's already-cast pick.
     */
    fun goBack() {
        if (impactCard != null) {
            impactCard = null
            currentRiskRound = RiskRound.IMPACT
        } else if (likelihoodCard != null) {
            likelihoodCard = null
            currentRiskRound = RiskRound.LIKELIHOOD
        }
    }

    fun reset() {
        selectedCard = null
        likelihoodCard = null
        impactCard = null
        currentRiskRound = RiskRound.LIKELIHOOD
        isRevealed = false
    }

    fun backToDecks() {
        selectedDeck = null
        reset()
    }
}
