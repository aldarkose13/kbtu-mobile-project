package com.example.assignment2.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import java.time.LocalDateTime

@Entity(
    tableName = "favors",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class FavorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val description: String,
    val weight: Double,
    val category: Category,
    val direction: Direction,
    val createdAt: LocalDateTime,
    val dueAt: LocalDateTime,
    val repaidAt: LocalDateTime? = null,
    /** Soft-delete marker, used later by the history/restore feature. Null = active. */
    val deletedAt: LocalDateTime? = null
)
