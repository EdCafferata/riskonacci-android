package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.R
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.model.RiskAxis
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

/**
 * Multiplayer equivalent of `CardGridScreen`, bound to
 * `MultiplayerRoomViewModel` instead of the solo `GameViewModel`. Mirrors
 * the iOS app's `RoomCardGridView.swift`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCardGridScreen(room: MultiplayerRoomViewModel) {
    val localId = room.localParticipantId
    val columnCount = if (room.selectedDeck == Deck.RISK) 1 else 2
    val cardHeight = if (columnCount == 1) 72.dp else 140.dp

    val cards = if (room.isTwoRoundFlow && localId != null) {
        val round = room.currentRound(localId) ?: info.cafferata.riskonacci.model.RiskRound.LIKELIHOOD
        if (round == info.cafferata.riskonacci.model.RiskRound.LIKELIHOOD) RiskAxis.LIKELIHOOD.cards else RiskAxis.IMPACT.cards
    } else {
        room.selectedDeck.cards
    }

    val localCardLabel = localId?.let { id ->
        val vote = room.votes[id]
        if (!room.isTwoRoundFlow) {
            vote?.singleLabel
        } else {
            val round = room.currentRound(id) ?: info.cafferata.riskonacci.model.RiskRound.LIKELIHOOD
            if (round == info.cafferata.riskonacci.model.RiskRound.LIKELIHOOD) vote?.likelihoodLabel else vote?.impactLabel
        }
    }

    val titleRes = if (room.isTwoRoundFlow && localId != null) {
        val round = room.currentRound(localId) ?: info.cafferata.riskonacci.model.RiskRound.LIKELIHOOD
        if (round == info.cafferata.riskonacci.model.RiskRound.LIKELIHOOD) RiskAxis.LIKELIHOOD.nameRes else RiskAxis.IMPACT.nameRes
    } else {
        room.selectedDeck.nameRes
    }

    Column {
        ParticipantsRow(room)
        TopAppBar(
            title = { Text(stringResource(titleRes)) },
            navigationIcon = {
                if (room.canGoBack) {
                    IconButton(onClick = { room.goBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            },
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier.widthIn(max = 700.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(cards) { card ->
                    PokerCardView(
                        card = card,
                        isSelected = localCardLabel == card.label,
                        isWide = columnCount == 1,
                        height = cardHeight,
                        onClick = { room.pick(card) },
                    )
                }
            }
        }
    }
}
