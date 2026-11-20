package ua.turskyi.domain.model

data class SubscriptionStatusModel(
    val active: Boolean,
    val expiryTimeMs: Long,
    val state: String = "",
    val hasBackup: Boolean = false,
    val backupStatus: String = "NONE",
)
