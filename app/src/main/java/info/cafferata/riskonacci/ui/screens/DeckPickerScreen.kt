package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.ui.iconForSymbolName

/**
 * Mirrors the iOS app's `DeckPickerView.swift`: each deck in its own
 * card with a visible gap between them, and an extra gap before Risk to
 * set it apart as the app's distinguishing deck.
 *
 * "Play together" (local multiplayer) and the tip jar aren't in this
 * first Android pass yet — see the port notes for why.
 */
@Composable
fun DeckPickerScreen(onDeckSelected: (Deck) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(info.cafferata.riskonacci.R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 16.dp),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val nonRiskDecks = Deck.entries.filter { it != Deck.RISK }
            items(nonRiskDecks) { deck -> DeckRow(deck, onDeckSelected) }

            item {
                androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))
            }

            item {
                DeckRow(Deck.RISK, onDeckSelected)
            }
        }
    }
}

@Composable
private fun DeckRow(deck: Deck, onDeckSelected: (Deck) -> Unit) {
    Card(
        onClick = { onDeckSelected(deck) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = iconForSymbolName(deck.symbolName)
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
            }
            Text(stringResource(deck.nameRes), style = MaterialTheme.typography.titleMedium)
        }
    }
}
