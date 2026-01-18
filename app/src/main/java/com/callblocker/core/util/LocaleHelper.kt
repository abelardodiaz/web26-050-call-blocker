package com.callblocker.core.util

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper for managing app language settings.
 * Uses AppCompatDelegate.setApplicationLocales() which works on Android 13+ natively
 * and provides backward compatibility for older versions.
 */
object LocaleHelper {

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_SPANISH = "es"
    const val LANGUAGE_ENGLISH = "en"

    /**
     * Apply the selected language to the app.
     * @param languageCode One of: "system", "es", "en"
     */
    fun setAppLanguage(languageCode: String) {
        val localeList = when (languageCode) {
            LANGUAGE_SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            LANGUAGE_SPANISH -> LocaleListCompat.forLanguageTags("es")
            LANGUAGE_ENGLISH -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Get the current language code from AppCompatDelegate.
     * Returns "system" if following system locale.
     */
    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            return LANGUAGE_SYSTEM
        }
        val locale = locales[0]
        return locale?.language ?: LANGUAGE_SYSTEM
    }

    /**
     * Get display name for a language code.
     */
    fun getLanguageDisplayName(languageCode: String, context: Context): String {
        return when (languageCode) {
            LANGUAGE_SYSTEM -> {
                // Get system locale display name
                val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
                systemLocale.getDisplayLanguage(systemLocale).replaceFirstChar { it.uppercase() }
            }
            LANGUAGE_SPANISH -> Locale("es").getDisplayLanguage(Locale("es")).replaceFirstChar { it.uppercase() }
            LANGUAGE_ENGLISH -> Locale("en").getDisplayLanguage(Locale("en")).replaceFirstChar { it.uppercase() }
            else -> languageCode
        }
    }

    /**
     * List of available language options.
     */
    val availableLanguages = listOf(
        LANGUAGE_SYSTEM,
        LANGUAGE_SPANISH,
        LANGUAGE_ENGLISH
    )
}
