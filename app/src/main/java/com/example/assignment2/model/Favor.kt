package com.example.assignment2.model

import java.time.LocalDateTime

data class Favor(
    val id: Int,
    val personId: Int,          // who it concerns (the other party)
    val description: String,
    val weight: Double,
    val category: Category,
    val direction: Direction,   // IN_FAVOR = they owe me, OUT_FAVOR = I owe them
    val createdAt: LocalDateTime,
    val dueAt: LocalDateTime,
    val repaidAt: LocalDateTime? = null
) : Owable<Favor> {

    fun statusAt(now: LocalDateTime): Status = when {
        repaidAt != null -> Status.REPAID
        now.isAfter(dueAt) -> Status.OVERDUE
        else -> Status.ACTIVE
    }
    override fun repay(now: LocalDateTime) = copy(repaidAt = now)
    override fun undo() = copy(repaidAt = null)
}

