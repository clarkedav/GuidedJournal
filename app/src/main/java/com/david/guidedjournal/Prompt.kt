package com.david.guidedjournal

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Prompt - Data model for a daily journal prompt question.
 *
 * @property id Auto-generated unique identifier
 * @property text The prompt question shown to the user
 * @property category The topic category of this prompt
 * @property timeOfDay When this prompt is shown: Morning, Afternoon, or Night
 * @property isMandatory Whether this is one of the 10 required daily prompts
 */
@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String = "",
    val category: String = "",
    val timeOfDay: String = "",
    val isMandatory: Boolean = false
)