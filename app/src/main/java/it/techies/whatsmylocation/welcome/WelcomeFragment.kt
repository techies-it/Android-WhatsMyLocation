package it.techies.whatsmylocation.welcome

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.techies.whatsmylocation.Constants.Companion.MY_PERMISSIONS_ACCESS_FINE_LOCATION
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.databinding.FragmentWelcomeBinding

// https://stackoverflow.com/questions/40760625/how-to-check-permission-in-fragment

/**
 * [Fragment] to welcome user and get location permission.
 */
class WelcomeFragment : Fragment() {

    private lateinit var mBinder: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate view and obtain an instance of the binding class
        mBinder = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_welcome,
            container,
            false
        )

        // To get location permission from user
        val permissionCheck = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Show dialog to get location permission
            requestLocationPermission()
        } else {
            // Permission is granted
            enableStartButton()
        }


        mBinder.btStart.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_dashboardFragment)
        }

        return mBinder.root
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out kotlin.String>,
        grantResults: IntArray
    ): Unit {
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // permission was granted, yay! Do the
                    enableStartButton()
                } else {
                    // permission denied, boo! Disable the
                    showDialog()
                }
                return
            }
        }
    }

    /**
     *  To enable Start button after getting
     *  location permission
     */
    private fun enableStartButton() {
        mBinder.btStart.isClickable = true
        mBinder.btStart.isEnabled = true
    }

    /**
     *  To show dialog to get device
     *  location permission
     */
    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_ACCESS_FINE_LOCATION
        )
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
                requestLocationPermission()
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
