package com.redv6.callblocker.domain.repository

import com.redv6.callblocker.domain.model.BlockedCall
import kotlinx.coroutines.flow.Flow

interface BlockedCallRepository {
    fun getAllBlockedCalls(): Flow<List<BlockedCall>>
    suspend fun addBlockedCall(blockedCall: BlockedCall): Long
    suspend fun updateSimSlot(id: Long, simSlot: Int)
    suspend fun deleteBlockedCall(id: Long)
    suspend fun clearAllBlockedCalls()
}
