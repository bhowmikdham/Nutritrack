package com.fit2081.nutritrack.data

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val PREFS_NAME     = "auth_prefs"
    private const val KEY_USER_ID    = "key_user_id"

    private lateinit var prefs: SharedPreferences

    /** Must be called once (e.g. in Application or MainActivity.onCreate) */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Persist the signed-in user */
    fun signIn(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    /** Clear the signed-in user */
    fun signOut() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    /** Return the current userId, or null if none */
    fun currentUserId(): String? =
        if (::prefs.isInitialized) prefs.getString(KEY_USER_ID, null)
        else null
}
