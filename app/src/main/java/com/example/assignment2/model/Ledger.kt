package com.example.assignment2.model

import java.time.LocalDateTime
class Ledger(private val policy : InterestPolicy = InterestPolicy()) {
    private val favors = mutableMapOf<Int, Favor>()
    private var nextId = 1


    fun all(): List<Favor> = favors.values.toList()

    fun add(personId: Int, description: String, weight: Double,
            category: Category, direction: Direction, now: LocalDateTime = LocalDateTime.now()
    ) : Favor {
        val favor = Favor(
            nextId++, personId, description, weight, category, direction, now,
            now.plusDays(7)
        )
        favors[favor.id] = favor
        return favor
    }
    fun repay(id: Int, now: LocalDateTime = LocalDateTime.now()){ favors[id]?.let { favors[id] = it.repay(now) } }
    fun undo(id: Int){ favors[id]?.let{ favors[id] = it.undo() } }
    fun search(condition: (Favor) -> Boolean) = all().filter(condition)

    fun balanceWith(personId: Int, now: LocalDateTime = LocalDateTime.now()): Double =
        all().filter { it.personId == personId && it.repaidAt == null }
            .sumOf {
                val amount = it.weight + policy.interest(it, now)
                if (it.direction == Direction.IN_FAVOR) amount else -amount
            }
}