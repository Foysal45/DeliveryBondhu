package com.ajkerdeal.app.essential.ui.quick_order_scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderCollectBinding
import com.ajkerdeal.app.essential.ui.barcode.BarcodeScanningActivity
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.alert
import com.ajkerdeal.app.essential.utils.toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.theartofdev.edmodo.cropper.CropImage
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class QuickOrderCollectFragment : Fragment() {

    private var binding: FragmentQuickOrderCollectBinding? = null
    private val viewModel: QuickOrderViewModel by inject()

    private var districtId = 0
    private var thanaId = 0
    private var districtName = ""
    private var thanaName = ""
    private var quickOrderId = ""
    private var quickOrderInfoImgUrl = ""

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

        binding?.invoicePic?.setOnClickListener {
            pickUpImage()
        }

        binding?.updateBtn?.setOnClickListener {
            placeOrder()
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
                    binding?.thana?.setText("")
                    binding?.area?.setText("")
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
                        binding?.area?.setText("")
                        if (position in 0..thanaModelList.size) {
                            thanaId = thanaModelList[position].districtId

                        }
                    }
                    binding!!.thana.isEnabled = true
                })
            }
        }

        binding!!.area.setOnClickListener {

            if (thanaId == 0) {
                context?.toast("থানা নির্বাচন করুন")
            } else {
                binding!!.area.isEnabled = false
                viewModel.loadAllDistrictsById(thanaId).observe(viewLifecycleOwner, Observer { response ->

                    val areaModelList = response
                    val areaNameList = areaModelList?.map { it.districtBng }

                    val dialog = LocationSelectionDialog.newInstance(areaNameList as MutableList<String>)
                    dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                    dialog.onLocationPicked = { position, value ->
                        binding?.area?.setText(value)
                    }
                    binding!!.area.isEnabled = true
                })
            }
        }

    }

    private fun placeOrder() {
        if (!validation()){
            return
        }
        isValidOrderId(quickOrderId)

    }

    private fun isValidOrderId(orderId: String){
        viewModel.checkIsQuickOrder(orderId).observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.uploadProfilePhoto(orderId, requireContext(), quickOrderInfoImgUrl).observe(viewLifecycleOwner, Observer {
                    if (it){
                        context?.toast("image updated")
                    }
                })
            }else{
                context?.toast("Invalid Order Id")
            }
        })
    }

    private fun validation(): Boolean {
         quickOrderId = binding?.scanResult?.text.toString()
        val district = binding?.district?.text.toString()

        if (quickOrderId.isNullOrEmpty()) {
            val message = "Please Scan Order Info"
            context?.toast(message)
            return false
        }

        if (district.isNullOrEmpty()) {
            val message = "Please Select District"
            context?.toast(message)
            return false
        }

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
                pickUpImage()
                binding?.scanResult?.text = scannedData
            }

        }
    }

    private fun pickUpImage() {
        if (!isStoragePermissions()) {
            return
        }
        try {
            ImagePicker.cameraOnly().start(this)
        } catch (e: Exception) {
            Timber.d(e)
            context?.toast("No Application found to handle this action")
        }
    }

    private fun isStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val storagePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                val storagePermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (storagePermissionRationale) {
                    permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                false
            } else {
                true
            }
        } else {
            return true
        }
    }

    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
        if (hasPermission) {

        } else {
            val storagePermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (storagePermissionRationale) {
                alert("Permission Required", "App required Storage permission to function properly. Please grand permission.", true, "Give Permission", "Cancel") {
                    if (it == AlertDialog.BUTTON_POSITIVE) {
                        isStoragePermissions()
                    }
                }.show()
            } else {
                alert("Permission Required", "Please go to Settings to enable Storage permission. (Settings-apps--permissions)", true, "Settings", "Cancel") {
                    if (it == AlertDialog.BUTTON_POSITIVE) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireContext().packageName}")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }.show()
            }
        }
    }

    private fun isCheckPermission(): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val permission2 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return when {
            permission1 == PackageManager.PERMISSION_GRANTED /* && permission2 == PackageManager.PERMISSION_GRANTED */-> {
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

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image: Image? = ImagePicker.getFirstImageOrNull(data)

            image?.path?.let {
                Timber.d("ImageLog ImagePickerPath: $it")
                val uri = FileProvider.getUriForFile(requireContext(), "com.ajkerdeal.app.essential.fileprovider", File(it))
                Timber.d("ImageLog ImagePickerUri: $uri")
                val actualPath = FileUtils(requireContext()).getPath(uri)
                Timber.d("FilePath: $actualPath")
                quickOrderInfoImgUrl = actualPath

                binding?.invoicePic?.let { view ->
                    Glide.with(view)
                        .load(actualPath)
                        .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(false)
                        .into(view)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imageCaptureResult = ImagePicker.shouldHandle(requestCode, resultCode, data)
         if (imageCaptureResult){
            val image: Image? = ImagePicker.getFirstImageOrNull(data)
            image?.path?.let {
                Timber.d("ImageLog ImagePickerPath: $it")

                val uri = FileProvider.getUriForFile(requireContext(), "com.ajkerdeal.app.essential.fileprovider", File(it))
                Timber.d("ImageLog ImagePickerUri: $requestCode, $uri")
                //quickOrderInfoImgUrl = actualPath

                val builder = CropImage.activity()
                builder.start( requireActivity())
            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.uri
                Timber.d("ImageLog cropPhotoURI $uri")
                binding?.invoicePic?.let { view ->
                    Glide.with(view)
                        .load(uri)
                        .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(view)
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val msg = result.error
                Timber.d("Error $msg")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}