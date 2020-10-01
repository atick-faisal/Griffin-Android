package ai.andromeda.griffin.util

import ai.andromeda.griffin.config.Config
import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun getString(context: Context, key: String): String? {
        return getSharedPreferences(context).getString(key, null)
    }

    fun getLong(context: Context, key: String): Long {
        return getSharedPreferences(context).getLong(key, 0L)
    }

    fun putString(context: Context, key: String, newValue: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(key, newValue)
        editor.apply()
    }

    fun putLong(context: Context, key: String, newValue: Long) {
        val editor = getSharedPreferences(context).edit()
        editor.putLong(key, newValue)
        editor.apply()
    }
}