package com.david.guidedjournal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * JournalDatabase - The main Room database for the app.
 * Version 2 adds promptText and promptTimeOfDay fields to entries.
 */
@Database(entities = [Entry::class, Prompt::class], version = 2, exportSchema = false)
abstract class JournalDatabase : RoomDatabase() {

    /**
     * Returns the DAO used to access database operations.
     */
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        /**
         * Migration from version 1 to 2.
         * Adds promptText and promptTimeOfDay columns to the entries table.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE entries ADD COLUMN promptText TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE entries ADD COLUMN promptTimeOfDay TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        /**
         * Returns the singleton database instance.
         * Creates it if it does not already exist.
         *
         * @param context The application context
         * @return The JournalDatabase instance
         */
        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "journal_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}