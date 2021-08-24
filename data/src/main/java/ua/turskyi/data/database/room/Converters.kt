package ua.turskyi.data.database.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ua.turskyi.data.entities.local.CityEntity
import java.lang.reflect.Type

class Converters {
    @TypeConverter
    fun fromStringToEntities(value: String?): List<CityEntity>? {
        return if (value != null) {
            val listType: Type = object : TypeToken<List<CityEntity>>() {}.type
            Gson().fromJson(value, listType)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromEntitiesToString(list: List<CityEntity>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}