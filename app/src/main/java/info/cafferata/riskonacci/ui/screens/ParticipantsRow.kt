package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.networking.SessionParticipant
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

/**
 * A compact strip of chips, one per participant, showing who's voted —
 * not what they voted, that stays hidden until the host reveals. Mirrors
 * the iOS app's `ParticipantsRow.swift`.
 */
@Composable
fun ParticipantsRow(room: MultiplayerRoomViewModel) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        room.participants.forEach { participant -> Chip(room, participant) }
    }
}

@Composable
private fun Chip(room: MultiplayerRoomViewModel, participant: SessionParticipant) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (room.hostId == participant.id) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
            }
            Text(participant.nickname, style = MaterialTheme.typography.labelMedium)
            val voted = room.hasVoted(participant.id)
            Icon(
                if (voted) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                contentDescription = null,
                tint = if (voted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
