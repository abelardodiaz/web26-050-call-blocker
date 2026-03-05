package com.redv6.callblocker.core.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper for managing app language settings.
 * Uses LocaleManager on Android 13+ (API 33+) for native per-app language support.
 * Falls back to AppCompatDelegate for older versions.
 */
object LocaleHelper {

    private const val TAG = "LocaleHelper"

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_SPANISH = "es"
    const val LANGUAGE_ENGLISH = "en"

    /**
     * Apply the selected language to the app.
     * @param languageCode One of: "system", "es", "en"
     * @param context Required for API 33+ to access LocaleManager
     */
    fun setAppLanguage(languageCode: String, context: Context? = null) {
        Log.d(TAG, "setAppLanguage called with: $languageCode (API ${Build.VERSION.SDK_INT})")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context != null) {
            // Android 13+ (API 33+): Use LocaleManager for native per-app language
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val localeList = when (languageCode) {
                    LANGUAGE_SYSTEM -> LocaleList.getEmptyLocaleList()
                    LANGUAGE_SPANISH -> LocaleList.forLanguageTags("es")
                    LANGUAGE_ENGLISH -> LocaleList.forLanguageTags("en")
                    else -> LocaleList.getEmptyLocaleList()
                }
                Log.d(TAG, "Using LocaleManager.setApplicationLocales($localeList)")
                localeManager.applicationLocales = localeList
                Log.d(TAG, "LocaleManager.setApplicationLocales() completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting locale via LocaleManager", e)
            }
        } else {
            // Android 12 and below: Use AppCompatDelegate
            val localeList = when (languageCode) {
                LANGUAGE_SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                LANGUAGE_SPANISH -> LocaleListCompat.forLanguageTags("es")
                LANGUAGE_ENGLISH -> LocaleListCompat.forLanguageTags("en")
                else -> LocaleListCompat.getEmptyLocaleList()
            }
            Log.d(TAG, "Using AppCompatDelegate.setApplicationLocales($localeList)")
            AppCompatDelegate.setApplicationLocales(localeList)
            Log.d(TAG, "AppCompatDelegate.setApplicationLocales() completed")
        }
    }

    /**
     * Get the current language code.
     * Returns "system" if following system locale.
     */
    fun getCurrentLanguage(context: Context? = null): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context != null) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val locales = localeManager.applicationLocales
                if (locales.isEmpty) {
                    return LANGUAGE_SYSTEM
                }
                return locales[0]?.language ?: LANGUAGE_SYSTEM
            } catch (e: Exception) {
                Log.e(TAG, "Error getting locale via LocaleManager", e)
            }
        }

        // Fallback to AppCompatDelegate
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            return LANGUAGE_SYSTEM
        }
        return locales[0]?.language ?: LANGUAGE_SYSTEM
    }

    /**
     * Get display name for a language code.
     */
    fun getLanguageDisplayName(languageCode: String, context: Context): String {
        return when (languageCode) {
            LANGUAGE_SYSTEM -> {
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
