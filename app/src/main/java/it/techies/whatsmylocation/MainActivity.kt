package it.techies.whatsmylocation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import it.techies.whatsmylocation.dashboard.DashboardFragment
import it.techies.whatsmylocation.dashboard.DashboardFragment.Companion.REQUEST_CHECK_SETTINGS

class MainActivity : AppCompatActivity() {

    companion object {
        val SCREEN_WELCOME = "Welcome"
        val SCREEN_DASHBOARD = "Dashboard"
        var onScreen = SCREEN_WELCOME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    if (onScreen == SCREEN_DASHBOARD) {
                        // The user agreed to make required location settings changes
                        (supportFragmentManager.findFragmentById(R.id.dashboardFragment)
                                as? DashboardFragment)?.startLocationUpdates()
                    }
                }

                Activity.RESULT_CANCELED -> {
                    // The user chose not to make required location settings changes
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
        }
    }
}
