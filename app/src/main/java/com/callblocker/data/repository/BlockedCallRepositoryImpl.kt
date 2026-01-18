package com.callblocker.data.repository

import com.callblocker.data.local.dao.BlockedCallDao
import com.callblocker.data.local.entity.BlockedCallEntity
import com.callblocker.domain.model.BlockedCall
import com.callblocker.domain.repository.BlockedCallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockedCallRepositoryImpl @Inject constructor(
    private val blockedCallDao: BlockedCallDao
) : BlockedCallRepository {

    override fun getAllBlockedCalls(): Flow<List<BlockedCall>> {
        return blockedCallDao.getAllBlockedCalls().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBlockedCall(blockedCall: BlockedCall): Long {
        return blockedCallDao.insert(BlockedCallEntity.fromDomain(blockedCall))
    }

    override suspend fun updateSimSlot(id: Long, simSlot: Int) {
        blockedCallDao.updateSimSlot(id, simSlot)
    }

    override suspend fun deleteBlockedCall(id: Long) {
        blockedCallDao.deleteById(id)
    }

    override suspend fun clearAllBlockedCalls() {
        blockedCallDao.clearAll()
    }
}
