package ai.andromeda.griffin

import ai.andromeda.griffin.background.MqttConnectionManagerService
import ai.andromeda.griffin.config.Config.RESTART_REQUEST_KEY
import ai.andromeda.griffin.scanner.ScannerFragment
import ai.andromeda.griffin.util.showMessage
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //------------------------- NAVIGATION DRAWER --------------------------//
        val navController = this.findNavController(R.id.navHostFragment)
        NavigationUI.setupWithNavController(navView, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        //--------------- ASK PERMISSIONS ----------------//
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        startMqttService()

        //------------------- WHEN ACCESSED FROM NOTIFICATION ----------------//
        val isRestartRequested: Boolean = intent.getBooleanExtra(RESTART_REQUEST_KEY, false)
        if (isRestartRequested) { startMqttService() }
    }

    //---------------- NAV DRAWER SETUP --------------//
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    //-------------------- START MQTT SERVICE -------------------//
    private fun startMqttService() {
        val intent = Intent(
            this@MainActivity, MqttConnectionManagerService::class.java
        )
        startService(intent)
    }

    //------------------------- PERMISSIONS ----------------------//
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == ScannerFragment.REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                showMessage(applicationContext, "PERMISSIONS NOT GRANTED")
                finish()
            }
        }
    }
}