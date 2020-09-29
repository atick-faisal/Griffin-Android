package ai.andromeda.griffin

import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config.LOG_TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(topAppBar)

        //startMqttService()
    }

    private fun startMqttService() {
        Log.i(LOG_TAG, "SERVICE STARTING...")
        val intent = Intent(
            this@MainActivity, MqttConnectionManagerService::class.java
        )
        startService(intent)
    }
}