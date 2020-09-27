package ai.andromeda.griffin

import ai.andromeda.griffin.config.Config.ALLOWED_CHARACTERS
import ai.andromeda.griffin.config.Config.ID_LENGTH
import ai.andromeda.griffin.config.Config.PREFERENCE_NAME
import android.content.Context
import android.content.SharedPreferences
import java.util.*

object SharedPreferencesManager {

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun getString(context: Context, key: String): String? {
        return getSharedPreferences(context).getString(key, null)
    }

    fun putString(context: Context, key: String, newValue: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(key, newValue)
        editor.apply()
    }
}

fun generateDeviceId(): String {
    val random = Random()
    val sb = StringBuilder(ID_LENGTH)
    for (i in 0 until ID_LENGTH)
        sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
    return sb.toString()
}