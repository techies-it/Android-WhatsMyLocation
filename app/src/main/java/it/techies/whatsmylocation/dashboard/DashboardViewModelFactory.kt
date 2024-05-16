package it.techies.whatsmylocation.dashboard

import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient

class DashboardViewModelFactory(private val context: Context?,
                                private val manager: LocationManager?,
                                private val fusedLocationClient: FusedLocationProviderClient?)
    :ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)){
            return context?.let { manager?.let { it1 -> fusedLocationClient?.let { it2 ->
                DashboardViewModel(it, it1,
                    it2
                )
            } } } as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}