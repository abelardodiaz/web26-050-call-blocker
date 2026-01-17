package com.callblocker.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callblocker.presentation.components.SettingsSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp)
        ) {
            SettingsSwitch(
                title = "Call Blocking Enabled",
                description = "Block calls from numbers in your block list",
                checked = settings.isBlockingEnabled,
                onCheckedChange = { viewModel.setBlockingEnabled(it) }
            )

            SettingsSwitch(
                title = "Block Unknown Numbers",
                description = "Block calls from numbers not in contacts",
                checked = settings.blockUnknownNumbers,
                onCheckedChange = { viewModel.setBlockUnknownNumbers(it) }
            )

            SettingsSwitch(
                title = "Block Private Numbers",
                description = "Block calls with hidden caller ID",
                checked = settings.blockPrivateNumbers,
                onCheckedChange = { viewModel.setBlockPrivateNumbers(it) }
            )

            SettingsSwitch(
                title = "Show Notifications",
                description = "Get notified when a call is blocked",
                checked = settings.showNotifications,
                onCheckedChange = { viewModel.setShowNotifications(it) }
            )
        }
    }
}
