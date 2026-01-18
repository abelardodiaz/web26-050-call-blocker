package com.callblocker.presentation.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callblocker.core.service.CallBlockerForegroundService
import com.callblocker.domain.model.BackupData
import com.callblocker.domain.model.Settings
import com.callblocker.domain.repository.SettingsRepository
import com.callblocker.domain.usecase.ExportBlockedNumbersUseCase
import com.callblocker.domain.usecase.ExportFullBackupUseCase
import com.callblocker.domain.usecase.ImportBlockedNumbersUseCase
import com.callblocker.domain.usecase.ImportFullBackupUseCase
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
    private val importBlockedNumbersUseCase: ImportBlockedNumbersUseCase,
    private val exportFullBackupUseCase: ExportFullBackupUseCase,
    private val importFullBackupUseCase: ImportFullBackupUseCase
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository
        .getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    // Estado para operaciones de backup (legacy - solo numeros)
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    // Estado para backup completo
    private val _isFullExporting = MutableStateFlow(false)
    val isFullExporting: StateFlow<Boolean> = _isFullExporting.asStateFlow()

    private val _isFullImporting = MutableStateFlow(false)
    val isFullImporting: StateFlow<Boolean> = _isFullImporting.asStateFlow()

    // Estado para dialogo de contrasena
    private val _passwordDialogState = MutableStateFlow<PasswordDialogState>(PasswordDialogState.Hidden)
    val passwordDialogState: StateFlow<PasswordDialogState> = _passwordDialogState.asStateFlow()

    // Preview del backup antes de importar
    private val _backupPreview = MutableStateFlow<BackupData?>(null)
    val backupPreview: StateFlow<BackupData?> = _backupPreview.asStateFlow()

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

    // ====== Legacy backup (solo numeros) ======

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

    // ====== Full backup (numeros + historial + settings) ======

    /**
     * Inicia el proceso de backup completo.
     * Muestra dialogo para decidir si encriptar.
     */
    fun startFullBackup() {
        _passwordDialogState.value = PasswordDialogState.PromptEncrypt
    }

    /**
     * Ejecuta el backup completo con la contrasena opcional.
     */
    fun executeFullBackup(password: String?) {
        viewModelScope.launch {
            _passwordDialogState.value = PasswordDialogState.Hidden
            _isFullExporting.value = true
            _backupMessage.value = null

            exportFullBackupUseCase.execute(password)
                .onSuccess { filePath ->
                    val encrypted = if (password != null) " (encriptado)" else ""
                    _backupMessage.value = "Backup completo guardado en: $filePath$encrypted"
                }
                .onFailure { error ->
                    _backupMessage.value = "Error: ${error.message}"
                }

            _isFullExporting.value = false
        }
    }

    /**
     * Inicia el proceso de restauracion.
     * Verifica si el backup esta encriptado.
     */
    fun startFullRestore(uri: Uri) {
        viewModelScope.launch {
            _isFullImporting.value = true

            val isEncrypted = importFullBackupUseCase.isBackupEncrypted(uri)

            if (isEncrypted) {
                // Solicitar contrasena
                _passwordDialogState.value = PasswordDialogState.RequestDecrypt(uri)
                _isFullImporting.value = false
            } else {
                // Importar directamente
                executeFullRestore(uri, null)
            }
        }
    }

    /**
     * Ejecuta la restauracion con la contrasena proporcionada.
     */
    fun executeFullRestore(uri: Uri, password: String?) {
        viewModelScope.launch {
            _passwordDialogState.value = PasswordDialogState.Hidden
            _isFullImporting.value = true
            _backupMessage.value = null

            importFullBackupUseCase.execute(uri, password)
                .onSuccess { result ->
                    _backupMessage.value = buildString {
                        append("Restaurado: ")
                        append("${result.numbersImported} numeros")
                        if (result.numbersSkipped > 0) {
                            append(" (${result.numbersSkipped} existentes)")
                        }
                        append(", ${result.callsImported} llamadas")
                        if (result.settingsRestored) {
                            append(", configuracion")
                        }
                    }
                }
                .onFailure { error ->
                    _backupMessage.value = "Error: ${error.message}"
                }

            _isFullImporting.value = false
        }
    }

    fun dismissPasswordDialog() {
        _passwordDialogState.value = PasswordDialogState.Hidden
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

    // Developer mode functions
    fun setDeveloperModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeveloperModeEnabled(enabled)
        }
    }

    fun setDevSimDetectionByFormat(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDevSimDetectionByFormat(enabled)
        }
    }

    fun setDevBlockSim1(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDevBlockSim1(enabled)
        }
    }

    fun setDevBlockSim2(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDevBlockSim2(enabled)
        }
    }
}

/**
 * Estados del dialogo de contrasena.
 */
sealed class PasswordDialogState {
    /** Dialogo oculto */
    data object Hidden : PasswordDialogState()

    /** Preguntando si quiere encriptar (para export) */
    data object PromptEncrypt : PasswordDialogState()

    /** Solicitando contrasena para crear backup encriptado */
    data object EnterEncryptPassword : PasswordDialogState()

    /** Solicitando contrasena para restaurar backup encriptado */
    data class RequestDecrypt(val uri: Uri) : PasswordDialogState()
}
