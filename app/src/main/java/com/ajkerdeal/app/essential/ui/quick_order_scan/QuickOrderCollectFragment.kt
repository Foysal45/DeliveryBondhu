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
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderCollectBinding
import com.ajkerdeal.app.essential.ui.barcode.BarcodeScanningActivity
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.CustomSpinnerAdapter
import com.ajkerdeal.app.essential.utils.alert
import com.ajkerdeal.app.essential.utils.toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class QuickOrderCollectFragment : Fragment() {

    private var binding: FragmentQuickOrderCollectBinding? = null
    private val viewModel: QuickOrderViewModel by inject()

    private var scannedOrderID = ""
    private var quickOrderRequestID = 0
    private var districtId = 0
    private var thanaId = 0
    private var districtName = ""
    private var thanaName = ""
    private var quickOrderId = ""
    private var quickOrderInfoImgUrl: String? = ""
    private var weight: String = ""
    private var isWeightSelected: Boolean = false
    private var deliveryType: String = ""
    private var merchantDistrict: Int = 0
    private var serviceType: String = "alltoall"
    private var payShipmentCharge: Double = 0.0
    private var deliveryCharge: Double = 0.0
    private var extraDeliveryCharge: Double = 0.0

    private lateinit var deliveryTypeAdapter: DeliveryTypeAdapter

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

    private fun init(){
        deliveryTypeAdapter = DeliveryTypeAdapter()
        binding?.recyclerView?.let { view ->
            with(view) {
                setHasFixedSize(false)
                isNestedScrollingEnabled = false
                layoutManager = GridLayoutManager(context, 2, androidx.recyclerview.widget.RecyclerView.VERTICAL, false)
                layoutAnimation = null
                adapter = deliveryTypeAdapter
            }
        }

    }

    private fun initListener() {

        getDeliveryCharge(14, 10026, 0, serviceType)

        binding?.scanBtn?.setOnClickListener {
            if (isCheckPermission()) {
                scanBarcode()
            }
        }

        binding?.invoicePic?.setOnClickListener {
            pickUpImage()
        }

        binding?.updateBtn?.setOnClickListener {
            startPlaceOrderProcess()
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
                getDeliveryCharge(districtId, thanaId, 0, serviceType)
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
                    getDeliveryCharge(districtId, thanaId, 0, serviceType)
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

    private fun startPlaceOrderProcess() {
        if (!validation()){
            return
        }
        isValidOrderId(quickOrderId)

    }

    private fun isValidOrderId(orderId: String){
        viewModel.checkIsQuickOrder(orderId).observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.uploadProfilePhoto(orderId, requireContext(), quickOrderInfoImgUrl ?: "").observe(viewLifecycleOwner, Observer {
                    if (it){
                        context?.toast("image updated")
                        placeOrder()
                    }
                })
            }else{
                context?.toast("Invalid Order Id")
            }
        })
    }

    private fun placeOrder(){
        val requestBody = QuickOrderUpdateRequest(
            scannedOrderID,
            quickOrderRequestID,
            1,
            44,
            14,
            10026,
            0,
            0,
            0,
            18,
            5,
            "3 kg - 4 kg",
            "Sador Express 48 hours ৩",
            "alltoall",
            125.0,
            5,
            "https://static.ajkerdeal.com/images/dt/orderrequest/${scannedOrderID.lowercase(Locale.US)}.jpg"
        )

        viewModel.updateQuickOrder(requestBody).observe(viewLifecycleOwner, Observer {
            context?.toast("Order SuccessFull")
        })
    }

    private fun getDeliveryCharge(districtId: Int, thanaId: Int, areaId: Int, serviceType: String) {

        viewModel.getDeliveryCharge(DeliveryChargeRequest(districtId, thanaId, areaId, serviceType)).observe(viewLifecycleOwner, Observer { list ->

            if (serviceType == "citytocity" && list.isEmpty()) {
                this.serviceType = "alltoall"
                getDeliveryCharge(districtId, thanaId, areaId, this.serviceType)
                return@Observer
            }

            val weightList: MutableList<String> = mutableListOf()
            weightList.add("ওজন (কেজি)")
            for (model1 in list) {
                weightList.add(model1.weight)
            }

            val weightAdapter = CustomSpinnerAdapter(requireContext(), R.layout.item_view_spinner_item, weightList)
            binding?.spinnerWeightSelection?.adapter = weightAdapter
            binding?.spinnerWeightSelection?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 != 0) {
                        val model2 = list[p2 - 1]
                        weight = model2.weight
                        isWeightSelected = true
                        deliveryType = ""

                        var filterDeliveryTypeList = model2.weightRangeWiseData
                        if (merchantDistrict != 14) {
                            filterDeliveryTypeList = model2.weightRangeWiseData.filterNot { it.type == "express" }
                        }
                        deliveryTypeAdapter.initLoad(filterDeliveryTypeList)
                        deliveryTypeAdapter.selectPreSelection()
                    } else {
                        isWeightSelected = false
                        deliveryType = ""
                        if (list.isNotEmpty()) {
                            val model2 = list.first()
                            var filterDeliveryTypeList = model2.weightRangeWiseData
                            if (merchantDistrict != 14) {
                                filterDeliveryTypeList = model2.weightRangeWiseData.filterNot { it.type == "express" }
                            }
                            deliveryTypeAdapter.initLoad(filterDeliveryTypeList)
                            //Reset change
                            payShipmentCharge = 0.0
                            deliveryCharge = 0.0
                            extraDeliveryCharge = 0.0
                            //calculateTotalPrice()
                            // select pre selected
                          /*  if (selectedServiceType != 0) {
                                deliveryTypeAdapter.selectByDeliveryRangeId(selectedServiceType)
                            } else {
                                deliveryTypeAdapter.selectPreSelection()
                            }*/
                        } else {
                            deliveryTypeAdapter.clearList()
                        }
                    }
                }
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

        if (quickOrderInfoImgUrl.isNullOrEmpty()) {
            val message = "Please Captured Image"
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
                scannedOrderID = scannedData
                quickOrderRequestID = scannedData.replace("DT-", "").toInt()
                binding?.scanResult?.text = scannedData
            }

        }
    }

    private fun pickUpImage() {
        if (!isStoragePermissions()) {
            return
        }
        ImagePicker.with(this)
            .cameraOnly()
            .crop(1.5f,1f)
            .compress(512)
            .createIntent { intent ->
                startForQuickOrderImageResult.launch(intent)
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

    private val startForQuickOrderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!
            Timber.d("ImageLog Image Uri->  $uri")
            quickOrderInfoImgUrl = uri.path
            Timber.d("ImageLog Image Uri Path-> $quickOrderInfoImgUrl")
            binding?.invoicePic?.let {  view->
                Glide.with(view)
                    .load(uri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(view)
            }


        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            context?.toast(ImagePicker.getError(data))
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