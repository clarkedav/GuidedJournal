package com.david.guidedjournal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * JournalDao - Defines all database operations for entries and prompts.
 */
@Dao
interface JournalDao {

    /**
     * Inserts a new journal entry into the database.
     * @param entry The Entry object to insert
     */
    @Insert
    suspend fun insertEntry(entry: Entry)

    /**
     * Retrieves all journal entries ordered by newest first.
     * @return List of all Entry objects
     */
    @Query("SELECT * FROM entries ORDER BY date DESC")
    suspend fun getAllEntries(): List<Entry>

    /**
     * Updates an existing journal entry.
     * @param entry The Entry object with updated values
     */
    @Update
    suspend fun updateEntry(entry: Entry)

    /**
     * Deletes a journal entry from the database.
     * @param entry The Entry object to delete
     */
    @Delete
    suspend fun deleteEntry(entry: Entry)

    /**
     * Inserts a new prompt into the database.
     * @param prompt The Prompt object to insert
     */
    @Insert
    suspend fun insertPrompt(prompt: Prompt)

    /**
     * Retrieves a random prompt for a specific time of day.
     * @param timeOfDay Morning, Afternoon, or Night
     * @return A random Prompt for that time period
     */
    @Query("SELECT * FROM prompts WHERE timeOfDay = :timeOfDay ORDER BY RANDOM() LIMIT 1")
    suspend fun getPromptByTimeOfDay(timeOfDay: String): Prompt?

    /**
     * Retrieves multiple prompts for a specific time of day.
     * Used when the user requests more prompts.
     * @param timeOfDay Morning, Afternoon, or Night
     * @param limit How many prompts to return
     * @return List of random prompts for that time period
     */
    @Query("SELECT * FROM prompts WHERE timeOfDay = :timeOfDay ORDER BY RANDOM() LIMIT :limit")
    suspend fun getPromptsByTimeOfDay(
        timeOfDay: String,
        limit: Int
    ): List<Prompt>

    /**
     * Retrieves all mandatory prompts from the database.
     * Used to select the required prompts for the daily journal.
     * @return List of all mandatory Prompt objects
     */
    @Query("SELECT * FROM prompts WHERE isMandatory = 1")
    suspend fun getAllMandatoryPrompts(): List<Prompt>

    /**
     * Retrieves all prompts from the database.
     * Used when displaying the complete prompt library for browsing.
     * @return List of all Prompt objects
     */
    @Query("SELECT * FROM prompts")
    suspend fun getAllPrompts(): List<Prompt>

    /**
     * Counts the total number of prompts stored in the database.
     * Used to determine whether the prompt database needs to be initialized.
     * @return Total number of prompts
     */
    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getPromptCount(): Int
}
