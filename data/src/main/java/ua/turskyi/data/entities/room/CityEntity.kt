package ua.turskyi.data.entities.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ua.turskyi.data.entities.room.CityEntity.Companion.COLUMN_ID
import ua.turskyi.data.entities.room.CityEntity.Companion.TABLE_CITIES

@Entity(tableName = TABLE_CITIES , indices = [Index(value = [COLUMN_ID], unique = true)])
data class CityEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Int,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
    @ColumnInfo(name = COLUMN_PARENT_ID)  val parentId: Int
) {
    companion object {
        const val TABLE_CITIES = "Countries"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PARENT_ID = "parentId"
    }
}
