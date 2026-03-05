package com.redv6.callblocker.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.redv6.callblocker.domain.model.SimConfig

/**
 * Utility class for managing SIM card information.
 *
 * Provides detection of active SIM cards (physical and eSIM) for per-SIM
 * blocking configuration. Uses SubscriptionManager which is available since API 22.
 */
object SimManager {

    /**
     * Gets the list of active SIM cards on the device.
     *
     * Requires READ_PHONE_STATE permission.
     * On Android 12+ (API 31+), also requires READ_PHONE_NUMBERS for SIM detection.
     *
     * @param context Application context
     * @return List of SimConfig for each active SIM, or empty list if no SIMs or no permission
     */
    fun getActiveSimCards(context: Context): List<SimConfig> {
        // Check READ_PHONE_STATE permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }

        // Android 12+: Also check READ_PHONE_NUMBERS (required for getActiveSubscriptionInfoList)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return emptyList()
            }
        }

        return try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                as? SubscriptionManager ?: return emptyList()

            subscriptionManager.activeSubscriptionInfoList?.map { subInfo ->
                SimConfig(
                    subscriptionId = subInfo.subscriptionId,
                    simSlot = subInfo.simSlotIndex,
                    carrierName = subInfo.carrierName?.toString() ?: "SIM ${subInfo.simSlotIndex + 1}",
                    phoneNumber = getPhoneNumber(context, subInfo.subscriptionId),
                    isBlockingEnabled = true // Default enabled, actual state comes from Settings
                )
            } ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    /**
     * Gets phone number for a SIM if available.
     *
     * May return null if number is not available (carrier restriction).
     * Requires READ_PHONE_NUMBERS permission on API 31+.
     */
    private fun getPhoneNumber(context: Context, subscriptionId: Int): String? {
        // Check for READ_PHONE_NUMBERS permission (required on API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
        }

        return try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                as? SubscriptionManager ?: return null

            // API 33+: Use SubscriptionManager.getPhoneNumber()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                subscriptionManager.getPhoneNumber(subscriptionId).takeIf { it.isNotBlank() }
            } else {
                // API 31-32: Try to get from SubscriptionInfo (may not be available)
                @Suppress("DEPRECATION")
                subscriptionManager.activeSubscriptionInfoList
                    ?.find { it.subscriptionId == subscriptionId }
                    ?.number
                    ?.takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if the device has multiple active SIMs.
     */
    fun isDualSimActive(context: Context): Boolean {
        return getActiveSimCards(context).size > 1
    }

    /**
     * Checks if the device supports dual SIM.
     *
     * Note: This checks for dual SIM support, not whether both slots are active.
     */
    fun supportsDualSim(context: Context): Boolean {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
            as? SubscriptionManager ?: return false

        return try {
            subscriptionManager.activeSubscriptionInfoCountMax > 1
        } catch (e: Exception) {
            false
        }
    }
}
