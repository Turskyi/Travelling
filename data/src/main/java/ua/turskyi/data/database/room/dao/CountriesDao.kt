package ua.turskyi.data.database.room.dao

import androidx.room.*
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CityEntity.Companion.PARAM_PARENT_ID
import ua.turskyi.data.entities.local.CityEntity.Companion.COLLECTION_CITIES
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.data.entities.local.CountryEntity.Companion.PARAM_ID
import ua.turskyi.data.entities.local.CountryEntity.Companion.PARAM_NAME
import ua.turskyi.data.entities.local.CountryEntity.Companion.PARAM_SELFIE
import ua.turskyi.data.entities.local.CountryEntity.Companion.COLLECTION_COUNTRIES

@Dao
interface CountriesDao {

    @Query("SELECT * FROM  $COLLECTION_COUNTRIES WHERE $PARAM_NAME LIKE :name LIMIT :limit OFFSET :offset")
    fun loadAllCountriesByNameAndRange(
        name: String,
        limit: Int,
        offset: Int
    ): MutableList<CountryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllCountries(countries: List<CountryEntity>)

    // using in paging adapters
    @Query("SELECT * FROM $COLLECTION_COUNTRIES LIMIT :limit OFFSET :offset")
    fun getCountriesByRange(limit: Int, offset: Int): List<CountryEntity>

    @Query("SELECT COUNT($PARAM_ID) FROM $COLLECTION_COUNTRIES WHERE ${CountryEntity.PARAM_VISITED} IS null OR ${CountryEntity.PARAM_VISITED} = 0")
    fun getNumNotVisitedCountries(): Int

    @Query("SELECT * FROM $COLLECTION_COUNTRIES WHERE ${CountryEntity.PARAM_VISITED} = 1")
    fun getVisitedCountries(): List<CountryEntity>

    @Query("SELECT * FROM $COLLECTION_CITIES")
    fun getCities(): List<CityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: CountryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: CityEntity)

    @Delete
    fun delete(city: CityEntity)

    @Query("DELETE FROM $COLLECTION_CITIES WHERE $PARAM_PARENT_ID = :parentId")
    fun removeCitiesByCountry(parentId: Int)

    @Query("UPDATE $COLLECTION_COUNTRIES SET $PARAM_SELFIE = :selfie WHERE $PARAM_ID = :id")
    fun updateSelfie(id: Int, selfie: String)
}