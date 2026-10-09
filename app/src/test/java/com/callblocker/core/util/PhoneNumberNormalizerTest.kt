package com.callblocker.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberNormalizerTest {

    @Test
    fun `quita simbolos y espacios`() {
        assertEquals("4441234567", PhoneNumberNormalizer.normalize("(444) 123-4567"))
    }

    @Test
    fun `quita codigo MX`() {
        assertEquals("4441234567", PhoneNumberNormalizer.normalize("+52 444 123 4567"))
    }

    @Test
    fun `quita codigo US`() {
        assertEquals("4441234567", PhoneNumberNormalizer.normalize("+1 444 123 4567"))
    }

    @Test
    fun `conserva otros codigos de pais`() {
        assertEquals("33162123456", PhoneNumberNormalizer.normalize("+33162123456"))
        assertEquals("33162", PhoneNumberNormalizer.normalize("+33162"))
    }

    @Test
    fun `numero guardado y llamada entrante coinciden`() {
        val stored = PhoneNumberNormalizer.normalize("+33 1 62 12 34 56")
        val incoming = PhoneNumberNormalizer.normalize("+33162123456")
        assertEquals(stored, incoming)
    }

    @Test
    fun `numeros cortos no se tocan`() {
        assertEquals("5212345", PhoneNumberNormalizer.normalize("5212345"))
        assertEquals("", PhoneNumberNormalizer.normalize(""))
    }

    @Test
    fun `candidatos incluyen variante con codigo de pais`() {
        assertEquals(
            listOf("4441234567", "524441234567"),
            PhoneNumberNormalizer.matchCandidates("+524441234567")
        )
        assertEquals(listOf("33162123456"), PhoneNumberNormalizer.matchCandidates("+33162123456"))
    }
}
