package com.ajkerdeal.app.essential.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentDashboardBinding
import com.ajkerdeal.app.essential.ui.home.HomeActivity
import com.ajkerdeal.app.essential.utils.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding? = null
    private val viewModel: DashboardViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_dashboard, container, false)
        return FragmentDashboardBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding?.lifecycleOwner = viewLifecycleOwner

        viewModel.updateUserStatus().observe(viewLifecycleOwner, Observer { userStatus ->
            when(userStatus.userType) {
                0 -> {
                    binding?.button1?.visibility = View.VISIBLE
                    binding?.button2?.visibility = View.VISIBLE
                }
                1 -> {
                    binding?.button1?.visibility = View.VISIBLE
                    binding?.button2?.visibility = View.GONE
                }
                2 -> {
                    binding?.button1?.visibility = View.GONE
                    binding?.button2?.visibility = View.VISIBLE
                }
            }
        })

        userName?.text = SessionManager.userName
        userMobile?.text = SessionManager.mobile

        activeSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Timber.d("Checked")
                viewModel.updateUserStatus("true", 1)
            } else {
                viewModel.updateUserStatus("false", 1)
            }
        }

        binding?.button1?.setOnClickListener {
            findNavController().navigate(R.id.nav_action_dashboard_orderList)
        }

        binding?.button2?.setOnClickListener {
            findNavController().navigate(R.id.nav_action_dashboard_parcelList)
            //test()
        }
        binding?.logoutBtn?.setOnClickListener {
            (activity as HomeActivity).logout()
        }

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ViewState.ShowMessage -> {
                    requireContext().toast(state.message)
                }
                is ViewState.KeyboardState -> {
                    hideKeyboard()
                }
                is ViewState.ProgressState -> {

                    if (state.isShow) {
                        binding?.progressBar?.visibility = View.VISIBLE
                    } else {
                        binding?.progressBar?.visibility = View.GONE
                    }

                }
            }
        })

    }

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }

    private fun test() {
        val url = "${AppConstant.GATEWAY_bKASH_SINGLE}??CID=3849331"
        //val url = "http://157.245.254.139/"
        /*ChromeCustomTabBrowser.launch(requireContext(), url) { fallbackUrl ->
            findNavController().navigate(R.id.nav_action_dashboard_webView, bundleOf("url" to fallbackUrl))
        }*/
        // or
        findNavController().navigate(R.id.nav_action_dashboard_webView, bundleOf("url" to url))
    }

}
