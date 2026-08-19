package com.smaran.app.profile

import android.content.Context

class ProfilePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("smaran_profile", Context.MODE_PRIVATE)

    var name: String
        get() = prefs.getString(KEY_NAME, "Akash") ?: "Akash"
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var age: String
        get() = prefs.getString(KEY_AGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AGE, value).apply()

    var profileImageUri: String
        get() = prefs.getString(KEY_IMAGE_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IMAGE_URI, value).apply()

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_AGE = "age"
        private const val KEY_IMAGE_URI = "profile_image_uri"
    }
}
