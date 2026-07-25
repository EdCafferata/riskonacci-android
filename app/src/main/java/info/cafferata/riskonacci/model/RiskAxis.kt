package info.cafferata.riskonacci.model

import info.cafferata.riskonacci.R

/**
 * The two-round "Risk Poker" scale (TMAP-style): likelihood and impact are
 * voted on separately, then combined into a position on a risk matrix.
 * Distinct from the single-card Risk deck in `Deck.kt`. Mirrors the iOS
 * app's `RiskAxis.swift`.
 */
enum class RiskAxis(val nameRes: Int, private val symbolPrefix: String) {
    LIKELIHOOD(R.string.axis_likelihood, "likelihood"),
    IMPACT(R.string.axis_impact, "impact"),
    ;

    val cards: List<PokerCard>
        get() {
            val labels = when (this) {
                LIKELIHOOD -> listOf("Rare", "Unlikely", "Possible", "Likely", "Almost Certain")
                IMPACT -> listOf("Negligible", "Minor", "Moderate", "Major", "Catastrophic")
            }
            return labels.indices.map { i ->
                PokerCard(
                    label = labels[i],
                    symbolName = "$symbolPrefix-${i + 1}",
                    tint = RiskLevelColor.color(level = i, outOf = labels.size),
                )
            }
        }

    /**
     * 0-based index of a card within this axis's scale, used to place it
     * on the risk matrix. Falls back to 0 (lowest) if the card isn't
     * actually one of this axis's cards.
     */
    fun index(of: PokerCard): Int {
        val i = cards.indexOfFirst { it.label == of.label }
        return if (i >= 0) i else 0
    }
}
