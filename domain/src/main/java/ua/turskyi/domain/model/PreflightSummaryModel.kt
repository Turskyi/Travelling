package ua.turskyi.domain.model

data class PreflightSummaryModel(
    val countryCount: Int,
    val cityCount: Int,
    val availableSelfieCount: Int,
    val missingSelfieCount: Int,
    val missingSelfiesByCountry: Map<String, String>, // countryName -> countryFlag or info
)
