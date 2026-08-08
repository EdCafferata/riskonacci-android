package info.cafferata.riskonacci.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import info.cafferata.riskonacci.billing.TipJarStore
import info.cafferata.riskonacci.billing.TipProduct
import info.cafferata.riskonacci.ui.theme.RiskonacciGold

@Composable
fun TipJarScreen(store: TipJarStore, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Tip Jar", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                TipJarContent(store)
            }
        }
    }
}

@Composable
private fun TipJarContent(store: TipJarStore) {
    val context = LocalContext.current
    val activity = context as? Activity
    val products by store.products
    val isLoading by store.isLoading
    val lastTipMessage by store.lastTipMessage

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFE0473C), modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Riskonacci is free, always.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "If it saves your team time, a small tip is always appreciated — never required.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RiskonacciGold)
                }
            }
        } else {
            items(TipProduct.ALL, key = { it.id }) { tip ->
                val product = products.firstOrNull { it.productId == tip.id }
                if (product != null && activity != null) {
                    TipRow(tip = tip, prijsTekst = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "", onClick = { store.tip(activity, product) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        if (lastTipMessage != null) {
            item {
                Text(
                    lastTipMessage!!, style = MaterialTheme.typography.titleMedium, color = RiskonacciGold,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center,
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=info.cafferata.riskonacci")))
                    } catch (_: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=info.cafferata.riskonacci")))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rate Riskonacci")
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 32.dp)) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp, vertical = 12.dp))
                Text("Grazie 🇮🇹", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Built by The IT Crowd, made better by everyone who tests it and sends feedback. Once Riskonacci moves to GitHub, code contributors will be credited here too.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Idea by Oscar Sarruco ↗",
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = RiskonacciGold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/oscarsarrucco/")))
                    },
                )
            }
        }
    }
}

@Composable
private fun TipRow(tip: TipProduct, prijsTekst: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(tip.icon, contentDescription = null, tint = RiskonacciGold, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(tip.displayName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(prijsTekst, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
    }
}
