package info.cafferata.riskonacci.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Play Billing-wrapper voor de tip jar (Android-equivalent van StoreKit 2
 * TipJarStore.swift op iOS): laadt de drie fooi-producten en verwerkt een
 * fooi als verbruikbare aankoop, dus opnieuw te fooien — geen "al gekocht"-status.
 */
class TipJarStore(context: Context) : PurchasesUpdatedListener {
    private val _products = mutableStateOf<List<ProductDetails>>(emptyList())
    val products: State<List<ProductDetails>> = _products

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _lastTipMessage = mutableStateOf<String?>(null)
    val lastTipMessage: State<String?> = _lastTipMessage

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    laadProducten()
                    ruimAchtergelatenAankopenOp()
                } else {
                    _isLoading.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                // Volgende actie van de gebruiker triggert een herverbinding via startConnection.
            }
        })
    }

    private fun laadProducten() {
        val productList = TipProduct.ALL.map { tip ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(tip.id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { result, queryProductDetailsResult ->
            _isLoading.value = false
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = queryProductDetailsResult.productDetailsList.sortedBy {
                    it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
                }
            }
        }
    }

    fun tip(activity: Activity, product: ProductDetails) {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) return
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Verbruiken i.p.v. bevestigen: een fooi mag herhaald gegeven worden, zelfde gedrag als iOS.
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.consumeAsync(consumeParams) { consumeResult, _ ->
                    if (consumeResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _lastTipMessage.value = "Grazie mille! 🍇"
                    }
                }
            }
        }
    }

    /** Zorgt dat een fooi die tijdens een vorige sessie nog niet verbruikt werd, alsnog wordt afgehandeld. */
    fun ruimAchtergelatenAankopenOp() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync
            purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
                val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                billingClient.consumeAsync(consumeParams) { _, _ -> }
            }
        }
    }
}
