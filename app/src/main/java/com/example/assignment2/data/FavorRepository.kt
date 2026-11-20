package com.example.assignment2.data

import androidx.room.withTransaction
import com.example.assignment2.data.local.AppDatabase
import com.example.assignment2.data.local.FavorEntity
import com.example.assignment2.data.local.HistoryEntity
import com.example.assignment2.data.local.PersonEntity
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import com.example.assignment2.model.Favor
import com.example.assignment2.model.HistoryEntry
import com.example.assignment2.model.HistoryType
import com.example.assignment2.model.InterestPolicy
import com.example.assignment2.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val END_OF_DAY: LocalTime = LocalTime.of(23, 59)

/**
 * Single access point to stored data; the ViewModel never touches the DAOs directly.
 * Every change to a favor is written together with a history entry in one transaction.
 */
class FavorRepository(
    private val db: AppDatabase,
    private val policy: InterestPolicy = InterestPolicy()
) {
    private val favorDao = db.favorDao()
    private val personDao = db.personDao()
    private val historyDao = db.historyDao()

    val favors: Flow<List<Favor>> = favorDao.observeAll().map { list -> list.map { it.toModel() } }
    val deletedFavors: Flow<List<Favor>> =
        favorDao.observeDeleted().map { list -> list.map { it.toModel() } }
    val people: Flow<List<Person>> = personDao.observeAll().map { list -> list.map { it.toModel() } }
    val history: Flow<List<HistoryEntry>> =
        historyDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun addFavor(
        personId: Int,
        description: String,
        weight: Double,
        category: Category,
        direction: Direction,
        dueDate: LocalDate,
        now: LocalDateTime = LocalDateTime.now()
    ) = db.withTransaction {
        val id = favorDao.insert(
            FavorEntity(
                personId = personId,
                description = description,
                weight = weight,
                category = category,
                direction = direction,
                createdAt = now,
                dueAt = dueDate.atTime(END_OF_DAY)
            )
        ).toInt()
        log(HistoryType.CREATED, id, description, personId, now)
    }

    suspend fun updateFavor(
        id: Int,
        personId: Int,
        description: String,
        weight: Double,
        category: Category,
        direction: Direction,
        dueDate: LocalDate
    ) = db.withTransaction {
        val entity = favorDao.getById(id) ?: return@withTransaction
        favorDao.update(
            entity.copy(
                personId = personId,
                description = description,
                weight = weight,
                category = category,
                direction = direction,
                // keep the exact original time when the day was not changed
                dueAt = if (entity.dueAt.toLocalDate() == dueDate) entity.dueAt
                else dueDate.atTime(END_OF_DAY)
            )
        )
        log(HistoryType.EDITED, id, description, personId)
    }

    suspend fun setRepaid(
        id: Int,
        repaid: Boolean,
        now: LocalDateTime = LocalDateTime.now()
    ) = db.withTransaction {
        val entity = favorDao.getById(id) ?: return@withTransaction
        favorDao.update(entity.copy(repaidAt = if (repaid) now else null))
        log(
            if (repaid) HistoryType.REPAID else HistoryType.REOPENED,
            id, entity.description, entity.personId, now
        )
    }

    /** Soft delete: the favor moves to "Recently deleted" and can be restored. */
    suspend fun deleteFavor(id: Int) = db.withTransaction {
        val entity = favorDao.getById(id) ?: return@withTransaction
        val now = LocalDateTime.now()
        favorDao.update(entity.copy(deletedAt = now))
        log(HistoryType.DELETED, id, entity.description, entity.personId, now)
    }

    suspend fun restoreFavor(id: Int) = db.withTransaction {
        val entity = favorDao.getById(id) ?: return@withTransaction
        favorDao.update(entity.copy(deletedAt = null))
        log(HistoryType.RESTORED, id, entity.description, entity.personId)
    }

    /** Removes a favor for good (only offered from "Recently deleted"). */
    suspend fun purgeFavor(id: Int) = db.withTransaction {
        val entity = favorDao.getById(id) ?: return@withTransaction
        favorDao.delete(entity)
        log(HistoryType.PURGED, id, entity.description, entity.personId)
    }

    suspend fun clearHistory() = historyDao.clear()

    suspend fun addPerson(username: String) {
        personDao.insert(PersonEntity(username = username))
    }

    suspend fun renamePerson(id: Int, username: String) {
        val entity = personDao.getById(id) ?: return
        personDao.update(entity.copy(username = username))
    }

    /** Also removes that person's favors (foreign key cascade); those removals are logged. */
    suspend fun deletePerson(id: Int) = db.withTransaction {
        val person = personDao.getById(id) ?: return@withTransaction
        favorDao.getByPerson(id).forEach {
            log(HistoryType.PURGED, it.id, it.description, id)
        }
        personDao.delete(person)
    }

    fun interestFor(favor: Favor, now: LocalDateTime = LocalDateTime.now()): Double =
        policy.interest(favor, now)

    /** Positive = they owe me, negative = I owe them. Repaid favors do not count. */
    fun balanceWith(
        favors: List<Favor>,
        personId: Int,
        now: LocalDateTime = LocalDateTime.now()
    ): Double = favors
        .filter { it.personId == personId && it.repaidAt == null }
        .sumOf {
            val amount = it.weight + policy.interest(it, now)
            if (it.direction == Direction.IN_FAVOR) amount else -amount
        }

    private suspend fun log(
        type: HistoryType,
        favorId: Int,
        description: String,
        personId: Int,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val personName = personDao.getById(personId)?.username ?: "?"
        historyDao.insert(
            HistoryEntity(
                favorId = favorId,
                description = description,
                personName = personName,
                type = type,
                timestamp = now
            )
        )
    }
}
