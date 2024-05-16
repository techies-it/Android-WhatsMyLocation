package it.techies.whatsmylocation.dashboard

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import it.techies.whatsmylocation.Constants
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.databinding.FragmentDashboardBinding
import it.techies.whatsmylocation.welcome.WelcomeFragment

/**
 * A simple [Fragment] subclass.
 */
class DashboardFragment : Fragment() {

    private lateinit var mBinding: FragmentDashboardBinding

    private lateinit var mViewModel: DashboardViewModel
    private lateinit var mViewModelFactory: DashboardViewModelFactory
    private var mAdView: AdView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dashboard,
            container,
            false
        )

        MobileAds.initialize(activity) {}
        mAdView = mBinding.root.findViewById(R.id.adViewDashboard)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mViewModelFactory = DashboardViewModelFactory(activity, manager,fusedLocationClient)

        // Initialise view model
        mViewModel = ViewModelProvider(this, mViewModelFactory)
            .get(DashboardViewModel::class.java)

        // Set the view model for data binding - this allows the bound layout access
        // to all the data in the ViewModel
        mBinding.dashboardViewModel = mViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        mBinding.lifecycleOwner = viewLifecycleOwner

        mViewModel.eventTrackerStop.observe(viewLifecycleOwner,
            Observer { hasStoped -> if (hasStoped) stopTracker() })

        mViewModel.showLoader.observe(viewLifecycleOwner, Observer { loaderShow ->
            if (loaderShow) {
                mBinding.progressLoader.visibility = View.VISIBLE
            } else {
                mBinding.progressLoader.visibility = View.GONE
            }
        })

        mViewModel.getLocationPermission.observe(viewLifecycleOwner, Observer { ask ->
            if (ask) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.MY_PERMISSIONS_ACCESS_FINE_LOCATION
                )
            }
        })

        return mBinding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.MY_PERMISSIONS_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // permission was granted, yay! Do the
                    enableLocation()

                } else {
                    // permission denied, boo! Disable the
                    showDialog()
                }
                return
            }
        }
    }

    private fun getDeviceLocation() {
        mViewModel.onLocationPermissionGrant()
    }

    private fun stopTracker() {
        Toast.makeText(activity, "Tracker Stopped!", Toast.LENGTH_SHORT).show()
        mViewModel.onStopTrackingFinish()
        findNavController().navigate(R.id.action_dashboardFragment_to_thanksFragment)
    }

    override fun onPause() {
        if (mAdView != null) {
            mAdView?.pause()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (mAdView != null) {
            mAdView?.resume()
        }
    }

    override fun onDestroy() {
        if (mAdView != null) {
            mAdView?.destroy()
        }
        super.onDestroy()
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
                    getDeviceLocation()
                }
                .addOnFailureListener(this.requireActivity()) { ex ->
                    if (ex is ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            ex.startResolutionForResult(
                                this.requireActivity(),
                                Constants.REQUEST_CODE_CHECK_SETTINGS
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {

                        }
                    }
                }

        } else {
            getDeviceLocation()
        }
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
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.MY_PERMISSIONS_ACCESS_FINE_LOCATION
                )
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
