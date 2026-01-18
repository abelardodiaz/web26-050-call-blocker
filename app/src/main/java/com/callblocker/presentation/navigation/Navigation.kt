package com.callblocker.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.callblocker.R

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    data object BlockedCalls : Screen(
        route = "blocked_calls",
        titleResId = R.string.nav_blocked_calls,
        icon = Icons.Default.CallEnd
    )

    data object BlockList : Screen(
        route = "block_list",
        titleResId = R.string.nav_block_list,
        icon = Icons.Default.Block
    )

    data object Settings : Screen(
        route = "settings",
        titleResId = R.string.nav_settings,
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(BlockedCalls, BlockList, Settings)
    }
}
