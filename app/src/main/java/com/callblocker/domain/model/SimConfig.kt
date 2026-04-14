package com.callblocker.domain.model

/**
 * Configuration for a SIM card (physical or eSIM).
 *
 * Allows enabling/disabling call blocking per SIM slot.
 */
data class SimConfig(
    val subscriptionId: Int,
    val simSlot: Int,
    val carrierName: String,
    val phoneNumber: String? = null,
    val isBlockingEnabled: Boolean = true
)
