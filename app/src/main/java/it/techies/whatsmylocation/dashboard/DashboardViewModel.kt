package it.techies.whatsmylocation.dashboard

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class DashboardViewModel(context: Context): ViewModel() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val _location = MutableLiveData<String>()
    val location: LiveData<String>
        get() = _location

    init {
        _location.value = "Searching..."

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        getLocation()

    }

    private fun getLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                _location.value = location?.latitude.toString() + " : " + location?.longitude.toString()
            }
    }
}