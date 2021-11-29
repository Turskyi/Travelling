package ua.turskyi.data.entities.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ua.turskyi.data.entities.local.CityEntity.Companion.PARAM_ID
import ua.turskyi.data.entities.local.CityEntity.Companion.COLLECTION_CITIES

@Entity(tableName = COLLECTION_CITIES, indices = [Index(value = [PARAM_ID], unique = true)])
data class CityEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = PARAM_ID) var id: Int,
    @ColumnInfo(name = PARAM_NAME) val name: String,
    @ColumnInfo(name = PARAM_PARENT_ID) val parentId: Int,
    @ColumnInfo(name = PARAM_MONTH) val month: String
) {
    companion object {
        const val COLLECTION_CITIES = "Cities"
        const val PARAM_ID = "id"
        const val PARAM_NAME = "name"
        const val PARAM_PARENT_ID = "parentId"
        const val PARAM_MONTH = "month"
    }
}
