package ua.turskyi.data.entities.network


import com.google.gson.annotations.SerializedName

typealias CountriesResponse = ArrayList<CountryResponse>
data class CountryResponse(
    @SerializedName("alpha2Code")
    val alpha2Code: String, // ZW
    @SerializedName("alpha3Code")
    val alpha3Code: String, // ZWE
    @SerializedName("altSpellings")
    val altSpellings: List<String>,
    @SerializedName("area")
    val area: Double, // 652230.0
    @SerializedName("borders")
    val borders: List<String>,
    @SerializedName("callingCodes")
    val callingCodes: List<String>,
    @SerializedName("capital")
    val capital: String, // Harare
    @SerializedName("cioc")
    val cioc: String, // ZIM
    @SerializedName("currencies")
    val currencies: List<CurrencyResponse>,
    @SerializedName("demonym")
    val demonym: String, // Zimbabwean
    @SerializedName("flag")
    val flag: String, // https://flagcdn.com/zw.svg
    @SerializedName("flags")
    val flags: FlagsResponse,
    @SerializedName("gini")
    val gini: Double, // 33.2
    @SerializedName("independent")
    val independent: Boolean, // true
    @SerializedName("languages")
    val languages: List<LanguageResponse>,
    @SerializedName("latlng")
    val latlng: List<Double>,
    @SerializedName("name")
    val name: String, // Zimbabwe
    @SerializedName("nativeName")
    val nativeName: String, // Zimbabwe
    @SerializedName("numericCode")
    val numericCode: String, // 716
    @SerializedName("population")
    val population: Int, // 14862927
    @SerializedName("region")
    val region: String, // Africa
    @SerializedName("regionalBlocs")
    val regionalBlocs: List<RegionalBlocResponse>,
    @SerializedName("subregion")
    val subregion: String, // Eastern Africa
    @SerializedName("timezones")
    val timezones: List<String>,
    @SerializedName("topLevelDomain")
    val topLevelDomain: List<String>,
    @SerializedName("translations")
    val translations: TranslationsResponse
)