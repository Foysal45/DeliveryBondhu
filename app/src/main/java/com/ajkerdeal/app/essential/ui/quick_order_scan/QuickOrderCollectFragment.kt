package com.ajkerdeal.app.essential.ui.quick_order_scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderCollectBinding
import com.ajkerdeal.app.essential.ui.barcode.BarcodeScanningActivity
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.toast
import com.esafirm.imagepicker.features.ImagePicker
import org.koin.android.ext.android.inject

class QuickOrderCollectFragment : Fragment() {

    private var binding: FragmentQuickOrderCollectBinding? = null
    private val viewModel: QuickOrderViewModel by inject()

    private var districtId = 0
    private var thanaId = 0
    private var districtName = ""
    private var thanaName = ""

    companion object {
        fun newInstance(): QuickOrderCollectFragment = QuickOrderCollectFragment().apply {}
        val tag: String = QuickOrderCollectFragment::class.java.name
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding?.root ?: FragmentQuickOrderCollectBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        initListener()
    }

    private fun init() {

    }

    private fun initListener() {

        binding?.scanBtn?.setOnClickListener {
            if (isCheckPermission()) {
                scanBarcode()
            }
        }
        binding?.updateBtn?.setOnClickListener {
            if (validation()) {
                // update
            }
        }

        binding!!.district.setOnClickListener {

            binding!!.district.isEnabled = false
            viewModel.loadAllDistrictsById(0).observe(viewLifecycleOwner, Observer { response ->

                val districtModelList = response
                val districtNameList = districtModelList.map { it.districtBng }

                val dialog = LocationSelectionDialog.newInstance(districtNameList as MutableList<String>)
                dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                dialog.onLocationPicked = { position, value ->
                    binding?.district?.setText(value)
                    if (position in 0..districtModelList.size) {
                        districtId = districtModelList[position].districtId
                    }
                }
                binding!!.district.isEnabled = true
            })
        }

        binding!!.thana.setOnClickListener {

            if (districtId == 0) {
                context?.toast("জেলা নির্বাচন করুন")
            } else {
                binding!!.thana.isEnabled = false
                viewModel.loadAllDistrictsById(districtId).observe(viewLifecycleOwner, Observer { response ->

                    val thanaModelList = response
                    val thanaNameList = thanaModelList?.map { it.districtBng }

                    val dialog = LocationSelectionDialog.newInstance(thanaNameList as MutableList<String>)
                    dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                    dialog.onLocationPicked = { position, value ->
                        thanaName = value
                        binding?.thana?.setText(value)
                        if (position in 0..thanaModelList.size) {
                            val model = thanaModelList[position]

                        }
                    }
                    binding!!.thana.isEnabled = true
                })
            }
        }

    }

    private fun validation(): Boolean {

        return true
    }

    private fun scanBarcode() {
        val intent = Intent(requireContext(), BarcodeScanningActivity::class.java)
        barcodeScanningLauncher.launch(intent)
    }

    private val barcodeScanningLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedData = result.data?.getStringExtra("data") ?: return@registerForActivityResult
            if (isCheckPermission()){
                ImagePicker.cameraOnly().start(this)
                context?.toast(scannedData)
            }

        }
    }

    private fun isCheckPermission(): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val permission2 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return when {
            permission1 == PackageManager.PERMISSION_GRANTED /*&& permission2 == PackageManager.PERMISSION_GRANTED*/ -> {
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
            scanBarcode()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}