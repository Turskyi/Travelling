package ua.turskyi.data.entities.local

import androidx.room.*
import ua.turskyi.data.entities.local.CountryEntity.Companion.COLUMN_NAME
import ua.turskyi.data.entities.local.CountryEntity.Companion.TABLE_COUNTRIES

@Entity(tableName = TABLE_COUNTRIES, indices = [Index(value = [COLUMN_NAME], unique = true)])
data class CountryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Int,
    @ColumnInfo(name = COLUMN_NAME) var name: String,
    @ColumnInfo(name = COLUMN_FLAG) var flag: String,
    @ColumnInfo(name = COLUMN_VISITED) var isVisited: Boolean?,
    @ColumnInfo(name = COLUMN_SELFIE) var selfie: String?
) {
    // required empty constructor for firestore serialization
    @Ignore
    constructor() : this(0, "", "", null, null)
    @Ignore
    constructor(id: Int, name: String, flag: String, isVisited: Boolean?) : this(
        id,
        name,
        flag,
        isVisited,
        null
    )

    companion object {
        const val TABLE_COUNTRIES = "Countries"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_FLAG = "flag"
        const val COLUMN_VISITED = "visited"
        const val COLUMN_SELFIE = "selfie"
    }
}
