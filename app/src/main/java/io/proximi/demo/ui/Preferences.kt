package io.proximi.demo.ui

import android.content.Context
import androidx.preference.PreferenceManager

private const val PREFERENCE_PRIVACY_POLICY_ACCEPTED = "preference_privacy_policy"
private const val PREFERENCE_USE_ACCESSIBLE_ROUTES = "preference_use_accessible_routes"
private const val PREFERENCE_USE_STAIRS = "preference_stairs"
private const val PREFERENCE_USE_ELEVATORS = "preference_elevators"
private const val PREFERENCE_USE_VIBRATION = "preference_vibration"
private const val PREFERENCE_USE_VOICE_GUIDANCE = "preference_voice_guidance"

class Preferences(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getPrivacyPolicyAccepted(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCE_PRIVACY_POLICY_ACCEPTED, false)
    }

    fun setPrivacyPolicyAccepted(value: Boolean) {
        sharedPreferences.edit().putBoolean(PREFERENCE_PRIVACY_POLICY_ACCEPTED, value).apply()
    }

    /* ------------------------------------------------------------------------------------------ */
    /* Preference screen accessors */

    fun getUseAccessibleRoutes(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCE_USE_ACCESSIBLE_ROUTES, false)
    }

    fun getUseStairs(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCE_USE_STAIRS, true)
    }

    fun getUseElevators(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCE_USE_ELEVATORS, true)
    }

    fun getUseVoiceGuidance(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCE_USE_VOICE_GUIDANCE, true)
    }

    fun getUseVibration(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCE_USE_VIBRATION, false)
    }
}