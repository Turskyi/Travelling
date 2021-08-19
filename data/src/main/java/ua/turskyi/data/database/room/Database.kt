package ua.turskyi.data.database.room

import android.content.Context
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.turskyi.data.BuildConfig
import ua.turskyi.data.R
import ua.turskyi.data.entities.local.CityEntity
import ua.turskyi.data.entities.local.CountryEntity
import ua.turskyi.data.database.room.dao.CountriesDao
import androidx.room.Database as DB

@DB(
    entities = [
        CountryEntity::class,
        CityEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
            instance ?: buildDatabase(context).also { database -> instance = database }
        }

        private fun buildDatabase(context: Context) = databaseBuilder(
            context,
            Database::class.java,
            BuildConfig.DATABASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.query(context.resources.getString(R.string.pragma_journal_mode_memory))
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.query(context.resources.getString(R.string.pragma_synchronous_off))
            }
        }).addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.query(context.resources.getString(R.string.pragma_journal_mode_memory))
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.query(context.resources.getString(R.string.pragma_synchronous_off))
            }
        }).build()
    }
}
