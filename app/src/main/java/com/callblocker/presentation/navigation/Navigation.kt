package com.callblocker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object BlockedCalls : Screen(
        route = "blocked_calls",
        title = "Llamadas",
        icon = Icons.Default.CallEnd
    )

    data object BlockList : Screen(
        route = "block_list",
        title = "Lista",
        icon = Icons.Default.Block
    )

    data object Settings : Screen(
        route = "settings",
        title = "Ajustes",
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(BlockedCalls, BlockList, Settings)
    }
}
