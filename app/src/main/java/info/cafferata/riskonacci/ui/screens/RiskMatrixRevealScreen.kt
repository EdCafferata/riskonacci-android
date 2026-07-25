package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.R
import info.cafferata.riskonacci.model.PokerCard
import info.cafferata.riskonacci.model.RiskAxis
import info.cafferata.riskonacci.ui.iconForSymbolName
import info.cafferata.riskonacci.ui.localizedLabel

/**
 * The combined reveal for the two-round Risk flow: Likelihood and Impact
 * shown together as one point on a risk matrix. Mirrors
 * `RiskMatrixRevealView.swift`.
 */
@Composable
fun RiskMatrixRevealScreen(likelihood: PokerCard, impact: PokerCard, onDismiss: () -> Unit) {
    val likelihoodIndex = RiskAxis.LIKELIHOOD.index(of = likelihood)
    val impactIndex = RiskAxis.IMPACT.index(of = impact)
    val magnitude = (likelihoodIndex + 1) * (impactIndex + 1)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.risk_matrix_title), style = MaterialTheme.typography.headlineSmall)

            RiskMatrixGrid(
                dots = listOf(RiskMatrixDot(likelihoodIndex = likelihoodIndex, impactIndex = impactIndex)),
                modifier = Modifier.widthIn(max = 320.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                axisSummary(titleRes = R.string.axis_likelihood, card = likelihood)
                axisSummary(titleRes = R.string.axis_impact, card = impact)
            }

            Text(
                stringResource(R.string.magnitude_format, magnitude.toString()),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        Button(onClick = onDismiss) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_pick_again))
        }
    }
}

@Composable
private fun axisSummary(titleRes: Int, card: PokerCard) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(titleRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val icon = iconForSymbolName(card.symbolName)
        val tint = if (card.tint == Color.Unspecified) MaterialTheme.colorScheme.primary else card.tint
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Text(localizedLabel(card), style = MaterialTheme.typography.titleMedium, color = tint)
        }
    }
}
