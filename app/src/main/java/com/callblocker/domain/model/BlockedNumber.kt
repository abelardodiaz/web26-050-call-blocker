package com.callblocker.domain.model

data class BlockedNumber(
    val id: Long = 0,
    val phoneNumber: String,
    val label: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
