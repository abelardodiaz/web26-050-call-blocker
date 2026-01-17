package com.callblocker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callblocker.domain.model.BlockedNumber

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val label: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): BlockedNumber = BlockedNumber(
        id = id,
        phoneNumber = phoneNumber,
        label = label,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: BlockedNumber): BlockedNumberEntity = BlockedNumberEntity(
            id = domain.id,
            phoneNumber = domain.phoneNumber,
            label = domain.label,
            createdAt = domain.createdAt
        )
    }
}
