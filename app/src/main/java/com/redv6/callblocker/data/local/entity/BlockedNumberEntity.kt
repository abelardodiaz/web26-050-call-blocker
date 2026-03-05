package com.redv6.callblocker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redv6.callblocker.domain.model.BlockedNumber

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val label: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_prefix", defaultValue = "0")
    val isPrefix: Boolean = false
) {
    fun toDomain(): BlockedNumber = BlockedNumber(
        id = id,
        phoneNumber = phoneNumber,
        label = label,
        createdAt = createdAt,
        isPrefix = isPrefix
    )

    companion object {
        fun fromDomain(domain: BlockedNumber): BlockedNumberEntity = BlockedNumberEntity(
            id = domain.id,
            phoneNumber = domain.phoneNumber,
            label = domain.label,
            createdAt = domain.createdAt,
            isPrefix = domain.isPrefix
        )
    }
}
