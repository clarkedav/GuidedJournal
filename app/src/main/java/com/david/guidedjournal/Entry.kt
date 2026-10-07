package com.david.guidedjournal

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entry - Data model representing a single journal entry.
 *
 * This class defines the information that is stored for each
 * journal response in the local Room database.
 *
 * @property id Auto-generated unique identifier
 * @property date Timestamp of when the entry was created
 * @property content The text written by the user
 * @property category The category this entry belongs to
 * @property promptText The actual prompt question the user responded to
 * @property promptTimeOfDay Time period of the prompt: Morning, Afternoon, or Night
 */
@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val content: String = "",
    val category: String = "",
    val promptText: String = "",
    val promptTimeOfDay: String = ""
)