package ua.turskyi.data.datastore.room.dao

import androidx.room.*
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CityEntity.Companion.COLUMN_PARENT_ID
import ua.turskyi.data.entities.local.CityEntity.Companion.TABLE_CITIES
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.data.entities.local.CountryEntity.Companion.COLUMN_ID
import ua.turskyi.data.entities.local.CountryEntity.Companion.COLUMN_NAME
import ua.turskyi.data.entities.local.CountryEntity.Companion.COLUMN_SELFIE
import ua.turskyi.data.entities.local.CountryEntity.Companion.TABLE_COUNTRIES

@Dao
interface CountriesDao {

    @Query("SELECT * FROM  $TABLE_COUNTRIES WHERE $COLUMN_NAME LIKE :name LIMIT :limit OFFSET :offset")
    fun loadAllCountriesByNameAndRange(
        name: String?,
        limit: Int,
        offset: Int
    ): MutableList<CountryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllCountries(countries: List<CountryEntity>?)

    /* using in paging adapters */
    @Query("SELECT * FROM $TABLE_COUNTRIES LIMIT :limit OFFSET :offset")
    fun getCountriesByRange(limit: Int, offset: Int): List<CountryEntity>

    @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_COUNTRIES WHERE ${CountryEntity.COLUMN_VISITED} IS null OR ${CountryEntity.COLUMN_VISITED} = 0")
    fun getNumNotVisitedCountries(): Int

    @Query("SELECT * FROM $TABLE_COUNTRIES WHERE ${CountryEntity.COLUMN_VISITED} = 1")
    fun getVisitedCountries(): List<CountryEntity>

    @Query("SELECT * FROM $TABLE_CITIES")
    fun getCities(): List<CityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountry(country: CountryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: CityEntity)

    @Delete
    fun delete(city: CityEntity)

    @Query("DELETE FROM $TABLE_CITIES WHERE $COLUMN_PARENT_ID = :parentId")
    fun removeCitiesByCountry(parentId: Int)

    @Query("UPDATE $TABLE_COUNTRIES SET $COLUMN_SELFIE = :selfie WHERE $COLUMN_ID = :id")
    fun updateSelfie(id: Int, selfie: String)
}