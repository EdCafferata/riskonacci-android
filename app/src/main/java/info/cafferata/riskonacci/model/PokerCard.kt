package info.cafferata.riskonacci.model

import androidx.compose.ui.graphics.Color

/**
 * One selectable card in a deck. `symbolName` is a small icon key (see
 * `IconForSymbolName` at the UI layer for the Material Icons mapping) —
 * kept as a plain string here, mirroring the iOS app's `PokerCard.swift`,
 * so the model layer doesn't need to know about the rendering framework.
 *
 * `label` doubles as the stable identity (decks are recomputed on every
 * access, so identity has to survive that or selection/highlight state
 * breaks after the first render) and as the translatable string resource
 * key — see `LocalizedLabel` at the UI layer.
 */
data class PokerCard(
    val label: String,
    val symbolName: String? = null,
    val tint: Color = Color.Unspecified,
)
