package it.techies.whatsmylocation.dashboard

import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient

class DashboardViewModelFactory
    :ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)){
            return  DashboardViewModel() as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}