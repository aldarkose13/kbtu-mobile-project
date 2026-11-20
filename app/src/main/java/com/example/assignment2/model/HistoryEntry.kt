package com.example.assignment2.model

import java.time.LocalDateTime

enum class HistoryType(val label: String) {
    CREATED("Created"),
    EDITED("Edited"),
    REPAID("Marked repaid"),
    REOPENED("Repayment undone"),
    DELETED("Moved to trash"),
    RESTORED("Restored"),
    PURGED("Deleted permanently")
}

/** One logged action. The description and person name are snapshots, so the entry outlives the favor. */
data class HistoryEntry(
    val id: Int,
    val favorId: Int?,
    val description: String,
    val personName: String,
    val type: HistoryType,
    val timestamp: LocalDateTime
)
