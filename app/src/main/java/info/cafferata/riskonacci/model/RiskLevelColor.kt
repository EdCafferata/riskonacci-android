package info.cafferata.riskonacci.model

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

/**
 * A green→red gradient with fixed saturation/brightness, so every level
 * in a risk scale is equally readable as a card background — system
 * colors look fine as small icons but aren't equally bright at full
 * saturation, which made some selected cards read as noticeably darker
 * than others. Mirrors the iOS app's `RiskLevelColor.swift` formula
 * exactly so both platforms produce the same scale.
 */
object RiskLevelColor {
    fun color(level: Int, outOf: Int): Color {
        val t = if (outOf > 1) level.toDouble() / (outOf - 1) else 0.0
        val hue = ((1 - t) * 0.33 * 360).toFloat()
        val argb = AndroidColor.HSVToColor(floatArrayOf(hue, 0.7f, 0.85f))
        return Color(argb)
    }
}
