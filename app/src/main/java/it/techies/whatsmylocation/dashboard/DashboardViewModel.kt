package it.techies.whatsmylocation.dashboard

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.techies.whatsmylocation.Constants.Companion.TRACKER_STOP_NO
import it.techies.whatsmylocation.Constants.Companion.TRACKER_STOP_YES
import org.json.JSONObject


class DashboardViewModel : ViewModel() {

    // The String version of the current time
    private val _currentTimeString = MutableLiveData<String>()
    val currentTimeString: LiveData<String>
        get() = _currentTimeString

    // To set and get location address in string
    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?>
        get() = _address

    // To set and get device location
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?>
        get() = _location

    // To stop location tracker
    private val _eventTrackerStop = MutableLiveData<Boolean>()
    val eventTrackerStop: LiveData<Boolean>
        get() = _eventTrackerStop

    // To handel progress loader
    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean>
        get() = _showLoader

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    fun setLocationValue(location: Location?){
        _location.value = location
    }

    fun setLoading(isLoading:Boolean){
        _showLoader.value = isLoading
    }

    fun setAddress(address:String){
        _address.value = address
    }

    fun setCurrentTimeString(currentTime:String){
        _currentTimeString.value = currentTime
    }

    fun  setErrorMessage(message: String){
        _errorMessage.value = message
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel the timer to avoid memory leaks
        onStopTrackingFinish()
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
        _location.value = null
        _address.value = "Searching..."
        _eventTrackerStop.value = TRACKER_STOP_NO
    }

    /**
     *  To prepare url to get address from lat long
     *  @param lat - latitude
     *  @param long - longitude
     */
    fun getURL(lat: String, long: String): String {
        /* key is from techiesindiainc@gmail.com/Taran121# */
        val url = "https://revgeocode.search.hereapi.com/v1/revgeocode?" +
                "apiKey=8BEdpmfmXOc7rk6YTbQFNhWtWzsHOKOiROeCc-l-Mvc&at=$lat,$long,250"
        Log.d("TAGUrl", "getURL: $url")
        return url
    }

    /**
     *  Get address from response to show to user
     *  @param response - reverse geocode API response
     */
    fun getAddress(response: JSONObject): String {

        return response.getJSONArray("items")
            .getJSONObject(0)
            .getJSONObject("address")
            .optString("label")
    }

    /* Response : {"items":[{"title":"1600 Amphitheatre Pkwy, Mountain View, CA 94043-1351, United States","id":"here:af:streetsection:4bOPaCDlMrE0bNgUMF9fFA:CggIBCDq7sq0AxABGgQxNjAw","resultType":"houseNumber","houseNumberType":"PA","address":{"label":"1600 Amphitheatre Pkwy, Mountain View, CA 94043-1351, United States","countryCode":"USA","countryName":"United States","stateCode":"CA","state":"California","county":"Santa Clara","city":"Mountain View","street":"Amphitheatre Pkwy","postalCode":"94043-1351","houseNumber":"1600"},"position":{"lat":37.42249,"lng":-122.08473},"access":[{"lat":37.42263,"lng":-122.08467}],"distance":84,"mapView":{"west":-122.09012,"south":37.42304,"east":-122.07795,"north":37.42396}}]}*/

}

// https://platform.here.com/admin/apps