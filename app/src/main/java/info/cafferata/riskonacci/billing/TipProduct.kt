package info.cafferata.riskonacci.billing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.ui.graphics.vector.ImageVector

/** Product-ID's zoals aan te maken in Play Console > Producten met eenmalige betaling (verbruikbaar, herhaalbaar te fooien — zelfde als iOS' TipProduct.swift). */
enum class TipProduct(val id: String, val displayName: String, val icon: ImageVector) {
    ESPRESSO("tip_espresso", "Espresso", Icons.Filled.LocalCafe),
    CORNETTO("tip_cornetto", "Cornetto", Icons.Filled.EmojiFoodBeverage),
    APERITIVO("tip_aperitivo", "Aperitivo", Icons.Filled.WineBar);

    companion object {
        val ALL = entries
        fun forId(id: String): TipProduct? = entries.firstOrNull { it.id == id }
    }
}
