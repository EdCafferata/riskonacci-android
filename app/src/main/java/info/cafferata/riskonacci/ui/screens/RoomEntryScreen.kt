package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.R
import info.cafferata.riskonacci.networking.RoomId
import info.cafferata.riskonacci.viewmodel.ConnectionState
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

/**
 * Entry point for multiplayer: pick a nickname, then either host a new
 * room (gets a fresh 5-character code to share) or join one with a code
 * from someone else. Works the same whether everyone's on the same
 * Wi-Fi or scattered anywhere with a connection — and the same whether
 * the other player is on iPhone or Android — since every device just
 * talks to the same shared Firebase room. Mirrors the iOS app's
 * `RoomEntryView.swift`.
 */
@Composable
fun RoomEntryScreen(room: MultiplayerRoomViewModel) {
    if (room.connectionState == ConnectionState.IDLE) {
        EntryForm(room)
    } else {
        RoomScreen(room)
    }
}

@Composable
private fun EntryForm(room: MultiplayerRoomViewModel) {
    var nickname by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    val trimmedNickname = nickname.trim()

    Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 480.dp).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
            Text(
                stringResource(R.string.subtitle_play_together),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text(stringResource(R.string.field_your_name)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        if (isJoining) {
            OutlinedTextField(
                value = joinCode,
                onValueChange = { joinCode = it.uppercase() },
                label = { Text(stringResource(R.string.field_room_code)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(
            onClick = { room.hostRoom(trimmedNickname) },
            enabled = trimmedNickname.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_host_room))
        }

        if (isJoining) {
            OutlinedButton(
                onClick = { room.joinRoom(joinCode.uppercase(), trimmedNickname) },
                enabled = trimmedNickname.isNotEmpty() && RoomId.isValid(joinCode),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_join))
            }
        } else {
            OutlinedButton(onClick = { isJoining = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_join_room_instead))
            }
        }

        Spacer(Modifier)
    }
}
