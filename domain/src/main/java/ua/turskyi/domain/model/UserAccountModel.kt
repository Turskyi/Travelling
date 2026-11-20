package ua.turskyi.domain.model

data class UserAccountModel(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val keepSignedIn: Boolean = false,
)
