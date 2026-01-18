package com.callblocker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Modo del dialogo de contrasena.
 */
enum class PasswordDialogMode {
    /** Crear contrasena para encriptar backup */
    ENCRYPT,
    /** Ingresar contrasena para desencriptar backup */
    DECRYPT
}

/**
 * Dialogo para solicitar contrasena en operaciones de backup.
 *
 * @param mode ENCRYPT para crear contrasena, DECRYPT para solicitarla
 * @param onConfirm Callback con la contrasena (null si se omite en ENCRYPT)
 * @param onDismiss Callback cuando se cancela
 */
@Composable
fun PasswordDialog(
    mode: PasswordDialogMode,
    onConfirm: (password: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val title = when (mode) {
        PasswordDialogMode.ENCRYPT -> "Proteger Backup"
        PasswordDialogMode.DECRYPT -> "Backup Protegido"
    }

    val description = when (mode) {
        PasswordDialogMode.ENCRYPT -> "Ingresa una contrasena para proteger el backup. Dejala vacia para no encriptar."
        PasswordDialogMode.DECRYPT -> "Este backup esta protegido. Ingresa la contrasena para restaurarlo."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Contrasena") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                // Solo mostrar confirmacion en modo ENCRYPT
                if (mode == PasswordDialogMode.ENCRYPT && password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirmar contrasena") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (mode) {
                        PasswordDialogMode.ENCRYPT -> {
                            if (password.isEmpty()) {
                                // Sin encriptacion
                                onConfirm(null)
                            } else if (password != confirmPassword) {
                                errorMessage = "Las contrasenas no coinciden"
                            } else if (password.length < 4) {
                                errorMessage = "La contrasena debe tener al menos 4 caracteres"
                            } else {
                                onConfirm(password)
                            }
                        }
                        PasswordDialogMode.DECRYPT -> {
                            if (password.isEmpty()) {
                                errorMessage = "Ingresa la contrasena"
                            } else {
                                onConfirm(password)
                            }
                        }
                    }
                }
            ) {
                Text(
                    when (mode) {
                        PasswordDialogMode.ENCRYPT -> if (password.isEmpty()) "Sin contrasena" else "Encriptar"
                        PasswordDialogMode.DECRYPT -> "Desencriptar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Dialogo para preguntar si se quiere proteger el backup.
 */
@Composable
fun BackupPasswordPromptDialog(
    onWithPassword: () -> Unit,
    onWithoutPassword: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup Completo") },
        text = {
            Text("Quieres proteger el backup con contrasena? Los backups encriptados son mas seguros pero requieren la contrasena para restaurar.")
        },
        confirmButton = {
            TextButton(onClick = onWithPassword) {
                Text("Con contrasena")
            }
        },
        dismissButton = {
            TextButton(onClick = onWithoutPassword) {
                Text("Sin contrasena")
            }
        }
    )
}
