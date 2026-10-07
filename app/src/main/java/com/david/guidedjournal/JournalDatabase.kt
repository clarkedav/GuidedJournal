package com.david.guidedjournal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * JournalDatabase - The main Room database for the app.
 *
 * This class defines the database configuration and provides access
 * to the JournalDao, which is responsible for performing database
 * operations on journal entries and prompts.
 *
 * Version 2 adds promptText and promptTimeOfDay fields to entries.
 */
@Database(entities = [Entry::class, Prompt::class], version = 2, exportSchema = false)
abstract class JournalDatabase : RoomDatabase() {

    /**
     * Provides access to the JournalDao.
     *
     * The DAO contains the functions used to insert, retrieve,
     * update, and delete journal entries and prompts.
     *
     * Room generates the implementation of this function automatically.
     *
     * @return The JournalDao used to perform database operations.
     */
    abstract fun journalDao(): JournalDao

    companion object {

        /**
         * Stores the single instance of the database.
         *
         * @Volatile ensures that changes to INSTANCE are immediately
         * visible to all threads. This is important because the
         * database may be accessed from multiple threads.
         */
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        /**
         * Migration from database version 1 to version 2.
         *
         * A migration is required when the structure of the database
         * changes after the app has already been installed.
         *
         * This migration adds two new columns to the entries table:
         * - promptText: Stores the prompt associated with an entry.
         * - promptTimeOfDay: Stores the time period associated with
         *   the prompt, such as Morning, Afternoon, or Night.
         *
         * Existing entries receive an empty string as the default
         * value for both new columns.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            /**
             * Performs the actual database changes required to move
             * the database from version 1 to version 2.
             *
             * @param db The SQLite database being migrated.
             */
            override fun migrate(db: SupportSQLiteDatabase) {

                // Add the promptText column to existing journal entries.
                db.execSQL(
                    "ALTER TABLE entries ADD COLUMN promptText TEXT NOT NULL DEFAULT ''"
                )

                // Add the promptTimeOfDay column to existing journal entries.
                db.execSQL(
                    "ALTER TABLE entries ADD COLUMN promptTimeOfDay TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        /**
         * Returns the singleton database instance.
         *
         * If the database has already been created, the existing
         * instance is returned. Otherwise, a new database instance
         * is created and stored in INSTANCE.
         *
         * Using a singleton prevents the application from creating
         * multiple Room database instances, which can waste resources
         * and cause database-related problems.
         *
         * synchronized(this) ensures that only one thread can create
         * the database instance at a time.
         *
         * @param context The application context used to create the database.
         * @return The shared JournalDatabase instance.
         */
        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {

                // Build the Room database using the application context.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "journal_database"
                )

                    // Apply the migration when an existing version 1
                    // database is upgraded to version 2.
                    .addMigrations(MIGRATION_1_2)

                    // Create the database only when it is first needed.
                    .build()

                // Store the newly created database instance so that
                // future calls reuse the same instance.
                INSTANCE = instance

                // Return the newly created database instance.
                instance
            }
        }
    }
}