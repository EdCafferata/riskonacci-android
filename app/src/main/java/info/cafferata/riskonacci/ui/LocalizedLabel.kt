package info.cafferata.riskonacci.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import info.cafferata.riskonacci.R
import info.cafferata.riskonacci.model.PokerCard

/**
 * Maps a card's plain-English `label` to its translated string resource.
 * Numeric/symbol labels (Fibonacci numbers, T-shirt sizes, "?", "☕")
 * aren't in the map and fall back to the raw label — they read the same
 * in every language, mirroring how the iOS app's String Catalog lookup
 * also just falls back to the raw key when there's nothing to translate.
 */
private val labelResources: Map<String, Int> = mapOf(
    "None" to R.string.card_none,
    "Low" to R.string.card_low,
    "Medium" to R.string.card_medium,
    "High" to R.string.card_high,
    "Critical" to R.string.card_critical,
    "Rare" to R.string.card_rare,
    "Unlikely" to R.string.card_unlikely,
    "Possible" to R.string.card_possible,
    "Likely" to R.string.card_likely,
    "Almost Certain" to R.string.card_almost_certain,
    "Negligible" to R.string.card_negligible,
    "Minor" to R.string.card_minor,
    "Moderate" to R.string.card_moderate,
    "Major" to R.string.card_major,
    "Catastrophic" to R.string.card_catastrophic,
)

@Composable
fun localizedLabel(card: PokerCard): String {
    val resId = labelResources[card.label] ?: return card.label
    return stringResource(resId)
}
