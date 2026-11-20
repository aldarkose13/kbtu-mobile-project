package com.example.assignment2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.assignment2.model.HistoryType
import java.time.LocalDateTime

/** No foreign key on purpose: history must survive when a favor or person is removed. */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val favorId: Int?,
    val description: String,
    val personName: String,
    val type: HistoryType,
    val timestamp: LocalDateTime
)
