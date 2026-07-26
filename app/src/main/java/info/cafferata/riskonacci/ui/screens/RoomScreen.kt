package info.cafferata.riskonacci.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.R
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

/** Mirrors the iOS app's `RoomView.swift`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(room: MultiplayerRoomViewModel) {
    val context = LocalContext.current
    val shareJoinPrefix = stringResource(R.string.share_join_room_prefix)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.label_room), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(room.roomId, style = MaterialTheme.typography.titleMedium)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "$shareJoinPrefix ${room.roomId}")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share_code))
                    }
                    TextButton(onClick = { room.leave() }) {
                        Text(stringResource(R.string.action_leave))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (room.isHost) {
                HostControls(room)
            }

            if (room.isRevealed) {
                RoomRevealScreen(room)
            } else {
                RoomCardGridScreen(room)
            }
        }
    }
}

@Composable
private fun HostControls(room: MultiplayerRoomViewModel) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Deck.entries.forEach { deck ->
            FilterChip(
                selected = room.selectedDeck == deck,
                onClick = { room.selectDeck(deck) },
                label = { Text(stringResource(deck.nameRes)) },
            )
        }

        if (room.selectedDeck == Deck.RISK) {
            val icon = remember(room.twoRoundsEnabled) { if (room.twoRoundsEnabled) Icons.Filled.CheckCircle else Icons.Filled.Circle }
            FilterChip(
                selected = room.twoRoundsEnabled,
                onClick = { room.updateTwoRoundsEnabled(!room.twoRoundsEnabled) },
                label = { Text(stringResource(R.string.action_two_rounds)) },
                leadingIcon = { Icon(icon, contentDescription = null) },
            )
        }

        FilterChip(
            selected = false,
            onClick = { room.resetRound() },
            label = { Text(stringResource(R.string.action_new_round)) },
            leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
        )

        FilterChip(
            selected = false,
            onClick = { room.reveal() },
            label = { Text(stringResource(R.string.action_reveal)) },
            leadingIcon = { Icon(Icons.Filled.RemoveRedEye, contentDescription = null) },
        )
    }
}
