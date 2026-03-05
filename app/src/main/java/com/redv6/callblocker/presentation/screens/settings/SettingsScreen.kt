package com.redv6.callblocker.presentation.screens.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.redv6.callblocker.BuildConfig
import com.redv6.callblocker.R
import com.redv6.callblocker.core.util.LocaleHelper
import com.redv6.callblocker.core.util.PermissionHandler
import com.redv6.callblocker.core.util.SimManager
import com.redv6.callblocker.domain.model.SimConfig
import com.redv6.callblocker.presentation.components.BackupPasswordPromptDialog
import com.redv6.callblocker.presentation.components.PasswordDialog
import com.redv6.callblocker.presentation.components.PasswordDialogMode
import com.redv6.callblocker.presentation.components.SettingsButton
import com.redv6.callblocker.presentation.components.SettingsSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val isFullExporting by viewModel.isFullExporting.collectAsState()
    val isFullImporting by viewModel.isFullImporting.collectAsState()
    val passwordDialogState by viewModel.passwordDialogState.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estados para la activacion secreta de 2 pasos
    var infoSectionTaps by remember { mutableIntStateOf(0) }
    var infoSectionLastTap by remember { mutableLongStateOf(0L) }
    var step1Completed by remember { mutableStateOf(false) }
    var step1CompletedTime by remember { mutableLongStateOf(0L) }

    var aboutSectionTaps by remember { mutableIntStateOf(0) }
    var aboutSectionLastTap by remember { mutableLongStateOf(0L) }

    // Estado reactivo para SIMs - se re-evalua cuando settings cambia
    // (permisos otorgados cambian el estado de settings)
    var simCards by remember { mutableStateOf(emptyList<SimConfig>()) }
    LaunchedEffect(settings) {
        simCards = SimManager.getActiveSimCards(context)
    }

    // Launcher para seleccionar archivo JSON para importar (legacy - solo numeros)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBlockedNumbers(it) }
    }

    // Launcher para seleccionar archivo para restore completo
    val fullRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.startFullRestore(it) }
    }

    // Mostrar mensaje de backup en snackbar
    LaunchedEffect(backupMessage) {
        backupMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearBackupMessage()
        }
    }

    // Strings for snackbar messages
    val devEnabledMessage = stringResource(R.string.settings_dev_enabled)
    val devDisabledMessage = stringResource(R.string.settings_dev_disabled)

    // Manejar dialogos de contrasena
    when (val state = passwordDialogState) {
        is PasswordDialogState.Hidden -> { /* No dialog */ }

        is PasswordDialogState.PromptEncrypt -> {
            BackupPasswordPromptDialog(
                onWithPassword = {
                    viewModel.dismissPasswordDialog()
                    // Mostrar dialogo para ingresar contrasena
                    viewModel.executeFullBackup(null) // Temporal, se reemplaza abajo
                },
                onWithoutPassword = {
                    viewModel.executeFullBackup(null)
                },
                onDismiss = { viewModel.dismissPasswordDialog() }
            )
        }

        is PasswordDialogState.EnterEncryptPassword -> {
            PasswordDialog(
                mode = PasswordDialogMode.ENCRYPT,
                onConfirm = { password ->
                    viewModel.executeFullBackup(password)
                },
                onDismiss = { viewModel.dismissPasswordDialog() }
            )
        }

        is PasswordDialogState.RequestDecrypt -> {
            PasswordDialog(
                mode = PasswordDialogMode.DECRYPT,
                onConfirm = { password ->
                    viewModel.executeFullRestore(state.uri, password)
                },
                onDismiss = { viewModel.dismissPasswordDialog() }
            )
        }
    }

    // Estado para mostrar dialogo de encriptacion
    var showEncryptDialog by remember { mutableStateOf(false) }

    if (showEncryptDialog) {
        PasswordDialog(
            mode = PasswordDialogMode.ENCRYPT,
            onConfirm = { password ->
                showEncryptDialog = false
                viewModel.executeFullBackup(password)
            },
            onDismiss = { showEncryptDialog = false }
        )
    }

    // Estado para prompt de backup
    var showBackupPrompt by remember { mutableStateOf(false) }

    if (showBackupPrompt) {
        BackupPasswordPromptDialog(
            onWithPassword = {
                showBackupPrompt = false
                showEncryptDialog = true
            },
            onWithoutPassword = {
                showBackupPrompt = false
                viewModel.executeFullBackup(null)
            },
            onDismiss = { showBackupPrompt = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_title_settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Seccion de Idioma
            SectionHeader(stringResource(R.string.settings_section_language))

            LanguageSelector(
                currentLanguage = settings.appLanguage,
                onLanguageSelected = { viewModel.setAppLanguage(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion de Bloqueo
            SectionHeader(stringResource(R.string.settings_section_blocking))

            SettingsSwitch(
                title = stringResource(R.string.settings_blocking_enabled),
                description = stringResource(R.string.settings_blocking_enabled_desc),
                checked = settings.isBlockingEnabled,
                onCheckedChange = { viewModel.setBlockingEnabled(it) }
            )

            SettingsSwitch(
                title = stringResource(R.string.settings_block_unknown),
                description = stringResource(R.string.settings_coming_soon),
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            SettingsSwitch(
                title = stringResource(R.string.settings_block_private),
                description = stringResource(R.string.settings_coming_soon),
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            SettingsSwitch(
                title = stringResource(R.string.settings_notifications),
                description = stringResource(R.string.settings_coming_soon),
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            SettingsSwitch(
                title = stringResource(R.string.settings_persistent_service),
                description = stringResource(R.string.settings_persistent_service_desc),
                checked = settings.persistentServiceEnabled,
                onCheckedChange = { viewModel.setPersistentServiceEnabled(it) }
            )

            // Seccion de Tarjetas SIM (info visual, sin control)
            if (simCards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(stringResource(R.string.settings_section_sim))

                SimInfoCard(simCards)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion de Respaldo
            SectionHeader(stringResource(R.string.settings_section_backup))

            // Backup completo (nuevo)
            SettingsButton(
                title = stringResource(R.string.settings_full_backup),
                description = stringResource(R.string.settings_full_backup_desc),
                onClick = { showBackupPrompt = true },
                isLoading = isFullExporting
            )

            SettingsButton(
                title = stringResource(R.string.settings_restore_backup),
                description = stringResource(R.string.settings_restore_backup_desc),
                onClick = {
                    fullRestoreLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                },
                isLoading = isFullImporting
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legacy backup (solo numeros)
            Text(
                text = stringResource(R.string.settings_legacy_backup_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            SettingsButton(
                title = stringResource(R.string.settings_export_list),
                description = stringResource(R.string.settings_export_list_desc),
                onClick = { viewModel.exportBlockedNumbers() },
                isLoading = isExporting
            )

            SettingsButton(
                title = stringResource(R.string.settings_import_list),
                description = stringResource(R.string.settings_import_list_desc),
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                isLoading = isImporting
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion de Informacion del Sistema
            SectionHeader(stringResource(R.string.settings_section_system))

            val androidLabel = stringResource(R.string.settings_android)
            val androidVersion = stringResource(R.string.settings_android_version_format, Build.VERSION.SDK_INT, getAndroidVersionName())
            val blockingMethodLabel = stringResource(R.string.settings_blocking_method)
            val blockingMethodValue = if (PermissionHandler.supportsCallScreeningService()) {
                stringResource(R.string.settings_method_screening)
            } else {
                stringResource(R.string.settings_method_legacy)
            }

            InfoCard(
                items = listOf(
                    androidLabel to androidVersion,
                    blockingMethodLabel to blockingMethodValue
                ),
                onTap = {
                    val now = System.currentTimeMillis()
                    if (now - infoSectionLastTap < 2000) {
                        infoSectionTaps++
                        if (infoSectionTaps >= 7) {
                            step1Completed = true
                            step1CompletedTime = now
                            infoSectionTaps = 0
                        }
                    } else {
                        infoSectionTaps = 1
                    }
                    infoSectionLastTap = now
                }
            )

            if (PermissionHandler.requiresLegacyBlocker()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_android9_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion Acerca de
            SectionHeader(stringResource(R.string.settings_section_about))

            AboutCard(
                onTap = {
                    val now = System.currentTimeMillis()

                    // Verificar que paso 1 se completo hace menos de 10 segundos
                    if (!step1Completed || now - step1CompletedTime > 10000) {
                        step1Completed = false
                        aboutSectionTaps = 0
                        return@AboutCard
                    }

                    if (now - aboutSectionLastTap < 2000) {
                        aboutSectionTaps++
                        if (aboutSectionTaps >= 7) {
                            // Activar modo desarrollador
                            viewModel.setDeveloperModeEnabled(true)
                            step1Completed = false
                            aboutSectionTaps = 0
                            scope.launch {
                                snackbarHostState.showSnackbar(devEnabledMessage)
                            }
                        }
                    } else {
                        aboutSectionTaps = 1
                    }
                    aboutSectionLastTap = now
                }
            )

            // Seccion de Desarrollador (oculta por defecto)
            if (settings.developerModeEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(stringResource(R.string.settings_section_developer))

                SettingsSwitch(
                    title = stringResource(R.string.settings_dev_sim_detection),
                    description = stringResource(R.string.settings_dev_sim_detection_desc),
                    checked = settings.devSimDetectionByFormat,
                    onCheckedChange = { viewModel.setDevSimDetectionByFormat(it) }
                )

                if (settings.devSimDetectionByFormat) {
                    SettingsSwitch(
                        title = stringResource(R.string.settings_dev_block_sim1),
                        description = stringResource(R.string.settings_dev_block_sim1_desc),
                        checked = settings.devBlockSim1,
                        onCheckedChange = { viewModel.setDevBlockSim1(it) }
                    )
                    SettingsSwitch(
                        title = stringResource(R.string.settings_dev_block_sim2),
                        description = stringResource(R.string.settings_dev_block_sim2_desc),
                        checked = settings.devBlockSim2,
                        onCheckedChange = { viewModel.setDevBlockSim2(it) }
                    )
                }

                SettingsSwitch(
                    title = stringResource(R.string.settings_dev_disable),
                    description = stringResource(R.string.settings_dev_disable_desc),
                    checked = false,
                    onCheckedChange = {
                        viewModel.setDeveloperModeEnabled(false)
                        scope.launch {
                            snackbarHostState.showSnackbar(devDisabledMessage)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoCard(
    items: List<Pair<String, String>>,
    onTap: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            items.forEachIndexed { index, (label, value) ->
                InfoRow(label = label, value = value)
                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SimInfoCard(simCards: List<SimConfig>) {
    val simNoNumber = stringResource(R.string.sim_no_number)
    val simBlockingAll = stringResource(R.string.sim_blocking_all)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            simCards.forEachIndexed { index, sim ->
                Column {
                    Text(
                        text = stringResource(R.string.sim_card_format, sim.simSlot + 1, sim.carrierName),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = sim.phoneNumber ?: simNoNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index < simCards.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = simBlockingAll,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AboutCard(
    onTap: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_developed_by),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.about_developer),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_compatibility),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languageOptions = listOf(
        LocaleHelper.LANGUAGE_SYSTEM to stringResource(R.string.settings_language_system),
        LocaleHelper.LANGUAGE_SPANISH to stringResource(R.string.settings_language_spanish),
        LocaleHelper.LANGUAGE_ENGLISH to stringResource(R.string.settings_language_english)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_app_language),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            languageOptions.forEach { (code, displayName) ->
                LanguageOption(
                    displayName = displayName,
                    isSelected = currentLanguage == code,
                    onClick = { onLanguageSelected(code) }
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun getAndroidVersionName(): String {
    return when (Build.VERSION.SDK_INT) {
        28 -> "Pie"
        29 -> "Q"
        30 -> "R (Android 11)"
        31 -> "S (Android 12)"
        32 -> "S_V2 (Android 12L)"
        33 -> "Tiramisu (Android 13)"
        34 -> "Upside Down Cake (Android 14)"
        35 -> "VanillaIceCream (Android 15)"
        36 -> "Android 16"
        37 -> "Android 17"
        else -> "Android ${Build.VERSION.SDK_INT}"
    }
}
