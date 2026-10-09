package com.callblocker.data.repository

import com.callblocker.core.util.PhoneNumberNormalizer
import com.callblocker.data.local.dao.BlockedNumberDao
import com.callblocker.data.local.entity.BlockedNumberEntity
import com.callblocker.domain.model.BlockedNumber
import com.callblocker.domain.repository.BlockedNumberRepository
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
        return blockedNumberDao.getByPhoneNumber(PhoneNumberNormalizer.normalize(phoneNumber))?.toDomain()
    }

    override suspend fun addBlockedNumber(blockedNumber: BlockedNumber) {
        val normalized = blockedNumber.copy(phoneNumber = PhoneNumberNormalizer.normalize(blockedNumber.phoneNumber))
        blockedNumberDao.insert(BlockedNumberEntity.fromDomain(normalized))
    }

    override suspend fun deleteBlockedNumber(id: Long) {
        blockedNumberDao.deleteById(id)
    }

    override suspend fun isNumberBlocked(phoneNumber: String): Boolean {
        return blockedNumberDao.isNumberBlocked(phoneNumber)
    }
}
