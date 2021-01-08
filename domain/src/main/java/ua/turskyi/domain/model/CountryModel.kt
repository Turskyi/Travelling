package ua.turskyi.domain.model

data class CountryModel(
    var id: Int,
    val name: String,
    val flag: String,
    @field:JvmField
    var isVisited: Boolean?,
    var selfie: String?
) {
    /* required empty constructor for firestore serialization */
    constructor() : this(0, "", "", null, "")
    constructor(id: Int, name: String, flag: String) : this(id, name, flag, null, null)
}
