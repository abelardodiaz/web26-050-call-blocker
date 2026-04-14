package com.callblocker.domain.repository

import com.callblocker.domain.model.BlockedNumber
import kotlinx.coroutines.flow.Flow

interface BlockedNumberRepository {
    fun getAllBlockedNumbers(): Flow<List<BlockedNumber>>
    suspend fun getBlockedNumberByPhone(phoneNumber: String): BlockedNumber?
    suspend fun addBlockedNumber(blockedNumber: BlockedNumber)
    suspend fun deleteBlockedNumber(id: Long)
    suspend fun isNumberBlocked(phoneNumber: String): Boolean
}
