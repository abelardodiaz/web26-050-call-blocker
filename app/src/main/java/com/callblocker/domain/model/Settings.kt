package com.callblocker.domain.model

data class Settings(
    val isBlockingEnabled: Boolean = true,
    val blockUnknownNumbers: Boolean = false,
    val blockPrivateNumbers: Boolean = false,
    val showNotifications: Boolean = true
)
