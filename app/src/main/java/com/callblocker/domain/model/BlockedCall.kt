package com.callblocker.domain.model

data class BlockedCall(
    val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: BlockReason = BlockReason.BLOCK_LIST
)

enum class BlockReason {
    BLOCK_LIST,
    UNKNOWN_NUMBER,
    PRIVATE_NUMBER
}
