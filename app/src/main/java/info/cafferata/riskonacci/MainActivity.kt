package info.cafferata.riskonacci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import info.cafferata.riskonacci.billing.TipJarStore
import info.cafferata.riskonacci.ui.screens.CardGridScreen
import info.cafferata.riskonacci.ui.screens.DeckPickerScreen
import info.cafferata.riskonacci.ui.screens.RevealScreen
import info.cafferata.riskonacci.ui.screens.RiskMatrixRevealScreen
import info.cafferata.riskonacci.ui.screens.RoomEntryScreen
import info.cafferata.riskonacci.ui.screens.TipJarScreen
import info.cafferata.riskonacci.ui.theme.RiskonacciTheme
import info.cafferata.riskonacci.viewmodel.GameViewModel
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiskonacciTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    RiskonacciApp()
                }
            }
        }
    }
}

/** Mirrors the iOS app's `ContentView.swift` root flow. */
@Composable
private fun RiskonacciApp() {
    val viewModel: GameViewModel = viewModel()
    var showMultiplayer by remember { mutableStateOf(false) }
    var showTipJar by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val tipJarStore = remember { TipJarStore(context) }

    if (showTipJar) {
        TipJarScreen(store = tipJarStore, onDismiss = { showTipJar = false })
    }

    if (showMultiplayer) {
        val room: MultiplayerRoomViewModel = viewModel()
        RoomEntryScreen(room)
        return
    }

    when {
        viewModel.isRevealed -> {
            val likelihood = viewModel.likelihoodCard
            val impact = viewModel.impactCard
            val single = viewModel.selectedCard
            when {
                viewModel.isTwoRoundFlow && likelihood != null && impact != null ->
                    RiskMatrixRevealScreen(likelihood = likelihood, impact = impact, onDismiss = { viewModel.reset() })
                single != null ->
                    RevealScreen(card = single, onDismiss = { viewModel.reset() })
            }
        }

        viewModel.selectedDeck != null ->
            CardGridScreen(
                viewModel = viewModel,
                onRevealed = {},
                onBackToDecks = { viewModel.backToDecks() },
            )

        else ->
            DeckPickerScreen(
                onDeckSelected = { deck -> viewModel.chooseDeck(deck) },
                onPlayTogether = { showMultiplayer = true },
                onTipJar = { showTipJar = true },
            )
    }
}
