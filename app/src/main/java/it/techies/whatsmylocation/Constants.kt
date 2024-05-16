package it.techies.whatsmylocation

class Constants {
    companion object{
        // Request code for location permission
        const val MY_PERMISSIONS_ACCESS_FINE_LOCATION = 123

        // For location enable
        const val REQUEST_CODE_CHECK_SETTINGS = 10

        // Time when the game is over
        const val DONE = 0L

        // Countdown time interval
        const val ONE_SECOND = 1000L

        // Total time for the game
        val COUNTDOWN_TIME = 2*60*1000L

        // Key timer start/stop
        const val TRACKER_STOP_YES = true
        const val TRACKER_STOP_NO = false

        // Key progress loader
        const val LOADER_SHOW_YES = true
        const val LOADER_SHOW_NO = false

        // Key restart tracker
        const val RESTART_TRACKER_YES = true
        const val RESTART_TRACKER_NO = false

    }
}