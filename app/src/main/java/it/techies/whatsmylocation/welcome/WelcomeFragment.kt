package it.techies.whatsmylocation.welcome

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import it.techies.whatsmylocation.MainActivity.Companion.SCREEN_WELCOME
import it.techies.whatsmylocation.MainActivity.Companion.onScreen
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.dashboard.DashboardFragment
import it.techies.whatsmylocation.databinding.FragmentWelcomeBinding


// https://stackoverflow.com/questions/40760625/how-to-check-permission-in-fragment

/**
 * [Fragment] to welcome user and get location permission.
 */
class WelcomeFragment : Fragment() {

    private lateinit var mBinder: FragmentWelcomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        mBinder = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_welcome,
            container,
            false
        )

        onScreen = SCREEN_WELCOME
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).apply {
            setMinUpdateIntervalMillis(1000L)
        }.build()


        // Initialize the Mobile Ads SDK
        MobileAds.initialize(requireActivity()) {}

        // Create an ad request and load the ad
        val adRequest = AdRequest.Builder().build()
        mBinder.adViewWelcome.loadAd(adRequest)


        mBinder.btStart.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_dashboardFragment)
        }

        checkLocationPermission()
        return mBinder.root
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the location directly
                enableLocationSettings()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain to the user why you need the permission
                showDialog()
            }
            else -> {
                // Request the location permission
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // To get device location on Permission is granted
            enableLocationSettings()
        } else {
            // Permission is denied
            activity?.finish()
        }
    }


    private fun enableLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            // All location settings are satisfied
            enableStartButton()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog
                    exception.startResolutionForResult(requireActivity(),
                        DashboardFragment.REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }

    }

    /**
     *  To enable Start button after getting
     *  location permission
     */
    private fun enableStartButton() {
        mBinder.btStart.isClickable = true
        mBinder.btStart.isEnabled = true
    }

    /**
     *  Alert dialog to show reason
     *  for asking location permission
     */
    private fun showDialog() {
        AlertDialog.Builder(activity)
            .setMessage("App require location permission to get device location.")
            .setCancelable(false)
            .setPositiveButton(
                "Ok"
            ) { dialog, id -> //put your code that needed to be executed when okay is clicked
                dialog.cancel()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, id ->
                dialog.cancel()
                activity?.finish()
            }
            .create()
            .show()
    }

}
