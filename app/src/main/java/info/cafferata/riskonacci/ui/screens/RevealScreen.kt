package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.R
import info.cafferata.riskonacci.model.PokerCard
import info.cafferata.riskonacci.ui.iconForSymbolName
import info.cafferata.riskonacci.ui.localizedLabel

/** Full-screen reveal of the picked card. Mirrors `RevealView.swift`. */
@Composable
fun RevealScreen(card: PokerCard, onDismiss: () -> Unit) {
    val tint = if (card.tint == Color.Unspecified) MaterialTheme.colorScheme.primary else card.tint
    val icon = iconForSymbolName(card.symbolName)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        Surface(
            color = tint,
            shape = RoundedCornerShape(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(64.dp))
                }
                Text(
                    text = localizedLabel(card),
                    style = MaterialTheme.typography.displayLarge,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        Button(onClick = onDismiss) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_pick_again))
        }
    }
}
