package com.callblocker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.callblocker.R
import com.callblocker.domain.model.BlockReason
import com.callblocker.domain.model.BlockedCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlockedCallCard(
    blockedCall: BlockedCall,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val privateNumber = stringResource(R.string.private_number)
    val deleteContentDesc = stringResource(R.string.content_desc_delete_call)

    // Get current locale for date formatting
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = blockedCall.phoneNumber.ifEmpty { privateNumber },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildString {
                        append(formatTimestamp(blockedCall.timestamp, locale))
                        blockedCall.simSlot?.let { slot ->
                            append(stringResource(R.string.sim_slot_format, slot + 1))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatBlockReason(blockedCall.reason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = deleteContentDesc,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long, locale: Locale): String {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", locale)
    return sdf.format(Date(timestamp))
}

@Composable
private fun formatBlockReason(reason: BlockReason): String {
    return when (reason) {
        BlockReason.BLOCK_LIST -> stringResource(R.string.block_reason_list)
        BlockReason.UNKNOWN_NUMBER -> stringResource(R.string.block_reason_unknown)
        BlockReason.PRIVATE_NUMBER -> stringResource(R.string.block_reason_private)
    }
}
