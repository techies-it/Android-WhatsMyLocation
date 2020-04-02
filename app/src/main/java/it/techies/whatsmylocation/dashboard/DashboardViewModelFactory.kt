package it.techies.whatsmylocation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DashboardViewModelFactory(private val context: Context?):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)){
            return context?.let { DashboardViewModel(it) } as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}