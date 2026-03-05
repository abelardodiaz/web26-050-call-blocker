package com.redv6.callblocker.data.repository

import com.redv6.callblocker.data.local.dao.BlockedNumberDao
import com.redv6.callblocker.data.local.entity.BlockedNumberEntity
import com.redv6.callblocker.domain.model.BlockedNumber
import com.redv6.callblocker.domain.repository.BlockedNumberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockedNumberRepositoryImpl @Inject constructor(
    private val blockedNumberDao: BlockedNumberDao
) : BlockedNumberRepository {

    override fun getAllBlockedNumbers(): Flow<List<BlockedNumber>> {
        return blockedNumberDao.getAllBlockedNumbers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBlockedNumberByPhone(phoneNumber: String): BlockedNumber? {
        return blockedNumberDao.getByPhoneNumber(phoneNumber)?.toDomain()
    }

    override suspend fun addBlockedNumber(blockedNumber: BlockedNumber) {
        blockedNumberDao.insert(BlockedNumberEntity.fromDomain(blockedNumber))
    }

    override suspend fun deleteBlockedNumber(id: Long) {
        blockedNumberDao.deleteById(id)
    }

    override suspend fun isNumberBlocked(phoneNumber: String): Boolean {
        return blockedNumberDao.isNumberBlocked(phoneNumber)
    }
}
