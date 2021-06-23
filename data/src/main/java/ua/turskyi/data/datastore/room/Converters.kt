package ua.turskyi.data.datastore.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ua.turskyi.data.entities.room.CityEntity

class Converters {
    @TypeConverter
    fun fromStringToEntities(value: String?): List<CityEntity>? {
        return if (value != null) {
            val listType = object : TypeToken<List<CityEntity>>() {}.type
            Gson().fromJson(value, listType)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromEntitiesToString(list: List<CityEntity>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}