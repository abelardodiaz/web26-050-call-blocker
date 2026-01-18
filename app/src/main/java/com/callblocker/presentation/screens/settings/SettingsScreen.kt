package com.callblocker.presentation.screens.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.callblocker.BuildConfig
import com.callblocker.core.util.PermissionHandler
import com.callblocker.core.util.SimManager
import com.callblocker.domain.model.SimConfig
import com.callblocker.presentation.components.BackupPasswordPromptDialog
import com.callblocker.presentation.components.PasswordDialog
import com.callblocker.presentation.components.PasswordDialogMode
import com.callblocker.presentation.components.SettingsButton
import com.callblocker.presentation.components.SettingsSwitch

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
                title = { Text("Ajustes") },
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
            // Seccion de Bloqueo
            SectionHeader("Bloqueo de Llamadas")

            SettingsSwitch(
                title = "Bloqueo Activo",
                description = "Bloquear llamadas de numeros en tu lista",
                checked = settings.isBlockingEnabled,
                onCheckedChange = { viewModel.setBlockingEnabled(it) }
            )

            SettingsSwitch(
                title = "Bloquear Desconocidos",
                description = "Proximamente...",
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            SettingsSwitch(
                title = "Bloquear Privados",
                description = "Proximamente...",
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            SettingsSwitch(
                title = "Mostrar Notificaciones",
                description = "Proximamente...",
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            SettingsSwitch(
                title = "Servicio Persistente",
                description = "Notificacion activa para mejor bloqueo",
                checked = settings.persistentServiceEnabled,
                onCheckedChange = { viewModel.setPersistentServiceEnabled(it) }
            )

            // Seccion de Tarjetas SIM (info visual, sin control)
            if (simCards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Tarjetas SIM Detectadas")

                SimInfoCard(simCards)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion de Respaldo
            SectionHeader("Respaldo")

            // Backup completo (nuevo)
            SettingsButton(
                title = "Backup Completo",
                description = "Guardar numeros, historial y configuracion",
                onClick = { showBackupPrompt = true },
                isLoading = isFullExporting
            )

            SettingsButton(
                title = "Restaurar Backup",
                description = "Recuperar datos desde archivo de backup",
                onClick = {
                    fullRestoreLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                },
                isLoading = isFullImporting
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legacy backup (solo numeros)
            Text(
                text = "Solo Lista Bloqueados",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            SettingsButton(
                title = "Exportar Lista",
                description = "Guardar solo numeros bloqueados (JSON)",
                onClick = { viewModel.exportBlockedNumbers() },
                isLoading = isExporting
            )

            SettingsButton(
                title = "Importar Lista",
                description = "Cargar numeros desde archivo JSON",
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                isLoading = isImporting
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion de Informacion del Sistema
            SectionHeader("Informacion del Sistema")

            InfoCard(
                items = listOf(
                    "Android" to "API ${Build.VERSION.SDK_INT} (${getAndroidVersionName()})",
                    "Metodo de Bloqueo" to if (PermissionHandler.supportsCallScreeningService()) {
                        "CallScreeningService"
                    } else {
                        "TelecomManager (Legacy)"
                    }
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
                    text = "Android 9: El bloqueo puede tener un breve retraso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Seccion Acerca de
            SectionHeader("Acerca de")

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
                                snackbarHostState.showSnackbar("Modo desarrollador activado")
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

                SectionHeader("Desarrollador")

                SettingsSwitch(
                    title = "Detectar SIM por formato",
                    description = "+52 = Bait (SIM1), sin +52 = AT&T (SIM2)",
                    checked = settings.devSimDetectionByFormat,
                    onCheckedChange = { viewModel.setDevSimDetectionByFormat(it) }
                )

                if (settings.devSimDetectionByFormat) {
                    SettingsSwitch(
                        title = "Bloquear en SIM 1 (Bait)",
                        description = "Llamadas con +52",
                        checked = settings.devBlockSim1,
                        onCheckedChange = { viewModel.setDevBlockSim1(it) }
                    )
                    SettingsSwitch(
                        title = "Bloquear en SIM 2 (AT&T)",
                        description = "Llamadas sin +52",
                        checked = settings.devBlockSim2,
                        onCheckedChange = { viewModel.setDevBlockSim2(it) }
                    )
                }

                SettingsSwitch(
                    title = "Desactivar modo dev",
                    description = "Oculta esta seccion",
                    checked = false,
                    onCheckedChange = {
                        viewModel.setDeveloperModeEnabled(false)
                        scope.launch {
                            snackbarHostState.showSnackbar("Modo desarrollador desactivado")
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
                        text = "SIM ${sim.simSlot + 1}: ${sim.carrierName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = sim.phoneNumber ?: "Sin numero",
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
                text = "El bloqueo aplica a todas las SIMs",
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
                text = "Call Blocker",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Desarrollado por",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "redv6.com",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compatible con Android 9 - 17",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
