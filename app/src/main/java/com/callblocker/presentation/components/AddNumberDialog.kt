package com.callblocker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.callblocker.R

@Composable
fun AddNumberDialog(
    onDismiss: () -> Unit,
    onConfirm: (phoneNumber: String, label: String?, isPrefix: Boolean) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isPrefix by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_add_number)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.phone_number_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(stringResource(R.string.phone_number_error)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.block_as_prefix))
                    Switch(
                        checked = isPrefix,
                        onCheckedChange = { isPrefix = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (phoneNumber.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(phoneNumber.trim(), label.trim().takeIf { it.isNotEmpty() }, isPrefix)
                    }
                }
            ) {
                Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
