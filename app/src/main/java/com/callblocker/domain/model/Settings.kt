package com.callblocker.domain.model

data class Settings(
    val isBlockingEnabled: Boolean = true,
    val blockUnknownNumbers: Boolean = false,
    val blockPrivateNumbers: Boolean = false,
    val showNotifications: Boolean = false,
    val enabledSimSlots: Set<Int> = emptySet(), // Subscription IDs with blocking enabled
    val persistentServiceEnabled: Boolean = false, // Foreground service for reliable blocking
    val appLanguage: String = "system", // "system", "es", "en"
    // Developer mode settings
    val developerModeEnabled: Boolean = false,
    val devSimDetectionByFormat: Boolean = false,  // Detect SIM by +52 format
    val devBlockSim1: Boolean = true,              // Block on SIM 1 (Bait)
    val devBlockSim2: Boolean = true               // Block on SIM 2 (AT&T)
)
