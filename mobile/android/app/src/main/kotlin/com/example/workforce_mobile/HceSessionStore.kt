package com.example.workforce_mobile

import android.content.Context
import android.util.Log

object HceSessionStore {
    const val CHANNEL = "flipper_auth/hce_session"

    private const val TAG = "FlipperAuthHceStore"
    private const val PREFS_NAME = "flipper_auth_hce"
    private const val KEY_ACTIVE = "active"
    private const val KEY_SESSION_ID = "sessionId"
    private const val KEY_USERNAME = "username"
    private const val KEY_ACTION = "action"
    private const val KEY_CHALLENGE = "challenge"
    private const val KEY_EXPIRES_AT = "expiresAt"

    fun save(
        context: Context,
        sessionId: Long,
        username: String,
        action: String,
        challenge: String,
        expiresAt: String
    ): Boolean {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ACTIVE, true)
            .putLong(KEY_SESSION_ID, sessionId)
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_ACTION, action.trim().uppercase())
            .putString(KEY_CHALLENGE, challenge.trim())
            .putString(KEY_EXPIRES_AT, expiresAt)
            .commit()

        Log.d(
            TAG,
            "Session saved=$saved sessionId=$sessionId username=${username.trim()} action=${action.trim().uppercase()}"
        )
        return saved
    }

    fun clear(context: Context) {
        val cleared = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        Log.d(TAG, "Session cleared=$cleared")
    }

    fun payload(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ACTIVE, false)) {
            return "NO_ACTIVE_SESSION"
        }

        val sessionId = prefs.getLong(KEY_SESSION_ID, -1L)
        val username = prefs.getString(KEY_USERNAME, null)
        val action = prefs.getString(KEY_ACTION, null)
        val challenge = prefs.getString(KEY_CHALLENGE, null)

        if (sessionId <= 0 || username.isNullOrBlank() ||
            action.isNullOrBlank() || challenge.isNullOrBlank()
        ) {
            return "NO_ACTIVE_SESSION"
        }

        return "AUTH|sessionId=$sessionId|username=$username|action=$action|challenge=$challenge"
    }
}
