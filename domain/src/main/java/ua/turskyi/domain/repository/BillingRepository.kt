package ua.turskyi.domain.repository

import ua.turskyi.domain.model.SubscriptionStatusModel

interface BillingRepository {
    fun startConnection(
        onReady: () -> Unit,
        onError: (String) -> Unit,
        onPurchaseVerified: (SubscriptionStatusModel) -> Unit
    )
    suspend fun querySubscriptionDetails(): Result<Pair<String, String>>
    fun launchBillingFlow(activity: Any, onError: (String) -> Unit)
    suspend fun verifyPurchaseWithBackend(purchaseToken: String): Result<SubscriptionStatusModel>
    suspend fun fetchSubscriptionStatus(): Result<SubscriptionStatusModel>
}
