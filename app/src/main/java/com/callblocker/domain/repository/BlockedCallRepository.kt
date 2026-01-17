package com.callblocker.domain.repository

import com.callblocker.domain.model.BlockedCall
import kotlinx.coroutines.flow.Flow

interface BlockedCallRepository {
    fun getAllBlockedCalls(): Flow<List<BlockedCall>>
    suspend fun addBlockedCall(blockedCall: BlockedCall)
    suspend fun deleteBlockedCall(id: Long)
    suspend fun clearAllBlockedCalls()
}
