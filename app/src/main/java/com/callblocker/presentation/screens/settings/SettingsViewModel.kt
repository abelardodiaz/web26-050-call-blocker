package com.callblocker.presentation.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callblocker.core.service.CallBlockerForegroundService
import com.callblocker.domain.model.Settings
import com.callblocker.domain.repository.SettingsRepository
import com.callblocker.domain.usecase.ExportBlockedNumbersUseCase
import com.callblocker.domain.usecase.ImportBlockedNumbersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val exportBlockedNumbersUseCase: ExportBlockedNumbersUseCase,
    private val importBlockedNumbersUseCase: ImportBlockedNumbersUseCase
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository
        .getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    // Estado para operaciones de backup
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

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

    fun exportBlockedNumbers() {
        viewModelScope.launch {
            _isExporting.value = true
            _backupMessage.value = null

            exportBlockedNumbersUseCase.execute()
                .onSuccess { filePath ->
                    _backupMessage.value = "Exportado a: $filePath"
                }
                .onFailure { error ->
                    _backupMessage.value = "Error: ${error.message}"
                }

            _isExporting.value = false
        }
    }

    fun importBlockedNumbers(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            _backupMessage.value = null

            importBlockedNumbersUseCase.execute(uri)
                .onSuccess { result ->
                    _backupMessage.value = buildString {
                        append("Importados: ${result.imported}")
                        if (result.skipped > 0) {
                            append(", Omitidos: ${result.skipped}")
                        }
                        if (result.errors > 0) {
                            append(", Errores: ${result.errors}")
                        }
                    }
                }
                .onFailure { error ->
                    _backupMessage.value = "Error: ${error.message}"
                }

            _isImporting.value = false
        }
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }

    fun setPersistentServiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPersistentServiceEnabled(enabled)
            if (enabled) {
                CallBlockerForegroundService.start(application)
            } else {
                CallBlockerForegroundService.stop(application)
            }
        }
    }
}
