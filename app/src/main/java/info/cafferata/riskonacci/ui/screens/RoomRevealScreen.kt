package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import info.cafferata.riskonacci.model.RiskAxis
import info.cafferata.riskonacci.ui.localizedLabel
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

/** Mirrors the iOS app's `RoomRevealView.swift`. */
@Composable
fun RoomRevealScreen(room: MultiplayerRoomViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (room.isTwoRoundFlow) {
                MatrixReveal(room)
            } else {
                ListReveal(room)
            }
        }

        if (room.isHost) {
            Button(onClick = { room.resetRound() }) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(stringResource(R.string.action_new_round))
            }
        } else {
            Spacer(Modifier)
        }
    }
}

@Composable
private fun MatrixReveal(room: MultiplayerRoomViewModel) {
    val dots = room.participants.mapNotNull { participant ->
        val vote = room.votes[participant.id] ?: return@mapNotNull null
        val likelihoodLabel = vote.likelihoodLabel ?: return@mapNotNull null
        val impactLabel = vote.impactLabel ?: return@mapNotNull null
        val likelihoodIndex = RiskAxis.LIKELIHOOD.cards.indexOfFirst { it.label == likelihoodLabel }.coerceAtLeast(0)
        val impactIndex = RiskAxis.IMPACT.cards.indexOfFirst { it.label == impactLabel }.coerceAtLeast(0)
        RiskMatrixDot(
            id = participant.id,
            likelihoodIndex = likelihoodIndex,
            impactIndex = impactIndex,
            label = participant.nickname.take(1).uppercase(),
            tint = MaterialTheme.colorScheme.primary,
        )
    }

    RiskMatrixGrid(dots = dots, modifier = Modifier.widthIn(max = 340.dp))
}

@Composable
private fun ListReveal(room: MultiplayerRoomViewModel) {
    Column(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        room.participants.forEach { participant ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(participant.nickname, style = MaterialTheme.typography.titleMedium)
                    Text(
                        room.votes[participant.id]?.singleLabel?.let { localizedLabel(it) } ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
