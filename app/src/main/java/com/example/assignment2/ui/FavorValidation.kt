package com.example.assignment2.ui

import java.time.LocalDate

const val MAX_DESCRIPTION_LENGTH = 100

/** Pure validation helpers: return an error message, or null when the input is valid. */
object FavorValidation {
    fun description(text: String): String? {
        val trimmed = text.trim()
        return when {
            trimmed.isEmpty() -> "Describe the favor"
            trimmed.length < 3 -> "Description is too short (min 3 characters)"
            trimmed.length > MAX_DESCRIPTION_LENGTH ->
                "Description is too long (max $MAX_DESCRIPTION_LENGTH characters)"
            else -> null
        }
    }

    fun person(personId: Int?): String? =
        if (personId == null) "Choose who this favor is with" else null

    /** A due date cannot be earlier than the day the favor was created. */
    fun dueDate(due: LocalDate, createdOn: LocalDate): String? =
        if (due.isBefore(createdOn)) "Due date cannot be before $createdOn" else null
}
