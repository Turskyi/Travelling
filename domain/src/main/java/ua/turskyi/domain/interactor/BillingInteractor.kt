package ua.turskyi.domain.interactor

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.turskyi.domain.model.SubscriptionStatusModel
import ua.turskyi.domain.repository.BillingRepository

@Suppress("unused")
class BillingInteractor : KoinComponent {
    private val billingRepository: BillingRepository by inject()

    fun startConnection(
        onReady: () -> Unit,
        onError: (String) -> Unit,
        onPurchaseVerified: (SubscriptionStatusModel) -> Unit
    ) = billingRepository.startConnection(onReady, onError, onPurchaseVerified)

    suspend fun querySubscriptionDetails() = billingRepository.querySubscriptionDetails()

    fun launchBillingFlow(activity: Any, onError: (String) -> Unit) =
        billingRepository.launchBillingFlow(activity, onError)

    suspend fun verifyPurchaseWithBackend(purchaseToken: String) =
        billingRepository.verifyPurchaseWithBackend(purchaseToken)

    suspend fun fetchSubscriptionStatus() = billingRepository.fetchSubscriptionStatus()
}
