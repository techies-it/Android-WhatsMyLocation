package it.techies.whatsmylocation.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.databinding.FragmentDashboardBinding

/**
 * A simple [Fragment] subclass.
 */
class DashboardFragment : Fragment() {

    private lateinit var mBinding: FragmentDashboardBinding

    private lateinit var mViewModel: DashboardViewModel
    private lateinit var mViewModelFactory: DashboardViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dashboard,
            container,
            false
        )

        mViewModelFactory = DashboardViewModelFactory(activity)

        // Initialise view model
        mViewModel = ViewModelProvider(this, mViewModelFactory)
            .get(DashboardViewModel::class.java)

        // Set the view model for data binding - this allows the bound layout access
        // to all the data in the ViewModel
        mBinding.dashboardViewModel = mViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        mBinding.lifecycleOwner = viewLifecycleOwner

        mViewModel.eventTrackerStop.observe(viewLifecycleOwner,
            Observer { hasStoped -> if (hasStoped) stopTracker() })

        mViewModel.showLoader.observe(viewLifecycleOwner, Observer { loaderShow ->
            if (loaderShow) {
                mBinding.progressLoader.visibility = View.VISIBLE
            } else {
                mBinding.progressLoader.visibility = View.GONE
            }
        })

        return mBinding.root
    }

    private fun stopTracker() {
        Toast.makeText(activity, "Tracker Stopped!", Toast.LENGTH_SHORT).show()
        mViewModel.onStopTrackingFinish()
        findNavController().navigate(R.id.action_dashboardFragment_to_thanksFragment)
    }

}
