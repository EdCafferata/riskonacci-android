package info.cafferata.riskonacci.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.cafferata.riskonacci.model.Deck
import info.cafferata.riskonacci.viewmodel.GameViewModel

/** Mirrors the iOS app's `CardGridView.swift`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardGridScreen(
    viewModel: GameViewModel,
    onRevealed: () -> Unit,
    onBackToDecks: () -> Unit,
) {
    val cards = viewModel.currentCards
    val columnCount = if (viewModel.selectedDeck == Deck.RISK) 1 else 2
    val cardHeight = if (columnCount == 1) 72.dp else 140.dp

    LaunchedEffect(viewModel.isRevealed) {
        if (viewModel.isRevealed) onRevealed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleRes = viewModel.currentTitleRes
                    if (titleRes != null) Text(stringResource(titleRes))
                },
                navigationIcon = {
                    if (viewModel.canGoBack) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(info.cafferata.riskonacci.R.string.action_back))
                        }
                    }
                },
                actions = {
                    if (viewModel.selectedDeck == Deck.RISK) {
                        IconButton(onClick = {
                            viewModel.twoRoundsEnabled = !viewModel.twoRoundsEnabled
                            viewModel.reset()
                        }) {
                            Icon(
                                if (viewModel.twoRoundsEnabled) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                                contentDescription = stringResource(info.cafferata.riskonacci.R.string.action_two_rounds),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier.widthIn(max = 700.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
            ) {
                items(cards) { card ->
                    PokerCardView(
                        card = card,
                        isSelected = viewModel.isSelected(card),
                        isWide = columnCount == 1,
                        height = cardHeight,
                        onClick = { viewModel.pick(card) },
                    )
                }
            }
        }
    }
}
