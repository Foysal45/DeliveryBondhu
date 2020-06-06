package com.ajkerdeal.app.essential.ui.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentDashboardBinding
import com.ajkerdeal.app.essential.utils.AppConstant

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_dashboard, container, false)
        return FragmentDashboardBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.button1?.setOnClickListener {
            findNavController().navigate(R.id.nav_action_dashboard_orderList)
        }

        binding?.button2?.setOnClickListener {
            findNavController().navigate(R.id.nav_action_dashboard_parcelList)
            //test()
        }
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
