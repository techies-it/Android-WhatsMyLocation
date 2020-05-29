package it.techies.whatsmylocation.thankyou

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import it.techies.whatsmylocation.R
import it.techies.whatsmylocation.databinding.FragmentThanksBinding

/**
 * A simple [Fragment] subclass.
 */
class ThanksFragment : Fragment() {

    private lateinit var mViewModel: ThanksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class.
        val mBinding: FragmentThanksBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_thanks,
                container,
                false
            )

        // Initialise view model
        mViewModel = ViewModelProvider(this)
            .get(ThanksViewModel::class.java)

        // Set the view model for data binding - this allows the bound layout access
        // to all the data in the ViewModel
        mBinding.thanksViewModel = mViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        mBinding.lifecycleOwner = viewLifecycleOwner

        mViewModel.eventRestartTracking.observe(viewLifecycleOwner,
            Observer { restartTracker ->
                if (restartTracker){
                    findNavController().navigate(R.id.action_thanksFragment_to_dashboardFragment,
                        null,
                        NavOptions.Builder().setEnterAnim(R.anim.slide_in_right_pop)
                            .setExitAnim(R.anim.slide_out_left_pop).build())
                    mViewModel.onRestartTrackerFinish()
                }
            })

        return mBinding.root
    }

}
