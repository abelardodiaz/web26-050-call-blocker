package com.callblocker.data.backup

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encriptacion AES-256-GCM para backups protegidos con contrasena.
 */
object BackupEncryption {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val IV_LENGTH = 12 // 96 bits recommended for GCM
    private const val SALT_LENGTH = 16
    private const val TAG_LENGTH = 128 // Authentication tag length in bits
    private const val ITERATIONS = 100_000 // PBKDF2 iterations

    /**
     * Header magico para identificar archivos encriptados.
     */
    private const val MAGIC_HEADER = "CBBK" // Call Blocker Backup

    /**
     * Encripta datos JSON con contrasena.
     *
     * Formato del resultado:
     * [4 bytes: magic header][16 bytes: salt][12 bytes: IV][N bytes: ciphertext+tag]
     *
     * @param data JSON string a encriptar
     * @param password Contrasena del usuario
     * @return ByteArray con los datos encriptados
     */
    fun encrypt(data: String, password: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))

        val ciphertext = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Combinar: magic + salt + iv + ciphertext
        return MAGIC_HEADER.toByteArray(Charsets.UTF_8) + salt + iv + ciphertext
    }

    /**
     * Desencripta datos con contrasena.
     *
     * @param encryptedData Datos encriptados (incluyendo header, salt, iv)
     * @param password Contrasena del usuario
     * @return JSON string desencriptado
     * @throws IllegalArgumentException si el formato es invalido
     * @throws javax.crypto.AEADBadTagException si la contrasena es incorrecta
     */
    fun decrypt(encryptedData: ByteArray, password: String): String {
        // Verificar longitud minima: magic(4) + salt(16) + iv(12) + tag(16) = 48 bytes minimo
        if (encryptedData.size < 48) {
            throw IllegalArgumentException("Archivo encriptado invalido: muy corto")
        }

        // Verificar magic header
        val magic = String(encryptedData.sliceArray(0 until 4), Charsets.UTF_8)
        if (magic != MAGIC_HEADER) {
            throw IllegalArgumentException("Archivo no es un backup encriptado de Call Blocker")
        }

        val salt = encryptedData.sliceArray(4 until 20)
        val iv = encryptedData.sliceArray(20 until 32)
        val ciphertext = encryptedData.sliceArray(32 until encryptedData.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))

        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    /**
     * Verifica si los datos estan encriptados.
     *
     * @param data ByteArray a verificar
     * @return true si tiene el header magico de encriptacion
     */
    fun isEncrypted(data: ByteArray): Boolean {
        if (data.size < 4) return false
        val header = String(data.sliceArray(0 until 4), Charsets.UTF_8)
        return header == MAGIC_HEADER
    }

    /**
     * Encripta y codifica en Base64 (para incluir en JSON si es necesario).
     */
    fun encryptToBase64(data: String, password: String): String {
        return Base64.encodeToString(encrypt(data, password), Base64.NO_WRAP)
    }

    /**
     * Decodifica Base64 y desencripta.
     */
    fun decryptFromBase64(base64Data: String, password: String): String {
        return decrypt(Base64.decode(base64Data, Base64.NO_WRAP), password)
    }

    /**
     * Deriva una clave AES-256 de la contrasena usando PBKDF2.
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, KEY_ALGORITHM)
    }
}
