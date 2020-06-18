package com.ajkerdeal.app.essential.ui.home.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentDashboardBinding
import com.ajkerdeal.app.essential.ui.home.HomeActivity
import com.ajkerdeal.app.essential.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding? = null
    private val viewModel: DashboardViewModel by inject()
    private var snackbar: Snackbar? = null

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
                    binding?.button3?.visibility = View.VISIBLE
                    binding?.button4?.visibility = View.VISIBLE
                }
                1 -> {
                    binding?.button1?.visibility = View.VISIBLE
                    binding?.button2?.visibility = View.VISIBLE
                    binding?.button3?.visibility = View.VISIBLE
                    binding?.button4?.visibility = View.GONE
                }
                2 -> {
                    binding?.button1?.visibility = View.GONE
                    binding?.button2?.visibility = View.GONE
                    binding?.button3?.visibility = View.GONE
                    binding?.button4?.visibility = View.VISIBLE
                }
            }
            activeSwitch?.isChecked = !userStatus.isNowOffline
            activeSwitch?.text = if (!userStatus.isNowOffline) "Available" else "Not Available"
            activeSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    Timber.d("Checked")
                    viewModel.updateUserStatus("false", 1)
                    activeSwitch?.text = "Available"
                } else {
                    viewModel.updateUserStatus("true", 1)
                    activeSwitch?.text = "Not Available"
                }
            }

            if (!userStatus.isProfileImage || !userStatus.isNID) {
                val msg = "নতুন অর্ডার পেতে আপনার ছবি, ভোটার আই.ডি কার্ড অথবা ড্রাইভিং লাইসেন্স এর ছবি আপলোড করুন"
                snackbar = binding?.parent?.snackbar(msg,actionName = "আপডেট") {
                    findNavController().navigate(R.id.nav_action_dashboard_profile)
                }
                snackbar?.show()
            }

            if (userStatus.locationUpdateIntervalInMinute > 0) {
                if (isLocationPermission()) {
                    (activity as HomeActivity).startLocationUpdate(userStatus.locationUpdateIntervalInMinute, userStatus.locationDistanceInMeter)
                }
            }

            Glide.with(this)
                .load(userStatus.profileImage)
                .apply(RequestOptions().placeholder(R.drawable.ic_person_circle).error(R.drawable.ic_person_circle).circleCrop())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding!!.userPic)

            SessionManager.userPic = userStatus.profileImage ?: ""

        })

        userName?.text = SessionManager.userName
        val sdf = SimpleDateFormat("dd MMM (EEEE)", Locale("bn","BD"))
        dateStamp?.text = sdf.format(Calendar.getInstance().timeInMillis)

        binding?.button1?.setOnClickListener {
            if (isLocationPermission()) {
                snackbar?.dismiss()
                val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_COLLECTION)
                findNavController().navigate(R.id.nav_action_dashboard_orderList, bundle)
            }
        }
        binding?.button2?.setOnClickListener {
            if (isLocationPermission()) {
                snackbar?.dismiss()
                val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_COLLECTION_DELIVERY)
                findNavController().navigate(R.id.nav_action_dashboard_orderList, bundle)
            }
        }
        binding?.button3?.setOnClickListener {
            if (isLocationPermission()) {
                snackbar?.dismiss()
                val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_DELIVERY)
                findNavController().navigate(R.id.nav_action_dashboard_orderList, bundle)
            }
        }
        binding?.button4?.setOnClickListener {
            if (isLocationPermission()) {
                snackbar?.dismiss()
                findNavController().navigate(R.id.nav_action_dashboard_parcelList)
            }
        }
        binding?.profileLayout?.setOnClickListener {
            snackbar?.dismiss()
            findNavController().navigate(R.id.nav_action_dashboard_profile)
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

    override fun onResume() {
        super.onResume()
        snackbar?.show()
    }

    override fun onPause() {
        super.onPause()
        snackbar?.dismiss()
    }

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }

    /*private fun test() {
        val url = "${AppConstant.GATEWAY_bKASH_SINGLE}??CID=3849331"
        //val url = "http://157.245.254.139/"
        *//*ChromeCustomTabBrowser.launch(requireContext(), url) { fallbackUrl ->
            findNavController().navigate(R.id.nav_action_dashboard_webView, bundleOf("url" to fallbackUrl))
        }*//*
        // or
        //findNavController().navigate(R.id.nav_action_dashboard_webView, bundleOf("url" to url))
    }*/

    private fun isLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            if (permission1 != PackageManager.PERMISSION_GRANTED) {
                showLocationPermissionDialog()
                false
            } else true
        } else {
            true
        }

    }

    private fun showLocationPermissionDialog() {
        snackbar?.dismiss()
        alert("লোকেশন পারমিশন", "নতুন অর্ডার পেতে আপনার বর্তমান লোকেশন জানা অত্যন্ত জরুরি, লোকেশন পারমিশন দিন", true, "ঠিক আছে", "পরে দিবো") {
            if (it == AlertDialog.BUTTON_POSITIVE) {
                (activity as HomeActivity).isLocationPermission()
            }
            snackbar?.show()
        }.show()
    }

}
