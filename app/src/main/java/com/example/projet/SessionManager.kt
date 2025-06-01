package com.example.projet

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "MyAppPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_ROLE = "userRole"
    
    // New keys for child settings
    private const val KEY_CHILD_AGE = "childAge"
    private const val KEY_DIFFICULTY_LEVEL = "difficultyLevel"
    private const val KEY_CHILD_NAME = "childName"
    private const val KEY_CURRENT_LOGIN = "currentLogin"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setLogin(context: Context, isLoggedIn: Boolean, role: String?) {
        val editor = getPrefs(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserRole(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ROLE, null)
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    // New methods for child settings
    fun setChildAge(context: Context, age: Int) {
        val editor = getPrefs(context).edit()
        editor.putInt(KEY_CHILD_AGE, age)
        editor.apply()
    }
    
    fun getChildAge(context: Context): Int {
        return getPrefs(context).getInt(KEY_CHILD_AGE, 8) // Default age 8
    }
    
    fun setDifficultyLevel(context: Context, level: Int) {
        val editor = getPrefs(context).edit()
        editor.putInt(KEY_DIFFICULTY_LEVEL, level)
        editor.apply()
    }
    
    fun getDifficultyLevel(context: Context): Int {
        return getPrefs(context).getInt(KEY_DIFFICULTY_LEVEL, 1) // Default difficulty 1 (easy)
    }
    
    fun setChildName(context: Context, name: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_CHILD_NAME, name)
        editor.apply()
    }
    
    fun getChildName(context: Context): String {
        return getPrefs(context).getString(KEY_CHILD_NAME, "Mon enfant") ?: "Mon enfant"
    }
    
    fun setCurrentLogin(context: Context, login: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_CURRENT_LOGIN, login)
        editor.apply()
    }
    
    fun getCurrentLogin(context: Context): String? {
        return getPrefs(context).getString(KEY_CURRENT_LOGIN, null)
    }
    
    // Method to set all child settings at once (useful for parent configuration)
    fun setChildSettings(context: Context, age: Int, difficultyLevel: Int, name: String = "") {
        val editor = getPrefs(context).edit()
        editor.putInt(KEY_CHILD_AGE, age)
        editor.putInt(KEY_DIFFICULTY_LEVEL, difficultyLevel)
        if (name.isNotEmpty()) {
            editor.putString(KEY_CHILD_NAME, name)
        }
        editor.apply()
    }
    
    // Method to get all child settings
    fun getChildSettings(context: Context): Map<String, Any> {
        val prefs = getPrefs(context)
        return mapOf(
            "age" to prefs.getInt(KEY_CHILD_AGE, 8),
            "difficultyLevel" to prefs.getInt(KEY_DIFFICULTY_LEVEL, 1),
            "name" to (prefs.getString(KEY_CHILD_NAME, "Mon enfant") ?: "Mon enfant")
        )
    }
    
    // Method to reset child settings to defaults
    fun resetChildSettings(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove(KEY_CHILD_AGE)
        editor.remove(KEY_DIFFICULTY_LEVEL)
        editor.remove(KEY_CHILD_NAME)
        editor.apply()
    }
}
