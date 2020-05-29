package it.techies.whatsmylocation.dashboard

import android.content.Context
import android.location.Location
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import it.techies.whatsmylocation.Constants.Companion.COUNTDOWN_TIME
import it.techies.whatsmylocation.Constants.Companion.DONE
import it.techies.whatsmylocation.Constants.Companion.LOADER_SHOW_NO
import it.techies.whatsmylocation.Constants.Companion.LOADER_SHOW_YES
import it.techies.whatsmylocation.Constants.Companion.ONE_SECOND
import it.techies.whatsmylocation.Constants.Companion.TRACKER_STOP_NO
import it.techies.whatsmylocation.Constants.Companion.TRACKER_STOP_YES
import it.techies.whatsmylocation.api.RequestAndErrorInit
import it.techies.whatsmylocation.api.VolleySingleton
import org.json.JSONObject


class DashboardViewModel(context: Context) : ViewModel(), RequestAndErrorInit {

    private var mContext: Context = context

    // To get device location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Timer to get location update
    private val timer: CountDownTimer

    // The String version of the current time
    private val _currentTimeString = MutableLiveData<String>()
    val currentTimeString: LiveData<String>
    get() = _currentTimeString

    // To set and get location address in string
    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address

    // To set and get device location
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location

    // To stop location tracker
    private val _eventTrackerStop = MutableLiveData<Boolean>()
    val eventTrackerStop: LiveData<Boolean>
        get() = _eventTrackerStop

    // To handel progress loader
    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean>
        get() = _showLoader

    init {

        // To get device current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        getLocation()

        // Timer to update device location after 2 minutes
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onFinish() {
                getLocation() // Get device location
                start() // restart timer
            }

            override fun onTick(millisUntilFinished: Long) {
                /**
                 * The millisUntilFinished is the amount of time until the
                 * timer is finished in milliseconds. Convert millisUntilFinished
                 * to seconds and assign it to _currentTimeString.
                 */
                _currentTimeString.value = (millisUntilFinished / ONE_SECOND).toString() // Update timer value
            }

        }.start()

    }

    override fun onCleared() {
        super.onCleared()
        // Cancel the timer to avoid memory leaks
        onStopTrackingFinish()
    }

    /**
     *  To get device current device location
     */
    private fun getLocation() {
        _showLoader.value = LOADER_SHOW_YES
        _address.value = "Searching..."
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    _location.value = location
                    getReverseGeocode()
                }else{
                    onLocationError()
                }
            }.addOnFailureListener {
                onLocationError()
            }
    }

    /**
     * To handle location reading error
     */
    private fun onLocationError() {
        _showLoader.value = LOADER_SHOW_NO
        _address.value = "Searching..."
        getLocation()
    }

    /**
     *  To trigger event tracker stop
     */
    fun onStopTracking() {
        _eventTrackerStop.value = TRACKER_STOP_YES
    }

    /**
     *  To reset tracker event value,
     *  reset current time and stop
     *  timer
     */
    fun onStopTrackingFinish() {
        timer.cancel()
        _location.value = null
        _address.value = "Searching..."
        _eventTrackerStop.value = TRACKER_STOP_NO
    }

    /**
     *  Call when success of volley request
     *  @param address - address of location
     *  after reverse geocode
     */
    override fun onSuccess(address: String?) {
        _showLoader.value = LOADER_SHOW_NO

        if (address != null) {
            _address.value = address
        } else {
            _address.value = "Searching..."
        }

    }

    /**
     * Called when an error occurs during a volley request
     * @param error the error which has occured in1 a volley request
     */
    override fun onError(error: VolleyError) {
        _showLoader.value = LOADER_SHOW_NO
        _address.value = "Searching..."
        if (error is TimeoutError || error is NoConnectionError || error is NetworkError) {
            Toast.makeText(mContext, "Something went wrong with network!", Toast.LENGTH_SHORT).show()
        } else if (error.networkResponse.statusCode == 401){
            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
        }

        getLocation() // Get device location

    }

    /**
     * Called when the reqeust has to be added to the queue
     * @param req the volley request to be added to our queue
     */
    override fun <T> addRequest(req: Request<T>) {
        VolleySingleton.getInstance(mContext).addToRequestQueue(req)
    }

    /**
     *  To make API call to get reverse
     *  geocode of location of device
     */
    private fun getReverseGeocode() {
        val lat = location.value?.latitude.toString()
        val long = location.value?.longitude.toString()
        val json = object : JsonObjectRequest(Request.Method.GET, getURL(lat, long), null,
            Response.Listener { response ->
                onSuccess(getAddress(response))
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
     *  To prepare url to get address from lat long
     *  @param lat - latitude
     *  @param long - longitude
     */
    private fun getURL(lat: String, long: String): String {
        return "https://reverse.geocoder.ls.hereapi.com/6.2/reversegeocode.json?" +
                "prox=$lat%2C$long%2C100&mode=retrieveAddresses&maxresults=1" +
                "&gen=9&apiKey=6yIzHUDni2r88yfbkuPCCDSW_EII_WdUUCZhxpS_3-I"  /* key is from ig@techies.it*/
    }

    /**
     *  Get address from response to show to user
     *  @param response - reverse geocode API response
     */
    private fun getAddress(response: JSONObject): String {
       return response.getJSONObject("Response")
            .getJSONArray("View")
            .getJSONObject(0)
            .getJSONArray("Result")
            .getJSONObject(0)
            .getJSONObject("Location")
            .getJSONObject("Address")
           .optString("Label")

//        return data.optString("HouseNumber") + ", " +
//                data.optString("Street") + ", " +
//                data.optString("District") + ", " +
//                data.optString("City") + ", " +
//                data.optString("County") + ", " +
//                data.optString("State") + ", " +
//                data.optString("Country")
    }
}