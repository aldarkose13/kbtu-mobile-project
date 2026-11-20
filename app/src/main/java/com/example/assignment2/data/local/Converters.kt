package com.example.assignment2.data.local

import androidx.room.TypeConverter
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import com.example.assignment2.model.HistoryType
import java.time.LocalDateTime

/** Teaches Room how to store the types it doesn't support natively. */
class Converters {
    @TypeConverter
    fun fromDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun fromCategory(value: Category): String = value.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)

    @TypeConverter
    fun fromDirection(value: Direction): String = value.name

    @TypeConverter
    fun toDirection(value: String): Direction = Direction.valueOf(value)

    @TypeConverter
    fun fromHistoryType(value: HistoryType): String = value.name

    @TypeConverter
    fun toHistoryType(value: String): HistoryType = HistoryType.valueOf(value)
}
