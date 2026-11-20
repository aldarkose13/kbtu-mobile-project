package com.example.assignment2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavorDao {
    @Query("SELECT * FROM favors WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavorEntity>>

    @Query("SELECT * FROM favors WHERE id = :id")
    fun observeById(id: Int): Flow<FavorEntity?>

    @Query("SELECT * FROM favors WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeleted(): Flow<List<FavorEntity>>

    @Query("SELECT * FROM favors WHERE id = :id")
    suspend fun getById(id: Int): FavorEntity?

    @Query("SELECT * FROM favors WHERE personId = :personId")
    suspend fun getByPerson(personId: Int): List<FavorEntity>

    @Insert
    suspend fun insert(favor: FavorEntity): Long

    @Update
    suspend fun update(favor: FavorEntity)

    @Delete
    suspend fun delete(favor: FavorEntity)
}
