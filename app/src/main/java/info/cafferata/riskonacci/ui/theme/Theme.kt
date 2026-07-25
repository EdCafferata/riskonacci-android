package info.cafferata.riskonacci.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = RiskonacciGold,
    secondary = RiskonacciGoldDark,
)

private val DarkColors = darkColorScheme(
    primary = RiskonacciGold,
    secondary = RiskonacciGoldDark,
)

@Composable
fun RiskonacciTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) is available on Android 12+, but
    // disabled by default here so the app keeps its own gold brand color
    // instead of matching the user's wallpaper — same fixed-brand
    // approach as the iOS AccentColor asset.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
