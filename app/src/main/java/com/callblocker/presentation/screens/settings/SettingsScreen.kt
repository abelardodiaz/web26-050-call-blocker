package com.callblocker.presentation.screens.settings

import android.os.Build
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.callblocker.core.util.SimManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callblocker.BuildConfig
import com.callblocker.core.util.PermissionHandler
import com.callblocker.presentation.components.SettingsSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val simCards = remember { SimManager.getActiveSimCards(context) }

    Scaffold(
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
                description = "Bloquear llamadas con ID oculto",
                checked = settings.blockPrivateNumbers,
                onCheckedChange = { viewModel.setBlockPrivateNumbers(it) }
            )

            SettingsSwitch(
                title = "Mostrar Notificaciones",
                description = "Proximamente...",
                checked = false,
                enabled = false,
                onCheckedChange = { }
            )

            // Seccion de Tarjetas SIM (solo si hay SIMs detectadas)
            if (simCards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Tarjetas SIM")

                simCards.forEach { sim ->
                    val isEnabled = settings.enabledSimSlots.contains(sim.subscriptionId)
                    SettingsSwitch(
                        title = "${sim.carrierName} (SIM ${sim.simSlot + 1})",
                        description = sim.phoneNumber ?: "Sin numero",
                        checked = isEnabled,
                        onCheckedChange = { viewModel.setSimBlockingEnabled(sim.subscriptionId, it) }
                    )
                }
            }

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
