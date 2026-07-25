package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.model.PokerCard
import info.cafferata.riskonacci.ui.iconForSymbolName
import info.cafferata.riskonacci.ui.localizedLabel

/**
 * A single tappable card. Mirrors the iOS app's `PokerCardView.swift` —
 * `isWide` lays out icon + label as a left-aligned row (used for the
 * single-column Risk deck, which reads better as a list), otherwise a
 * centered icon-over-label stack for the 2-column decks.
 */
@Composable
fun PokerCardView(
    card: PokerCard,
    isSelected: Boolean,
    isWide: Boolean,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val tint = if (card.tint == Color.Unspecified) MaterialTheme.colorScheme.primary else card.tint
    val iconColor = if (isSelected) Color.White else tint
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val icon = iconForSymbolName(card.symbolName)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) tint else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(14.dp))
                }
                Text(
                    text = localizedLabel(card),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = localizedLabel(card),
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
