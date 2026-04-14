package com.callblocker.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.callblocker.presentation.screens.blockedcalls.BlockedCallsScreen
import com.callblocker.presentation.screens.blocklist.BlockListScreen
import com.callblocker.presentation.screens.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Screen.BlockedCalls.route
        ) {
            composable(Screen.BlockedCalls.route) {
                BlockedCallsScreen()
            }
            composable(Screen.BlockList.route) {
                BlockListScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
