package com.ajkerdeal.app.essential.ui.home.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentDashboardBinding
import com.ajkerdeal.app.essential.ui.home.HomeActivity
import com.ajkerdeal.app.essential.ui.home.HomeActivityViewModel
import com.ajkerdeal.app.essential.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding? = null
    private val viewModel: DashboardViewModel by inject()
    private val viewModelHomeActivity: HomeActivityViewModel by inject()

    private var snackbar: Snackbar? = null
    private var isGPS: Boolean = false

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
            when (userStatus.riderType) {
                "" -> {
                    binding?.button1?.visibility = View.VISIBLE
                    binding?.button2?.visibility = View.VISIBLE
                    binding?.button3?.visibility = View.VISIBLE
                    binding?.button5?.visibility = View.GONE
                    binding?.button4?.visibility = View.GONE
                }
                "return" -> {
                    binding?.button1?.visibility = View.GONE
                    binding?.button2?.visibility = View.GONE
                    binding?.button3?.visibility = View.GONE
                    binding?.button5?.visibility = View.VISIBLE
                    binding?.button4?.visibility = View.GONE
                }
                "all" -> {
                    binding?.button1?.visibility = View.VISIBLE
                    binding?.button2?.visibility = View.VISIBLE
                    binding?.button3?.visibility = View.VISIBLE
                    binding?.button5?.visibility = View.VISIBLE
                    binding?.button4?.visibility = View.GONE
                }
            }
            binding?.activeSwitch?.isChecked = !userStatus.isNowOffline
            (activity as HomeActivity).availabilityState(!userStatus.isNowOffline)
            availabilityState(!userStatus.isNowOffline)

            if (!userStatus.isProfileImage || !userStatus.isNID) {
                val msg = "নতুন অর্ডার পেতে আপনার ছবি, ভোটার আই.ডি কার্ড অথবা ড্রাইভিং লাইসেন্স এর ছবি আপলোড করুন"
                snackbar = binding?.parent?.snackbar(msg, actionName = "আপডেট") {
                    findNavController().navigate(R.id.nav_action_dashboard_profile)
                }
                snackbar?.show()
            }

            if (userStatus.locationUpdateIntervalInMinute > 0) {
                if (isLocationPermission()) {
                    (activity as HomeActivity).startLocationUpdate(userStatus.locationUpdateIntervalInMinute, userStatus.locationDistanceInMeter)
                }
            }

            val profileImage =
                if (userStatus.profileImage.isNullOrEmpty()) "https://static.ajkerdeal.com/images/bondhuprofileimage/${SessionManager.userId}/profileimage.jpg" else userStatus.profileImage

            Glide.with(this)
                .load(profileImage)
                .apply(RequestOptions().placeholder(R.drawable.ic_person_circle).error(R.drawable.ic_person_circle).circleCrop())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding!!.userPic)

            SessionManager.userPic = profileImage ?: ""
            SessionManager.riderType = userStatus.riderType

        })

        viewModelHomeActivity.isOfflineLive.observe(viewLifecycleOwner, Observer {
            Timber.d("viewModelHomeActivity isOfflineLive $it")
            binding?.activeSwitch?.setOnCheckedChangeListener(null)
            binding?.activeSwitch?.isChecked = !it
            availabilityState(!it)
        })
        viewModelHomeActivity.isGPS.observe(viewLifecycleOwner, Observer {
            isGPS = it
        })

        binding?.userName?.text = SessionManager.userName
        val sdf = SimpleDateFormat("dd MMM (EEEE)", Locale("bn", "BD"))
        binding?.dateStamp?.text = sdf.format(Calendar.getInstance().timeInMillis)

        binding?.button1?.setOnClickListener {
            if (isLocationPermission() && checkLocationEnable()) {
                snackbar?.dismiss()
                val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_COLLECTION)
                findNavController().navigate(R.id.nav_action_dashboard_orderList, bundle)
            } else {
                showLocationRequiredDialog()
            }
        }
        binding?.button2?.setOnClickListener {
            if (isLocationPermission() && checkLocationEnable()) {
                snackbar?.dismiss()
                //val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_COLLECTION_DELIVERY)
                findNavController().navigate(R.id.nav_quick_order_lists)
            } else {
                showLocationRequiredDialog()
            }
        }
        binding?.button3?.setOnClickListener {
            if (isLocationPermission() && checkLocationEnable()) {
                snackbar?.dismiss()
                val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_DELIVERY)
                findNavController().navigate(R.id.nav_action_dashboard_orderList, bundle)
            } else {
                showLocationRequiredDialog()
            }
        }
        binding?.button5?.setOnClickListener {
            if (isLocationPermission() && checkLocationEnable()) {
                snackbar?.dismiss()
                val bundle = bundleOf("serviceType" to AppConstant.SERVICE_TYPE_RETURN)
                findNavController().navigate(R.id.nav_action_dashboard_orderList, bundle)
            } else {
                showLocationRequiredDialog()
            }
        }

        binding?.button4?.setOnClickListener {
            if (isLocationPermission() && checkLocationEnable()) {
                snackbar?.dismiss()
                findNavController().navigate(R.id.nav_action_dashboard_parcelList)
            } else {
                showLocationRequiredDialog()
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

        isCheckPermission()

    }

    override fun onResume() {
        super.onResume()
        snackbar?.show()
        checkLocationEnable()
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

    private fun checkLocationEnable(): Boolean {
        return if (isGPS) {
            true
        } else {
            (activity as HomeActivity).turnOnGPS()
            false
        }
    }

    private fun showLocationRequiredDialog() {
        snackbar = binding?.parent?.snackbar(message = "Turn on location and accept location permission", actionName = "Ok") {
            (activity as HomeActivity).turnOnGPS()
        }
        snackbar?.show()
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

    private fun showLocationOnDialog() {
        snackbar?.dismiss()
        alert("জিপিএস পারমিশন", "জিপিএস অন করুন", true, "ঠিক আছে", "পরে দিবো") {
            if (it == AlertDialog.BUTTON_POSITIVE) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            snackbar?.show()
        }.show()
    }


    private fun availabilityState(status: Boolean) {
        binding?.activeSwitch?.text = if (status) "Available" else "Not Available"
        binding?.activeSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Timber.d("Checked")
                viewModel.updateUserStatus("false", 1)
                binding?.activeSwitch?.text = "Available"

            } else {
                viewModel.updateUserStatus("true", 1)
                binding?.activeSwitch?.text = "Not Available"
            }
            (activity as HomeActivity).availabilityState(isChecked)
        }
    }


    private fun isLocationEnabled(): Boolean {

        val lm: LocationManager? = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var gpsEnabled = false
        var networkEnabled = false
        try {
            gpsEnabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            networkEnabled = lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return gpsEnabled && networkEnabled
    }

    private fun isCheckPermission(): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val permission2 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return when {
            permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED -> {
                true
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                false
            }
            else -> {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                false
            }
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var isCameraGranted: Boolean = false
        var isStorageGranted: Boolean = false
        permissions.entries.forEach { permission ->
            if (permission.key == Manifest.permission.CAMERA) {
                isCameraGranted = permission.value
            }
            if (permission.key == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                isStorageGranted = permission.value
            }
        }
        if (isCameraGranted /*&& isStorageGranted*/) {

        }
    }

}
