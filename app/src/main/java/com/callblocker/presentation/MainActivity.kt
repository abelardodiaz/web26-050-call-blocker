package com.callblocker.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.callblocker.core.service.CallBlockerForegroundService
import com.callblocker.domain.repository.SettingsRepository
import com.callblocker.presentation.navigation.AppNavigation
import com.callblocker.presentation.navigation.Screen
import com.callblocker.presentation.theme.CallBlockerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "=== onCreate START ===")
        Log.d(TAG, "API Level: ${Build.VERSION.SDK_INT}")

        try {
            super.onCreate(savedInstanceState)
            logPermissionsStatus()
            startPersistentServiceIfEnabled()
            enableEdgeToEdge()
            setContent {
                CallBlockerTheme {
                    MainScreen()
                }
            }
            Log.d(TAG, "=== onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e(TAG, "=== onCreate FAILED ===", e)
            throw e
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "=== onResume ===")
        logPermissionsStatus()
    }

    private fun startPersistentServiceIfEnabled() {
        lifecycleScope.launch {
            try {
                val isEnabled = settingsRepository.isPersistentServiceEnabled()
                val isRunning = CallBlockerForegroundService.isRunning(this@MainActivity)
                Log.d(TAG, "PersistentService: enabled=$isEnabled, running=$isRunning")

                if (isEnabled && !isRunning) {
                    Log.d(TAG, "Iniciando servicio persistente...")
                    CallBlockerForegroundService.start(this@MainActivity)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/starting persistent service", e)
            }
        }
    }

    private fun logPermissionsStatus() {
        Log.d(TAG, "--- ESTADO DE PERMISOS ---")

        val permissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_CONTACTS
        )

        permissions.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            val name = permission.substringAfterLast(".")
            Log.d(TAG, "  $name: ${if (granted) "GRANTED" else "DENIED"}")
        }

        Log.d(TAG, "--- FIN PERMISOS ---")
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.bottomNavItems.forEach { screen ->
                    val title = stringResource(screen.titleResId)
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
