package com.callblocker.core.util

/**
 * Normaliza numeros de telefono para guardarlos y compararlos con la misma regla.
 * Deja solo digitos y quita el codigo de pais MX (52) o US/CA (1) cuando sobran digitos.
 * Otros codigos de pais se conservan (+33162123456 -> 33162123456).
 */
object PhoneNumberNormalizer {

    private val NON_DIGITS = Regex("[^0-9]")

    fun digitsOnly(phoneNumber: String): String = phoneNumber.replace(NON_DIGITS, "")

    fun normalize(phoneNumber: String): String {
        val digits = digitsOnly(phoneNumber)
        return when {
            digits.length > 10 && digits.startsWith("52") -> digits.substring(2)
            digits.length > 10 && digits.startsWith("1") -> digits.substring(1)
            else -> digits
        }
    }

    /**
     * Variantes contra las que se compara una llamada entrante: normalizada y solo digitos.
     * Permite que prefijos guardados con codigo de pais (ej. +52 444) sigan coincidiendo.
     */
    fun matchCandidates(phoneNumber: String): List<String> =
        listOf(normalize(phoneNumber), digitsOnly(phoneNumber)).distinct()
}
