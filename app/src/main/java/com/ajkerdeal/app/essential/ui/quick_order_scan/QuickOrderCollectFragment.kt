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
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.district.DistrictThanaAreaDataModel
import com.ajkerdeal.app.essential.api.models.district.LocationData
import com.ajkerdeal.app.essential.api.models.district.LocationType
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderCollectBinding
import com.ajkerdeal.app.essential.ui.barcode.BarcodeScanningActivity
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.CustomSpinnerAdapter
import com.ajkerdeal.app.essential.utils.alert
import com.ajkerdeal.app.essential.utils.hideKeyboard
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
    private var areaId = 0
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

    //Merchant request info
    private var orderRequestId: Int = 0
    private var collectionTimeSlotId: Int = 0
    private var courierUserId: Int = 0
    private var collectionDistrictId: Int = 0
    private var collectionThanaId: Int = 0
    private var status: Int = 0

    private lateinit var deliveryTypeAdapter: DeliveryTypeAdapter
    private var dataAdapter:WeightSelectionAdapter = WeightSelectionAdapter()
    private var isLocationLoading: Boolean = false

    private var filteredDistrictLists: MutableList<DistrictThanaAreaDataModel> = mutableListOf()
    private var filteredThanaLists: MutableList<DistrictThanaAreaDataModel> = mutableListOf()
    private var filteredAreaLists: MutableList<DistrictThanaAreaDataModel> = mutableListOf()

    private var isAriaAvailable = true

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
        orderRequestId = arguments?.getInt("orderRequestId", 0) ?: 0
        courierUserId = arguments?.getInt("courierUserId", 0) ?: 0
        collectionTimeSlotId = arguments?.getInt("collectionTimeSlotId", 0) ?: 0
        collectionDistrictId = arguments?.getInt("collectionDistrictId", 0) ?: 0
        collectionThanaId = arguments?.getInt("collectionThanaId", 0) ?: 0
        status = arguments?.getInt("status", 0) ?: 0
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
        binding?.recyclerViewWeight?.let { view ->
            with(view) {
                setHasFixedSize(false)
                isNestedScrollingEnabled = false
                layoutManager = GridLayoutManager(context, 2, androidx.recyclerview.widget.RecyclerView.VERTICAL, false)
                layoutAnimation = null
                adapter = dataAdapter
            }
        }

    }

    private fun initListener() {

        //getDeliveryCharge(14, 10026, 0, serviceType)

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

            hideKeyboard()
            fetchLocationById(0, LocationType.DISTRICT)
        }

        binding!!.thana.setOnClickListener {

            hideKeyboard()
            if (districtId != 0) {
                if (filteredThanaLists.isEmpty()) {
                    fetchLocationById(districtId, LocationType.THANA)
                } else {
                    goToLocationSelectionDialog(filteredThanaLists, LocationType.THANA)
                }
            } else {
                context?.toast(getString(R.string.select_dist))
            }
        }

        binding!!.area.setOnClickListener {

            hideKeyboard()
            if (isAriaAvailable) {
                if (thanaId != 0) {
                    if (filteredAreaLists.isEmpty()) {
                        fetchLocationById(thanaId, LocationType.AREA)
                    } else {
                        goToLocationSelectionDialog(filteredAreaLists, LocationType.AREA)
                    }
                } else {
                    context?.toast(getString(R.string.select_thana))
                }
            } else {
                context?.toast(getString(R.string.no_aria))
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
            courierUserId,
            status,
            districtId,
            thanaId,
            areaId,
            collectionDistrictId,
            collectionThanaId,
            18,
            5,
            "3 kg - 4 kg",
            "Sador Express 48 hours ৩",
            "alltoall",
            125.0,
            collectionTimeSlotId,
            "https://static.ajkerdeal.com/images/dt/orderrequest/${scannedOrderID.lowercase(Locale.US)}.jpg"
        )

       viewModel.updateQuickOrder(requestBody).observe(viewLifecycleOwner, Observer {
            context?.toast("Order SuccessFull")
        })
    }

    private fun fetchLocationById(id: Int, locationType: LocationType, preSelect: Boolean = false) {

        if (isLocationLoading) {
            context?.toast("লোকেশন লোড হচ্ছে, অপেক্ষা করুন")
            return
        } else {
            isLocationLoading = true
        }
        when (locationType) {
            LocationType.DISTRICT -> {
                binding?.progressBar1?.isVisible = true
                viewModel.loadAllDistrictsById(id).observe(viewLifecycleOwner, Observer { list ->
                    isLocationLoading = false
                    binding?.progressBar1?.isVisible = false
                    filteredDistrictLists.clear()
                    filteredDistrictLists.addAll(list)
                    filteredThanaLists.clear()
                    filteredAreaLists.clear()
                    if (!preSelect) {
                        goToLocationSelectionDialog(filteredDistrictLists, locationType)
                    }
                })
            }
            LocationType.THANA -> {
                binding?.progressBar2?.isVisible = true
                viewModel.loadAllDistrictsById(id).observe(viewLifecycleOwner, Observer { list ->
                    isLocationLoading = false
                    binding?.progressBar2?.isVisible = false
                    filteredThanaLists.clear()
                    filteredThanaLists.addAll(list)
                    if (!preSelect) {
                        goToLocationSelectionDialog(filteredThanaLists, locationType)
                    } else {
                        if (list.isNotEmpty()) {
                            val sadarThana = list.first()
                            thanaId = sadarThana.districtId
                            binding?.thana?.setText(sadarThana.districtBng)
                            fetchLocationById(thanaId, LocationType.AREA, true)
                            getDeliveryCharge(districtId, thanaId, 0, serviceType)
                        }
                    }
                })
            }
            LocationType.AREA -> {
                binding?.progressBar3?.isVisible = true
                viewModel.loadAllDistrictsById(id).observe(viewLifecycleOwner, Observer { list ->
                    isLocationLoading = false
                    binding?.progressBar3?.isVisible = false
                    filteredAreaLists.clear()
                    filteredAreaLists.addAll(list)
                    isAriaAvailable = filteredAreaLists.isNotEmpty()
                    if (isAriaAvailable) {
                        binding?.areaLayout?.visibility = View.VISIBLE
                        if (!preSelect) {
                            goToLocationSelectionDialog(filteredAreaLists, locationType)
                        } else {
                            val sadarArea = list.first()
                            areaId = sadarArea.districtId
                            binding?.area?.setText(sadarArea.districtBng)
                            getDeliveryCharge(districtId, thanaId, areaId, serviceType)
                        }
                    } else {
                        binding?.areaLayout?.visibility = View.GONE
                        areaId = 0
                        binding?.area?.setText("")
                    }
                })
            }
        }
    }

    private fun updateUIAfterDistrict(id: Int, displayNameBangla: String) {

        districtId = id
        binding?.district?.setText(displayNameBangla)
        thanaId = 0
        binding?.thana?.setText("")
        areaId = 0
        binding?.area?.setText("")
        binding?.areaLayout?.visibility = View.GONE

        val selectedDistrict = filteredDistrictLists.find { it.districtId == districtId }
        selectedDistrict?.let { district ->
            showLocationAlert(district, LocationType.DISTRICT)
        }

        serviceType = if (merchantDistrict == districtId) {
            "citytocity"
        } else "alltoall"
        /*codChargePercentage = if (districtId == 14) {
            codChargePercentageInsideDhaka
        } else {
            codChargePercentageOutsideDhaka
        }
        calculateTotalPrice()*/
        fetchLocationById(districtId, LocationType.THANA, true)

        /*val filterList = allLocationList.filter { it.parentId == districtId }
        filteredThanaLists.clear()
        if (filterList.isNotEmpty()) {
            val sortedList = filterList.sortedBy { it.districtPriority } as MutableList<AllDistrictListsModel>
            filteredThanaLists.addAll(sortedList)
        }
        val sadarThana = filteredThanaLists.first()
        thanaId = sadarThana.districtId
        etThana.setText(sadarThana.districtBng)*/

        /*val filterArea = allLocationList.filter { it.parentId == thanaId } as MutableList<AllDistrictListsModel>
        Timber.d("filterArea $filterArea")
        isAriaAvailable = filterArea.isNotEmpty()
        if (isAriaAvailable) {
            filteredAreaLists.clear()
            filteredAreaLists.addAll(filterArea.sortedBy { it.districtPriority })
            etAriaPostOfficeLayout.visibility = View.VISIBLE
        } else {
            etAriaPostOfficeLayout.visibility = View.GONE
        }*/

        // Check same city logic
        /*serviceType = if (merchantDistrict == districtId) {
            "citytocity"
        } else "alltoall"
        getDeliveryCharge(districtId, sadarThana.districtId, 0, serviceType)
        if (districtId == 14) {
            codChargePercentage = codChargePercentageInsideDhaka
        } else {
            codChargePercentage = codChargePercentageOutsideDhaka
        }
        calculateTotalPrice()*/
    }

    private fun goToLocationSelectionDialog(list: MutableList<DistrictThanaAreaDataModel>, locationType: LocationType) {

        val locationList: MutableList<LocationData> = mutableListOf()
        val locationListName: MutableList<String> = mutableListOf()
        list.forEach { model ->
            locationList.add(LocationData.from(model))
            locationListName.add(model.districtBng ?: "")
        }

        val dialog = LocationSelectionDialog.newInstance(locationListName)
        dialog.show(childFragmentManager, LocationSelectionDialog.tag)
        dialog.onLocationPicked = { position, value ->
            when (locationType) {
                LocationType.DISTRICT -> {
                    districtId = list[position].districtId
                    binding?.district?.setText(value)
                    thanaId = 0
                    binding?.thana?.setText("")
                    areaId = 0
                    binding?.area?.setText("")
                    binding?.areaLayout?.visibility = View.GONE
                    filteredThanaLists.clear()
                    filteredAreaLists.clear()
                    updateUIAfterDistrict(districtId, value)
                    val locationModel = list[position]
                    showLocationAlert(locationModel, LocationType.DISTRICT)
                }
                LocationType.THANA -> {
                    thanaId = list[position].districtId
                    binding?.thana?.setText(value)
                    areaId = 0
                    binding?.area?.setText("")

                    val locationModel = list[position]
                    showLocationAlert(locationModel, LocationType.THANA)
                    getDeliveryCharge(districtId, thanaId, 0, serviceType)
                    if (filteredAreaLists.isEmpty()) {
                        fetchLocationById(thanaId, LocationType.AREA, true)
                    }

                }
                LocationType.AREA -> {
                    areaId = list[position].districtId
                    val areaName = value
                    binding?.area?.setText(areaName)

                    val locationModel = list[position]
                    showLocationAlert(locationModel, LocationType.AREA)

                    getDeliveryCharge(districtId, thanaId, areaId, serviceType)
                }
            }
        }
    }

    private fun showLocationAlert(model: DistrictThanaAreaDataModel, locationType: LocationType) {
        if (model.isActiveForCorona) {
            val msg = when (locationType) {
                LocationType.DISTRICT -> "${model.districtBng} জেলায় ডেলিভারি সার্ভিস সাময়িকভাবে বন্ধ রয়েছে।"
                LocationType.THANA -> "${model.districtBng} থানায় ডেলিভারি সার্ভিস সাময়িকভাবে বন্ধ রয়েছে।"
                LocationType.AREA -> "${model.districtBng} এরিয়ায় ডেলিভারি সার্ভিস সাময়িকভাবে বন্ধ রয়েছে।"
            }
            alert(getString(R.string.instruction), msg) {
                if (it == AlertDialog.BUTTON_POSITIVE) {
                    when (locationType) {
                        LocationType.DISTRICT -> {
                            districtId = 0
                            binding?.district?.setText("")
                            thanaId = 0
                            binding?.thana?.setText("")
                            areaId = 0
                            binding?.area?.setText("")
                            binding?.areaLayout?.visibility = View.GONE
                        }
                        LocationType.THANA -> {
                            thanaId = 0
                            binding?.thana?.setText("")
                            areaId = 0
                            binding?.area?.setText("")
                            binding?.areaLayout?.visibility = View.GONE
                        }
                        LocationType.AREA -> {
                            areaId = 0
                            binding?.area?.setText("")
                            binding?.areaLayout?.visibility = View.GONE
                        }
                    }
                }
            }.show()
        }
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
            //dataAdapter.initLoad(list)

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
                            /*if (selectedServiceType != 0) {
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