package it.techies.whatsmylocation.welcome

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.databinding.FragmentWelcomeBinding

/**
 * A simple [Fragment] subclass.
 */
class WelcomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate view and obtain an instance of the binding class
        val mBinder: FragmentWelcomeBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_welcome,
            container,
            false
        )



        mBinder.btStart.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_dashboardFragment)
        }

        return mBinder.root
    }



}
