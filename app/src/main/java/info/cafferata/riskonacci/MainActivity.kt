package info.cafferata.riskonacci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import info.cafferata.riskonacci.billing.TipJarStore
import info.cafferata.riskonacci.ui.screens.CardGridScreen
import info.cafferata.riskonacci.ui.screens.DeckPickerScreen
import info.cafferata.riskonacci.ui.screens.RevealScreen
import info.cafferata.riskonacci.ui.screens.RiskMatrixRevealScreen
import info.cafferata.riskonacci.ui.screens.RoomEntryScreen
import info.cafferata.riskonacci.ui.screens.TipJarScreen
import info.cafferata.riskonacci.ui.theme.RiskonacciLogoGreen
import info.cafferata.riskonacci.ui.theme.RiskonacciLogoRed
import info.cafferata.riskonacci.ui.theme.RiskonacciTheme
import info.cafferata.riskonacci.viewmodel.ConnectionState
import info.cafferata.riskonacci.viewmodel.GameViewModel
import info.cafferata.riskonacci.viewmodel.MultiplayerRoomViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiskonacciTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .riskonacciLogoWash()
                            .safeDrawingPadding(),
                    ) {
                        RiskonacciApp()
                    }
                }
            }
        }
    }
}

/** Mirrors the iOS app's `ContentView.swift` root flow. */
@Composable
private fun RiskonacciApp() {
    val viewModel: GameViewModel = viewModel()
    val room: MultiplayerRoomViewModel = viewModel()
    var showMultiplayer by remember { mutableStateOf(false) }
    var showTipJar by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val tipJarStore = remember { TipJarStore(context) }

    // System back button: unwind the app's own navigation one level at a
    // time instead of finishing the Activity (which otherwise closes the
    // app outright from every screen). The deck picker is the root — there
    // the handler stays disabled so the system still exits the app. The
    // Tip Jar is a Dialog and already consumes its own back press.
    BackHandler(enabled = showMultiplayer || viewModel.isRevealed || viewModel.selectedDeck != null) {
        when {
            showMultiplayer ->
                if (room.connectionState == ConnectionState.IDLE) showMultiplayer = false
                else room.leave()
            viewModel.isRevealed -> viewModel.reset()
            viewModel.selectedDeck != null ->
                if (viewModel.canGoBack) viewModel.goBack() else viewModel.backToDecks()
        }
    }

    if (showTipJar) {
        TipJarScreen(store = tipJarStore, onDismiss = { showTipJar = false })
    }

    if (showMultiplayer) {
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

/**
 * A very faint diagonal wash — the app icon's tricolore, green in the
 * top-left corner fading out through the middle to red in the bottom-right,
 * on the same diagonal as the logo — drawn behind every screen so the whole
 * app quietly echoes the icon. Kept subtle (low alpha), a touch stronger in
 * dark mode where the base is near-black.
 */
@Composable
private fun Modifier.riskonacciLogoWash(): Modifier {
    val dark = isSystemInDarkTheme()
    val greenAlpha = if (dark) 0.54f else 0.50f
    val redAlpha = if (dark) 0.26f else 0.20f
    return this.background(
        Brush.linearGradient(
            0.0f to RiskonacciLogoGreen.copy(alpha = greenAlpha),
            0.32f to RiskonacciLogoGreen.copy(alpha = greenAlpha * 0.55f),
            0.58f to Color.Transparent,
            1.0f to RiskonacciLogoRed.copy(alpha = redAlpha),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
    )
}
