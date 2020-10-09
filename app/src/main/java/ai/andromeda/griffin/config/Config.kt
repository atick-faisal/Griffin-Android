package ai.andromeda.griffin.config

import android.content.ComponentName
import android.content.Intent

object Config {
    const val LOG_TAG = "GRIFFIN_LOG"
    const val LOCAL_BROKER_IP = "tcp://192.168.4.1:1883"
    const val SUBSCRIPTION_TOPIC = "Pub/Griffin0"
    const val PUBLISH_TOPIC = "Sub/Griffin0"
    const val GLOBAL_BROKER_IP = "tcp://broker.hivemq.com:1883"
    const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    const val ID_LENGTH = 8
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
    const val AUTO_START_KEY = "AUTO_START"
    const val RETRY_INTERVAL = 5000L

    //------------- POWER MANAGER INTENTS --------------//
    val POWER_MANAGER_INTENTS = arrayOf(
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.htc.pitroad",
                "com.htc.pitroad.landingpage.activity.LandingPageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.MainActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.transsion.phonemanager",
                "com.itel.autobootmanager.activity.AutoBootMgrActivity"
            )
        )
    )
}