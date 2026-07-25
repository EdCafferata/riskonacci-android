package info.cafferata.riskonacci.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.UUID

/**
 * One dot on the matrix — plain white for the solo reveal, or labeled
 * (initial + tint) for the multiplayer reveal so several dots stay
 * distinguishable. Mirrors `RiskMatrixGrid.swift`'s `RiskMatrixDot`.
 */
data class RiskMatrixDot(
    val id: String = UUID.randomUUID().toString(),
    val likelihoodIndex: Int,
    val impactIndex: Int,
    val label: String? = null,
    val tint: Color = Color.White,
)

private const val GRID_SIZE = 5

/**
 * A 5×5 risk matrix: likelihood on the x-axis, impact on the y-axis,
 * green→red by magnitude. Mirrors `RiskMatrixGrid.swift`.
 */
@Composable
fun RiskMatrixGrid(dots: List<RiskMatrixDot>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        val cell = size.minDimension / GRID_SIZE

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                drawRect(
                    color = cellColor(row, col),
                    topLeft = Offset(col * cell, row * cell),
                    size = androidx.compose.ui.geometry.Size(cell, cell),
                )
            }
        }

        dots.forEach { dot ->
            val cx = dot.likelihoodIndex * cell + cell / 2
            val cy = (GRID_SIZE - 1 - dot.impactIndex) * cell + cell / 2
            val radius = cell * 0.25f
            drawCircle(color = dot.tint, radius = radius, center = Offset(cx, cy))
        }
    }
}

/**
 * row 0 is the top of the grid (highest impact), col 0 is the left
 * (lowest likelihood) — standard risk-matrix orientation.
 */
private fun cellColor(row: Int, col: Int): Color {
    val impactLevel = GRID_SIZE - 1 - row
    val likelihoodLevel = col
    val magnitude = ((impactLevel + 1) * (likelihoodLevel + 1)).toDouble() / (GRID_SIZE * GRID_SIZE)
    val hue = ((1 - magnitude) * 0.33 * 360).toFloat()
    return Color(AndroidColor.HSVToColor(floatArrayOf(hue, 0.75f, 0.9f)))
}
