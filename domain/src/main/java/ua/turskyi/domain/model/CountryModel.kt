package ua.turskyi.domain.model

data class CountryModel(
    var id: Int,
    val name: String,
    val flag: String,
    @field:JvmField
    var isVisited: Boolean,
    var selfie: String
) {
    constructor(name: String, flag: String) : this(
        id = 0,
        name = name,
        flag = flag,
        isVisited = false,
        selfie = "",
    )
}
