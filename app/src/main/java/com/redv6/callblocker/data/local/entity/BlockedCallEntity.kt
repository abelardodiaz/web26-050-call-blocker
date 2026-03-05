package com.redv6.callblocker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redv6.callblocker.domain.model.BlockedCall
import com.redv6.callblocker.domain.model.BlockReason

@Entity(tableName = "blocked_calls")
data class BlockedCallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = BlockReason.BLOCK_LIST.name,
    val simSlot: Int? = null // 0 = SIM 1, 1 = SIM 2, null = desconocido
) {
    fun toDomain(): BlockedCall = BlockedCall(
        id = id,
        phoneNumber = phoneNumber,
        timestamp = timestamp,
        reason = BlockReason.valueOf(reason),
        simSlot = simSlot
    )

    companion object {
        fun fromDomain(domain: BlockedCall): BlockedCallEntity = BlockedCallEntity(
            id = domain.id,
            phoneNumber = domain.phoneNumber,
            timestamp = domain.timestamp,
            reason = domain.reason.name,
            simSlot = domain.simSlot
        )
    }
}
