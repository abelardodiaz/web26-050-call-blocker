package com.redv6.callblocker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redv6.callblocker.data.local.entity.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY createdAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getByPhoneNumber(phoneNumber: String): BlockedNumberEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE (is_prefix = 0 AND phoneNumber = :phoneNumber) OR (is_prefix = 1 AND :phoneNumber LIKE phoneNumber || '%'))")
    suspend fun isNumberBlocked(phoneNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
