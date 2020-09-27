package ai.andromeda.griffin.util

import ai.andromeda.griffin.config.Config.ALLOWED_CHARACTERS
import ai.andromeda.griffin.config.Config.ID_LENGTH
import java.util.*

fun generateDeviceId(): String {
    val random = Random()
    val sb = StringBuilder(ID_LENGTH)
    for (i in 0 until ID_LENGTH)
        sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
    return sb.toString()
}