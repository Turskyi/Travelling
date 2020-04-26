package ua.turskyi.data.room.dao

import androidx.paging.DataSource
import androidx.room.*
import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CityEntity.Companion.TABLE_CITIES
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.data.entities.room.CountryEntity.Companion.COLUMN_NAME
import ua.turskyi.data.entities.room.CountryEntity.Companion.TABLE_NAME

@Dao
abstract class CountriesDao {

    @Query("SELECT * FROM  $TABLE_NAME")
    abstract fun loadAllSearchedCountries(): DataSource.Factory<Int, CountryEntity>

    @Query("SELECT * FROM  $TABLE_NAME where $COLUMN_NAME LIKE :name")
    abstract fun loadAllCountriesByName(name: String): DataSource.Factory<Int, CountryEntity>

    @Query("SELECT * FROM  $TABLE_NAME where $COLUMN_NAME LIKE :name LIMIT :limit OFFSET :offset")
    abstract fun loadAllCountriesByNameAndRange(name: String?, limit: Int, offset: Int): MutableList<CountryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertAllCountries(countries: List<CountryEntity>?)

    /* using in paging adapters */
    @Query("SELECT * FROM $TABLE_NAME LIMIT :limit OFFSET :offset")
    abstract fun getCountriesByRange(limit: Int, offset: Int): List<CountryEntity>

    @Query("SELECT COUNT(${CountryEntity.COLUMN_ID}) FROM $TABLE_NAME WHERE ${CountryEntity.COLUMN_VISITED} IS null OR ${CountryEntity.COLUMN_VISITED} = 0")
   abstract fun getNumNotVisitedCountries(): Int

    @Query("SELECT * FROM $TABLE_NAME WHERE ${CountryEntity.COLUMN_VISITED} = 1")
   abstract fun getVisitedCountries(): List<CountryEntity>

    @Query("SELECT * FROM $TABLE_CITIES")
    abstract fun getCities(): MutableList<CityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCountry(country: CountryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun  insertCity(city: CityEntity)

    @Delete
    abstract fun delete(city: CityEntity)
}