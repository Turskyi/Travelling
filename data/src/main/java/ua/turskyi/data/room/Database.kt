package ua.turskyi.data.room

import android.content.Context
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.turskyi.data.BuildConfig
import ua.turskyi.data.entities.room.CityEntity
import ua.turskyi.data.entities.room.CountryEntity
import ua.turskyi.data.room.dao.CountriesDao
import androidx.room.Database as DB

@DB(
    entities = [
        CountryEntity::class,
        CityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {

    abstract fun countriesDao(): CountriesDao

    companion object {
        @Volatile
        private var instance: Database? = null
        private val LOCK = Any()

        /**
         * use in koin for mutations and if it is necessary
         * to perform actions on database create or open
         */
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = databaseBuilder(
            context,
            Database::class.java,
            BuildConfig.DATABASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.query("PRAGMA journal_mode = MEMORY")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.query("PRAGMA synchronous = OFF")
            }
        }).fallbackToDestructiveMigration()
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.query("PRAGMA journal_mode = MEMORY")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.query("PRAGMA synchronous = OFF")
                }
            }).build()
    }
}
