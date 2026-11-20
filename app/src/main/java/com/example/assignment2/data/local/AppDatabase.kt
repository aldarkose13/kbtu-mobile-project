package com.example.assignment2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PersonEntity::class, FavorEntity::class, HistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favorDao(): FavorDao
    abstract fun personDao(): PersonDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "favors.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

/** v1 -> v2: adds the activity history table. Existing favors and people are untouched. */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `history` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`favorId` INTEGER, " +
                "`description` TEXT NOT NULL, " +
                "`personName` TEXT NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`timestamp` TEXT NOT NULL)"
        )
    }
}
