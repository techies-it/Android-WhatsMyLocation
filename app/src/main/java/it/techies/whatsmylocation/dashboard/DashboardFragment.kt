package it.techies.whatsmylocation.dashboard

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import it.techies.whatsmylocation.Constants
import it.techies.whatsmylocation.MainActivity
import it.techies.whatsmylocation.MainActivity.Companion.SCREEN_DASHBOARD
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.api.RequestAndErrorInit
import it.techies.whatsmylocation.api.VolleySingleton
import it.techies.whatsmylocation.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(), RequestAndErrorInit {

    private lateinit var mBinding: FragmentDashboardBinding
    private lateinit var mContext:Context
    private lateinit var mViewModel: DashboardViewModel
    private lateinit var mViewModelFactory: DashboardViewModelFactory
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    // Timer to get location update
    private var timer: CountDownTimer? = null
    private var manager:LocationManager? =null
    // to update location
    private var locationUpdated: Location? = null

    companion object {
        const val REQUEST_CHECK_SETTINGS = 1001
        var isFirstTime = false
    }

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

        MainActivity.onScreen = SCREEN_DASHBOARD
        mViewModelFactory = DashboardViewModelFactory()
        // Initialise view model
        mViewModel = ViewModelProvider(this, mViewModelFactory)[DashboardViewModel::class.java]

        // Set the view model for data binding - this allows the bound layout access
        // to all the data in the ViewModel
        mBinding.dashboardViewModel = mViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        mBinding.lifecycleOwner = viewLifecycleOwner
        
        mContext = requireActivity()

        manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
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
        mBinding.adViewDashboard.loadAd(adRequest)


        mViewModel.eventTrackerStop.observe(viewLifecycleOwner,
            Observer { hasStoped -> if (hasStoped) stopTracker() })

        mViewModel.showLoader.observe(viewLifecycleOwner, Observer { loaderShow ->
            if (loaderShow) {
              mBinding.progressLoader.visibility = View.VISIBLE
            } else {
              mBinding.progressLoader.visibility = View.GONE
            }
        })

        mViewModel.currentTimeString.observe(viewLifecycleOwner) {
            mBinding.tvTime.text = it
        }
        mViewModel.address.observe(viewLifecycleOwner) {
            mBinding.tvUserLocation.text = it
        }

        mViewModel.errorMessage.observe(viewLifecycleOwner) { ask ->
            Toast.makeText(mContext, ask, Toast.LENGTH_SHORT).show()
        }

        mViewModel.setAddress("Searching...")
        mBinding.adViewDashboard?.resume()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Handle the location result
                for (location in locationResult.locations) {
                    // Use the location object
                    locationUpdated = location
                }

                if (isFirstTime){
                    Log.d("Dash", "First Time : true")
                    mViewModel.setLocationValue(locationUpdated)
                    getReverseGeocode()
                    isFirstTime = false
                }
            }
        }

        checkLocationPermission()
        return mBinding.root
    }

    override fun onPause() {
        mBinding.adViewDashboard?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        mBinding.adViewDashboard?.destroy()
        super.onDestroy()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the location directly
                isFirstTime = true
                startTimer()
                enableLocationSettings()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain to the user why you need the permission
                showDialog()
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // To get device location on Permission is granted
            isFirstTime = true
            startTimer()
            enableLocationSettings()
        } else {
            // Permission is denied
            activity?.finish()
        }
    }

    private fun enableLocationSettings() {

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            // All location settings are satisfied
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            checkLocationPermission()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun startTimer(){
        // Timer to update device location after 2 minutes
        timer = object : CountDownTimer(Constants.COUNTDOWN_TIME, Constants.ONE_SECOND) {
            override fun onFinish() {
                //getLocation() // Get device location
                mViewModel.setLoading(Constants.LOADER_SHOW_YES)
                mViewModel.setAddress("Searching...")
                if (ContextCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                ) {
                    mViewModel.setLocationValue(locationUpdated)
                    getReverseGeocode()
                } else {
                    // Permission is not granted
                    // Show dialog to get location permission
                    Log.d("Dash", "Permission error")
                    /*_location.value = null
                    _address.value = "Searching..."
                    _getLocationPermission.value = true*/

                    mViewModel.setLocationValue(null)
                    mViewModel.setAddress("Searching...")
                }

                // restart timer
                start()

            }

            override fun onTick(millisUntilFinished: Long) {
                /**
                 * The millisUntilFinished is the amount of time until the
                 * timer is finished in milliseconds. Convert millisUntilFinished
                 * to seconds and assign it to _currentTimeString.
                 */
                //_currentTimeString.value =
                // Update timer value
                mViewModel.setCurrentTimeString((millisUntilFinished / Constants.ONE_SECOND).toString())
            }

        }.start()
    }

    private fun stopTracker() {
        timer?.cancel()
        mViewModel.setAddress("Searching...")
        locationUpdated = null
        Toast.makeText(activity, "Tracker Stopped!", Toast.LENGTH_SHORT).show()
        mViewModel.onStopTrackingFinish()
        findNavController().navigate(R.id.action_dashboardFragment_to_thanksFragment)
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

    /**
     *  To make API call to get reverse
     *  geocode of location of device
     */
    fun getReverseGeocode() {
        Log.d("Dash", "getReverseGeocode")
        val lat = mViewModel.location.value?.latitude.toString()
        val long = mViewModel.location.value?.longitude.toString()
        val json = object : JsonObjectRequest(Request.Method.GET, mViewModel.getURL(lat, long), null,
            Response.Listener { response ->
                Log.d("TAG123", "Response: $response")
                onSuccess(mViewModel.getAddress(response))
            },
            Response.ErrorListener { error ->
                onError(error)
            }
        ) { /* Do Nothing for header*/ }

        // To stop from resend volley request
        json.retryPolicy = VolleySingleton getUploadRetryStrat true
        addRequest(req = json)
    }

    /**
     *  Call when success of volley request
     *  @param address - address of location
     *  after reverse geocode
     */
    override fun onSuccess(address: String?) {
        mViewModel.setLoading(Constants.LOADER_SHOW_NO)
        if (address != null) {
            mViewModel.setAddress(address)
        } else {
            mViewModel.setAddress("Searching...")
        }
    }

    /**
     * Called when an error occurs during a volley request
     * @param error the error which has occured in1 a volley request
     */
    override fun onError(error: VolleyError) {
        mViewModel.setLoading(Constants.LOADER_SHOW_NO)
        mViewModel.setAddress("Searching...")
        if (error is TimeoutError || error is NoConnectionError || error is NetworkError) {
            mViewModel.setErrorMessage("Something went wrong with network!")
            Log.d("Dash", "Something went wrong with network!")
        } else if (error.networkResponse.statusCode == 401) {
            mViewModel.setErrorMessage("Something went wrong!")
            Log.d("Dash", "Error : 401")
        } else {
            mViewModel.setErrorMessage(error.message.toString())
            Log.d("Dash", error.message.toString())
        }

        isFirstTime = true
        enableLocationSettings() // Get device location
    }

    /**
     * Called when the reqeust has to be added to the queue
     * @param req the volley request to be added to our queue
     */
    override fun <T> addRequest(req: Request<T>) {
        VolleySingleton.getInstance(mContext).addToRequestQueue(req)
    }

}

// https://github.com/nntuyen/mkloader
