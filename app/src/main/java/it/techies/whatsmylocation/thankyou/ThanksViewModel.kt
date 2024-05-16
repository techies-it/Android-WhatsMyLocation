package it.techies.whatsmylocation.thankyou

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.techies.whatsmylocation.Constants.Companion.RESTART_TRACKER_NO
import it.techies.whatsmylocation.Constants.Companion.RESTART_TRACKER_YES

class ThanksViewModel : ViewModel() {

    // To handle restart tracker event
    private val _eventRestartTracking = MutableLiveData<Boolean>()
    val eventRestartTracking: LiveData<Boolean>
        get() = _eventRestartTracking

    fun onRestartTracker(){
        _eventRestartTracking.value = RESTART_TRACKER_YES
    }

    fun onRestartTrackerFinish(){
        _eventRestartTracking.value = RESTART_TRACKER_NO
    }
}