package ua.turskyi.data.entities.local

import androidx.room.*
import ua.turskyi.data.entities.local.CountryEntity.Companion.COLLECTION_COUNTRIES
import ua.turskyi.data.entities.local.CountryEntity.Companion.PARAM_NAME

@Entity(tableName = COLLECTION_COUNTRIES, indices = [Index(value = [PARAM_NAME], unique = true)])
data class CountryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = PARAM_ID) var id: Int = 0,
    @ColumnInfo(name = PARAM_NAME) var name: String,
    @ColumnInfo(name = PARAM_FLAG) var flag: String,
    @ColumnInfo(name = PARAM_VISITED) var isVisited: Boolean,
    @ColumnInfo(name = PARAM_SELFIE) var selfie: String
) {
    @Ignore
    constructor() : this(0, "", "", false, "")

    @Ignore
    constructor(id: Int, name: String, flag: String, isVisited: Boolean) : this(
        id,
        name,
        flag,
        isVisited,
        ""
    )

    @Ignore
    constructor(name: String, flag: String, isVisited: Boolean) : this(
        0,
        name,
        flag,
        isVisited,
        ""
    )

    companion object {
        const val COLLECTION_COUNTRIES = "Countries"
        const val PARAM_ID = "id"
        const val PARAM_NAME = "name"
        const val PARAM_FLAG = "flag"
        const val PARAM_VISITED = "visited"
        const val PARAM_SELFIE = "selfie"
    }
}
