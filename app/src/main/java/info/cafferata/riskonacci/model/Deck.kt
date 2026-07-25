package info.cafferata.riskonacci.model

import androidx.compose.ui.graphics.Color
import info.cafferata.riskonacci.R

/**
 * Mirrors the iOS app's `Deck.swift`. `rawValue` is the plain-English
 * identity used for identity/persistence/network messages (not shown to
 * the user directly) — `nameRes` is the localized display name.
 */
enum class Deck(val rawValue: String, val nameRes: Int, val symbolName: String) {
    FIBONACCI("Fibonacci", R.string.deck_fibonacci, "tag"),
    STANDARD("Standard", R.string.deck_standard, "textformat_123"),
    T_SHIRT("T-Shirt", R.string.deck_tshirt, "tshirt"),
    RISK("Risk", R.string.deck_risk, "warning"),
    ;

    val cards: List<PokerCard>
        get() = when (this) {
            FIBONACCI -> listOf("0", "½", "1", "2", "3", "5", "8", "13", "21", "34", "55", "89")
                .map { PokerCard(it) } + specialCards

            STANDARD -> listOf("0", "1", "2", "3", "5", "8", "13", "20", "40", "100")
                .map { PokerCard(it) } + specialCards

            T_SHIRT -> listOf("XS", "S", "M", "L", "XL", "XXL")
                .map { PokerCard(it) } + specialCards

            RISK -> riskCards + specialCards
        }

    /**
     * Riskonacci's differentiator: risk isn't just Fibonacci in a
     * different color, it's a labeled low→critical scale so a team can
     * see at a glance where the disagreement is, not just a spread of
     * numbers.
     */
    private val riskCards: List<PokerCard>
        get() {
            val labels = listOf("None", "Low", "Medium", "High", "Critical")
            val symbols = listOf("check_circle", "circle_1", "circle_2", "circle_3", "flame")
            return labels.indices.map { i ->
                PokerCard(labels[i], symbolName = symbols[i], tint = RiskLevelColor.color(level = i, outOf = labels.size))
            }
        }

    private val specialCards: List<PokerCard>
        get() = listOf(
            PokerCard("?", symbolName = "question_mark", tint = Color.Gray),
            PokerCard("☕", symbolName = "coffee", tint = Color(0xFF8B5E3C)),
        )

    companion object {
        fun fromRawValue(value: String): Deck? = entries.firstOrNull { it.rawValue == value }
    }
}
