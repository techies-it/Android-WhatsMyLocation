package it.techies.whatsmylocation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import it.techies.whatsmylocation.dashboard.DashboardFragment
import it.techies.whatsmylocation.dashboard.DashboardFragment.Companion.REQUEST_CHECK_SETTINGS

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    companion object {
        val SCREEN_WELCOME = "Welcome"
        val SCREEN_DASHBOARD = "Dashboard"
        var onScreen = SCREEN_WELCOME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set the navigation graph programmatically
        navController.setGraph(R.navigation.main_navigation)
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
                }
            }
        }
    }
}
