package ai.andromeda.griffin.config

object Config {
    const val LOG_TAG = "GRIFFIN_LOG"
    const val LOCAL_BROKER_IP = "tcp://192.168.0.105:1883" // TODO LOCAL IP
    const val SUBSCRIPTION_TOPIC = "Pub/Griffin0"
    const val PUBLISH_TOPIC = "Sub/Griffin0"
    const val GLOBAL_BROKER_IP = "tcp://broker.hivemq.com:1883"
    const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    const val ID_LENGTH = 16
    const val PREFERENCE_NAME = "APP_SETTINGS"
    const val PERSISTENT_NOTIFICATION_TITLE = "Griffin Service"
    const val PERSISTENT_NOTIFICATION_ID = 10010
    const val ALERT_NOTIFICATION_TITLE = "Security Alert!"
    const val ALERT_NOTIFICATION_ID = 191919
    const val PERSISTENT_CHANNEL_ID = "MQTT_SERVICE_ID"
    const val ALERT_CHANNEL_ID = "ALERT_SERVICE_ID"
    const val WORK_NAME = "GRIFFIN_MQTT_SERVICE_WORK"
    const val WORK_REPEAT_PERIOD = 15L
    const val DEVICE_ID_KEY = "GRIFFIN_DEVICE_IDS"
    const val RESTART_REQUEST_KEY = "RESTART_MQTT"
    const val WAIT_FOR_FEEDBACK = true
}