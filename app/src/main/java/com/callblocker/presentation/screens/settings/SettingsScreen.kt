package com.callblocker.presentation.screens.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callblocker.BuildConfig
import com.callblocker.core.util.PermissionHandler
import com.callblocker.core.util.SimManager
import com.callblocker.domain.model.SimConfig
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

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado reactivo para SIMs - se re-evalua cuando settings cambia
    // (permisos otorgados cambian el estado de settings)
    var simCards by remember { mutableStateOf(emptyList<SimConfig>()) }
    LaunchedEffect(settings) {
        simCards = SimManager.getActiveSimCards(context)
    }

    // Launcher para seleccionar archivo JSON para importar
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBlockedNumbers(it) }
    }

    // Mostrar mensaje de backup en snackbar
    LaunchedEffect(backupMessage) {
        backupMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearBackupMessage()
        }
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

            SettingsButton(
                title = "Exportar Lista",
                description = "Guardar numeros bloqueados en archivo JSON",
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
                )
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

            AboutCard()
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
private fun InfoCard(items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
