package com.callblocker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callblocker.domain.model.Settings
import com.callblocker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository
        .getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun setBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockingEnabled(enabled)
        }
    }

    fun setBlockUnknownNumbers(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockUnknownNumbers(enabled)
        }
    }

    fun setBlockPrivateNumbers(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlockPrivateNumbers(enabled)
        }
    }

    fun setShowNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowNotifications(enabled)
        }
    }

    fun setSimBlockingEnabled(subscriptionId: Int, enabled: Boolean) {
        viewModelScope.launch {
            val currentSlots = settings.value.enabledSimSlots.toMutableSet()
            if (enabled) {
                currentSlots.add(subscriptionId)
            } else {
                currentSlots.remove(subscriptionId)
            }
            settingsRepository.setEnabledSimSlots(currentSlots)
        }
    }
}
