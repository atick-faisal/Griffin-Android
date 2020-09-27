package ai.andromeda.griffin.config

object Config {
    const val LOG_TAG = "GRIFFIN_LOG"
    const val LOCAL_BROKER_IP = "tcp://192.168.0.105:1883"
    const val SUBSCRIPTION_TOPIC = "ESP_CONFIG"
    const val PUBLISH_TOPIC = "ESP_CONFIG"
    const val GLOBAL_BROKER_IP = "tcp://broker.hivemq.com:1883"
    const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    const val ID_LENGTH = 8
    const val PREFERENCE_NAME = "APP_SETTINGS"
}