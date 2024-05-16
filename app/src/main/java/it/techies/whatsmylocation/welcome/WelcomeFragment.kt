package it.techies.whatsmylocation.welcome

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import it.techies.whatsmylocation.Constants.Companion.MY_PERMISSIONS_ACCESS_FINE_LOCATION
import it.techies.whatsmylocation.Constants.Companion.REQUEST_CODE_CHECK_SETTINGS
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.databinding.FragmentWelcomeBinding


// https://stackoverflow.com/questions/40760625/how-to-check-permission-in-fragment

/**
 * [Fragment] to welcome user and get location permission.
 */
class WelcomeFragment : Fragment() {

    private lateinit var mBinder: FragmentWelcomeBinding
    private var mAdView: AdView? = null

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

        MobileAds.initialize(activity) {}
        mAdView = mBinder.root.findViewById(R.id.adViewWelcome)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)


        mBinder.btStart.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_dashboardFragment)
        }

        return mBinder.root
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out kotlin.String>,
        grantResults: IntArray
    ): Unit {
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    enableLocation()
                } else {
                    // permission denied, boo! Disable the
                    showDialog()
                }
                return
            }
        }
    }


    private fun enableLocation() {
        // permission was granted, yay! Do the
        val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val LOCATION_UPDATE_INTERVAL = 2000L
            val LOCATION_UPDATE_FASTEST_INTERVAL = 1000L

            val locationRequest = LocationRequest.create()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            LocationServices
                .getSettingsClient(this.requireActivity())
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this.requireActivity()) { response: LocationSettingsResponse? ->
                    enableStartButton()
                }
                .addOnFailureListener(this.requireActivity()) { ex ->
                    if (ex is ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            ex.startResolutionForResult(
                                this.requireActivity(),
                                REQUEST_CODE_CHECK_SETTINGS
                            )
                        } catch (sendEx: SendIntentException) {

                        }
                    }
                }

        } else {
            enableStartButton()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        if (REQUEST_CODE_CHECK_SETTINGS == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                Toast.makeText(
                    activity, "You need to enable location in setting for this app",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else{
            Toast.makeText(
                activity, "You need to enable location in setting for this app",
                Toast.LENGTH_LONG
            ).show()
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()

        // To get location permission from user
        val permissionCheck = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Show dialog to get location permission
            requestLocationPermission()
        } else {
            // Permission is granted
            enableLocation()
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
     *  To show dialog to get device
     *  location permission
     */
    fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_ACCESS_FINE_LOCATION
        )
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
                requestLocationPermission()
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
