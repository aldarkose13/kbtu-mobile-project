package com.example.assignment2.data

import com.example.assignment2.data.local.FavorEntity
import com.example.assignment2.data.local.HistoryEntity
import com.example.assignment2.data.local.PersonEntity
import com.example.assignment2.model.Favor
import com.example.assignment2.model.HistoryEntry
import com.example.assignment2.model.Person

fun FavorEntity.toModel() = Favor(
    id = id,
    personId = personId,
    description = description,
    weight = weight,
    category = category,
    direction = direction,
    createdAt = createdAt,
    dueAt = dueAt,
    repaidAt = repaidAt
)

fun PersonEntity.toModel() = Person(id = id, username = username)

fun HistoryEntity.toModel() = HistoryEntry(
    id = id,
    favorId = favorId,
    description = description,
    personName = personName,
    type = type,
    timestamp = timestamp
)
