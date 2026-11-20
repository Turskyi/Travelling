package ua.turskyi.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.data.BuildConfig
import ua.turskyi.domain.model.SubscriptionStatusModel
import ua.turskyi.domain.repository.AuthRepository
import ua.turskyi.domain.repository.BillingRepository

class BillingRepositoryImpl(
    private val context: Context,
) : BillingRepository, KoinComponent {

    @Suppress("unused")
    companion object {
        private const val TAG = "BillingRepository"
        const val PRODUCT_ID_CLOUD_BACKUP = "cloud_backup"
        const val BASE_PLAN_ID_MONTHLY = "monthly"
    }

    private val authRepository: AuthRepository by inject()
    private val okHttpClient: OkHttpClient by inject()

    private val backendBaseUrl: String
        get() {
            val url = BuildConfig.BACKEND_BASE_URL
            return if (url.endsWith("/")) url.dropLast(1) else url
        }

    private var billingClient: BillingClient? = null
    private var cachedProductDetails: ProductDetails? = null
    private var cachedOfferToken: String? = null

    override fun startConnection(
        onReady: () -> Unit,
        onError: (String) -> Unit,
        onPurchaseVerified: (SubscriptionStatusModel) -> Unit
    ) {
        val listener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        CoroutineScope(Dispatchers.IO).launch {
                            verifyPurchaseWithBackend(purchase.purchaseToken).onSuccess { status ->
                                onPurchaseVerified(status)
                            }
                        }
                    }
                }
            }
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(listener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onReady()
                } else {
                    onError("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                onError("Billing service disconnected")
            }
        })
    }

    override suspend fun querySubscriptionDetails(): Result<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val client = billingClient ?: throw Exception("BillingClient not initialized")
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_CLOUD_BACKUP)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                val result = client.queryProductDetails(params)
                val detailsList = result.productDetailsList

                if (detailsList.isNullOrEmpty()) {
                    Result.failure(Exception("Product $PRODUCT_ID_CLOUD_BACKUP not found"))
                } else {
                    val details = detailsList.first()
                    cachedProductDetails = details
                    val offerDetails = details.subscriptionOfferDetails?.firstOrNull()
                    cachedOfferToken = offerDetails?.offerToken
                    val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()

                    val formattedPrice = pricingPhase?.formattedPrice ?: "$1.99"
                    val billingPeriod = pricingPhase?.billingPeriod ?: "P1M"

                    Result.success(Pair(formattedPrice, billingPeriod))
                }
            } catch (e: Exception) {
                Log.e(TAG, "querySubscriptionDetails failed", e)
                Result.failure(e)
            }
        }

    override fun launchBillingFlow(
        activity: Any,
        onError: (String) -> Unit
    ) {
        val client = billingClient
        val productDetails = cachedProductDetails
        val offerToken = cachedOfferToken

        if (client == null || productDetails == null || offerToken == null) {
            onError("Billing details not ready")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        client.launchBillingFlow(activity as Activity, billingFlowParams)
    }

    override suspend fun verifyPurchaseWithBackend(
        purchaseToken: String
    ): Result<SubscriptionStatusModel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val idToken = authRepository.getIdToken().getOrThrow()
            val payload = JSONObject().apply {
                put("purchaseToken", purchaseToken)
                put("packageName", "ua.turskyi.travelling")
            }

            val request = Request.Builder()
                .url("$backendBaseUrl/api/subscription/verify")
                .addHeader("Authorization", "Bearer $idToken")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body.string()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(bodyString).optString("error", "Verification failed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing verify response body", e)
                    "Verification failed: HTTP ${response.code}"
                }
                Result.failure(Exception(errorMsg))
            } else {
                val json = JSONObject(bodyString)
                val active = json.optBoolean("active", false)
                val expiryTimeMs = json.optLong("expiryTimeMs", 0L)
                val state = json.optString("state", "")

                Result.success(
                    SubscriptionStatusModel(
                        active = active,
                        expiryTimeMs = expiryTimeMs,
                        state = state,
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyPurchaseWithBackend failed", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchSubscriptionStatus(): Result<SubscriptionStatusModel> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val idToken = authRepository.getIdToken().getOrThrow()
                val request = Request.Builder()
                    .url("$backendBaseUrl/api/subscription/status")
                    .addHeader("Authorization", "Bearer $idToken")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val bodyString = response.body.string()

                if (!response.isSuccessful) {
                    Result.failure(Exception("Failed to fetch status: HTTP ${response.code}"))
                } else {
                    val json = JSONObject(bodyString)
                    Result.success(
                        SubscriptionStatusModel(
                            active = json.optBoolean("active", false),
                            expiryTimeMs = json.optLong("expiryTimeMs", 0L),
                            state = json.optString("state", ""),
                            hasBackup = json.optBoolean("hasBackup", false),
                            backupStatus = json.optString("backupStatus", "NONE"),
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchSubscriptionStatus failed", e)
                Result.failure(e)
            }
        }
}
