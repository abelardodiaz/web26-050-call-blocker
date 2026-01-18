package com.callblocker.domain.model

data class Settings(
    val isBlockingEnabled: Boolean = true,
    val blockUnknownNumbers: Boolean = false,
    val blockPrivateNumbers: Boolean = false,
    val showNotifications: Boolean = false,
    val enabledSimSlots: Set<Int> = emptySet(), // Subscription IDs with blocking enabled
    val persistentServiceEnabled: Boolean = false // Foreground service for reliable blocking
)
