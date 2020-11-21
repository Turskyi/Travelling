package ua.turskyi.travelling.utils

import android.text.TextUtils
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.toast.toast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.common.Constants.SKU_ID
import ua.turskyi.travelling.extensions.toastLong
import ua.turskyi.travelling.features.home.view.ui.HomeActivity
import java.util.*

class BillingManager(private val activity: HomeActivity) : PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private val mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()

    init {
        initBilling()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            /*     we will get here after the purchase is made */
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            toast(R.string.toast_error_due_to_canceling_purchase)
        } else {
            toast(R.string.toast_error_unexpected)
        }
    }

    private fun initBilling() {
        billingClient =
            newBuilder(activity).enablePendingPurchases().setListener(this)
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    /* The BillingClient is ready. Query purchases here. */
                    /* here we can request information about purchases */

                    /* Sku request */
                    querySkuDetails()

                    /* purchase request */
                    val purchasesList = queryPurchases()
                    /* if the product has already been purchased, provide it to the user */
                    purchasesList?.size?.let {
                        for (i in 0 until it) {
                            val purchaseId = purchasesList[i]?.sku
                            if (TextUtils.equals(SKU_ID, purchaseId)) {
                                activity.setUpgradedVersion()
                            }
                        }
                    }
                } else {
                    toast(R.string.toast_connection_billing)
                }
            }

            override fun onBillingServiceDisconnected() {
                /* we get here if something goes wrong */
                /*   Try to restart the connection on the next request to
                    Google Play by calling the startConnection() method.*/
                toast(R.string.toast_internet_connection_lost)
            }

            private fun queryPurchases(): List<Purchase?>? {
                val purchasesResult: Purchase.PurchasesResult =
                    billingClient.queryPurchases(SkuType.INAPP)
                return purchasesResult.purchasesList
            }
        })
    }

    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        /* here we are adding the product id from the Play Console */
        skuList.add(SKU_ID)
        skuDetailsParamsBuilder.setSkusList(skuList).setType(SkuType.INAPP)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                for (skuDetails in purchases) {
                    mSkuDetailsMap[skuDetails.sku] = skuDetails
                }
            }
        }
    }

    fun launchBilling() {
        val billingFlowParams = mSkuDetailsMap[SKU_ID]?.let {
            BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
        }
        billingFlowParams?.let { flowParams ->
            billingClient.launchBillingFlow(
                activity,
                flowParams
            )

        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            /*  Grant the item to the user */
            activity.setUpgradedVersion()
            /* acknowledge the purchase */
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                activity.launch {
                    withContext(Dispatchers.IO) {
                        val acknowledgePurchaseResponseListener =
                            AcknowledgePurchaseResponseListener { toast(R.string.toast_purchase_acknowledged) }
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(),
                            acknowledgePurchaseResponseListener
                        )
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            /*      Here we can confirm to the user that they've started the pending
                  purchase, and to complete it, they should follow instructions that
                  are given to them. You can also choose to remind the user in the
                  future to complete the purchase if you detect that it is still
                  pending. */
            activity.toastLong(R.string.toast_complete_purchase)
        }
    }
}