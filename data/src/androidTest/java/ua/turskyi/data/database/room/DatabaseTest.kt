package ua.turskyi.data.database.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ua.turskyi.data.database.room.dao.CountriesDao
import ua.turskyi.data.entities.local.CountryEntity

@RunWith(AndroidJUnit4::class)
class DatabaseTest : TestCase() {
    private lateinit var db: Database
    private lateinit var dao: CountriesDao

    @Before
    public override fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
        dao = db.countriesDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadCountry() = runBlocking {
        val country = CountryEntity(
            name = "Afghanistan",
            flag = "https://restcountries.eu/data/afg.svg",
            isVisited = false
        )

        dao.insertCountry(country)

        val countries: List<CountryEntity> = dao.getCountriesByRange(1, 0)


        assertEquals(1, countries.size)
    }
}