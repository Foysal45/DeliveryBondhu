package com.ajkerdeal.app.essential.ui.quick_order_scan

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
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
import com.ajkerdeal.app.essential.BuildConfig
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.district.DistrictThanaAreaDataModel
import com.ajkerdeal.app.essential.api.models.district.LocationData
import com.ajkerdeal.app.essential.api.models.district.LocationType
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeResponse
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.OrderIdWiseAmount
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatusUpdateRequest
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderCollectBinding
import com.ajkerdeal.app.essential.ui.barcode.BarcodeScanningActivity
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.*
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

    private var courierOrdersId = ""
    private var districtId = 0
    private var thanaId = 0
    private var areaId = 0
    private var quickOrderId = ""
    private var quickOrderInfoImgUrl: String? = ""
    private var isWeightSelected: Boolean = false
    private var serviceType: String = "alltoall"
    private var deliveryCharge: Double = 0.0

    //Merchant request info
    private var orderRequestSelfList: List<OrderIdWiseAmount> = listOf()
    private var requestOrderAmountTotal: Int = 0
    private var collectionTimeSlotId: Int = 0
    private var courierUserId: Int = 0
    private var collectionDistrictId: Int = 0
    private var collectionThanaId: Int = 0
    private var status: Int = 0
    private var operationFlag: Int = 0

    private lateinit var deliveryTypeAdapter: DeliveryTypeAdapter
    private var weightDataAdapter: WeightSelectionAdapter = WeightSelectionAdapter()
    private var isLocationLoading: Boolean = false

    private var filteredDistrictLists: MutableList<DistrictThanaAreaDataModel> = mutableListOf()
    private var filteredThanaLists: MutableList<DistrictThanaAreaDataModel> = mutableListOf()
    private var filteredAreaLists: MutableList<DistrictThanaAreaDataModel> = mutableListOf()

    private var isAriaAvailable = true
    private var weightRangeId: Int = 0
    private var selectedWeight = ""
    private var deliveryRangeId: Int = 0
    private var deliveryType: String = ""

    private var currentOrderRequestId: Int = 0
    private var currentOrderAmountInOrderRequestId: Int = 0
    private var currentTotalParcel: Int = 0
    private var previousOrderAmountInOrderRequestId: Int = 0

    private var orderType: String = "Only Delivery"
    private var isOrderTypeSelected: Boolean = false
    private var isCollection: Boolean = false

    private var codChargePercentageInsideDhaka: Double = 0.0
    private var codChargePercentageOutsideDhaka: Double = 0.0
    private var codChargePercentage: Double = 0.0
    private var codChargeMin: Int = 0

    private var dialog: ProgressDialog? = null


    //#region Life cycles
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding?.root ?: FragmentQuickOrderCollectBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bundle data
        orderRequestSelfList = (arguments?.getParcelableArrayList("orderRequestSelfList") ?: listOf())
        requestOrderAmountTotal = arguments?.getInt("requestOrderAmountTotal", 0) ?: 0
        courierUserId = arguments?.getInt("courierUserId", 0) ?: 0
        collectionTimeSlotId = arguments?.getInt("collectionTimeSlotId", 0) ?: 0
        collectionDistrictId = arguments?.getInt("collectionDistrictId", 0) ?: 0
        collectionThanaId = arguments?.getInt("collectionThanaId", 0) ?: 0
        status = arguments?.getInt("status", 0) ?: 0
        operationFlag = arguments?.getInt("operationFlag", 0) ?: 0

        Timber.d("BundleLog ${arguments?.bundleToString()}")

        val firstModel = orderRequestSelfList.first()
        currentOrderRequestId = firstModel.orderRequestId
        currentOrderAmountInOrderRequestId = firstModel.requestOrderAmount
        Timber.d("orderRequestIdChangeDebug $currentOrderRequestId $currentOrderAmountInOrderRequestId $currentTotalParcel")

        /*if (BuildConfig.DEBUG) {
            status = 44
            collectionDistrictId = 14
            collectionThanaId = 10026
            courierUserId = 1
            collectionTimeSlotId = 10
            orderRequestId = 26
        }*/

        init()
        initListener()
        if (operationFlag == 1) {
            binding?.scanBtn?.performClick()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
    //#endregion

    //#region init
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
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                layoutAnimation = null
                adapter = weightDataAdapter
            }
        }
        getBreakableCharge()
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
            //orderRequestIdChange()
            startPlaceOrderProcess()
        }

        binding?.district?.setOnClickListener {
            hideKeyboard()
            if (filteredDistrictLists.isEmpty()) {
                fetchLocationById(0, LocationType.DISTRICT)
            } else {
                goToLocationSelectionDialog(filteredDistrictLists, LocationType.DISTRICT)
            }
        }

        binding?.thana?.setOnClickListener {
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

        binding?.area?.setOnClickListener {
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

        deliveryTypeAdapter.onItemClick = { _, model ->
            deliveryRangeId = model.deliveryRangeId
            deliveryType = model.deliveryType ?: ""
            deliveryCharge = model.chargeAmount
        }

        binding?.prePaidOrderBtn?.setOnClickListener {
            isOrderTypeSelected = true
            binding?.prePaidOrderBtn?.background = ContextCompat.getDrawable(requireContext(), R.drawable.dotted_selected)
            binding?.codOrderBtn?.background = ContextCompat.getDrawable(requireContext(), R.drawable.dotted)
            isCollection = false
            orderType = "Only Delivery"
            binding?.collectionAmount?.hint = "প্যাকেজ এর ভিতরে প্রোডাক্টের দাম"
            binding?.collectionAmount?.requestFocus()
        }

        binding?.codOrderBtn?.setOnClickListener {
            isOrderTypeSelected = true
            binding?.codOrderBtn?.background = ContextCompat.getDrawable(requireContext(), R.drawable.dotted_selected)
            binding?.prePaidOrderBtn?.background = ContextCompat.getDrawable(requireContext(), R.drawable.dotted)
            isCollection = true
            orderType = "Delivery Taka Collection"
            binding?.collectionAmount?.hint = "কালেকশন অ্যামাউন্ট"
            binding?.collectionAmount?.requestFocus()
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
                    if (state.type == 1) {
                        if (state.isShow) {
                            if (dialog == null) {
                                dialog = progressDialog()
                            }
                            dialog?.show()
                        } else {
                            dialog?.dismiss()
                        }
                    } else if (state.type == 0) {
                        //binding?.progressBar?.isVisible = state.isShow
                    }
                }

            }

        })
    }
    //#endregion

    //#region Location Selection
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
            }
            LocationType.THANA -> {
                binding?.progressBar2?.isVisible = true
            }
            LocationType.AREA -> {
                binding?.progressBar3?.isVisible = true
            }
        }

        viewModel.loadAllDistrictsById(id).observe(viewLifecycleOwner, Observer { list ->
            isLocationLoading = false
            binding?.progressBar1?.isVisible = false
            binding?.progressBar2?.isVisible = false
            binding?.progressBar3?.isVisible = false

            when (locationType) {
                LocationType.DISTRICT -> {
                    filteredDistrictLists.clear()
                    filteredDistrictLists.addAll(list)
                    filteredThanaLists.clear()
                    filteredAreaLists.clear()
                    if (!preSelect) {
                        goToLocationSelectionDialog(filteredDistrictLists, locationType)
                    }
                }
                LocationType.THANA -> {
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
                }
                LocationType.AREA -> {
                    filteredAreaLists.clear()
                    filteredAreaLists.addAll(list)
                    isAriaAvailable = filteredAreaLists.isNotEmpty()
                    if (isAriaAvailable) {
                        binding?.areaLayout?.isVisible = true
                        if (!preSelect) {
                            goToLocationSelectionDialog(filteredAreaLists, locationType)
                        } else {
                            val sadarArea = list.first()
                            areaId = sadarArea.districtId
                            binding?.area?.setText(sadarArea.districtBng)
                            getDeliveryCharge(districtId, thanaId, areaId, serviceType)
                        }
                    } else {
                        binding?.areaLayout?.isVisible = false
                        areaId = 0
                        binding?.area?.setText("")
                    }
                }
            }
        })

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
                    filteredAreaLists.clear()

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

        serviceType = if (collectionDistrictId == districtId) { "citytocity" } else "alltoall"
        codChargePercentage = if (districtId == 14) {
            codChargePercentageInsideDhaka
        } else {
            codChargePercentageOutsideDhaka
        }

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
    //#endregion

    //#region Order Service location wise
    private fun getDeliveryCharge(districtId: Int, thanaId: Int, areaId: Int, serviceType: String) {
        viewModel.getDeliveryCharge(DeliveryChargeRequest(districtId, thanaId, areaId, serviceType)).observe(viewLifecycleOwner, Observer { list ->

            if (serviceType == "citytocity" && list.isEmpty()) {
                this.serviceType = "alltoall"
                getDeliveryCharge(districtId, thanaId, areaId, this.serviceType)
                return@Observer
            }

            if (list.isNotEmpty()) {
                val model = list.first()
                var filterDeliveryTypeList = model.weightRangeWiseData
                if (collectionDistrictId != 14) {
                    filterDeliveryTypeList = model.weightRangeWiseData.filterNot { it.type == "express" }
                }
                deliveryTypeAdapter.initLoad(filterDeliveryTypeList)
                deliveryTypeAdapter.selectPreSelection()


                val selectedWeightList: MutableList<DeliveryChargeResponse> = mutableListOf()
                val subList = list.subList(0, 3)
                selectedWeightList.addAll(subList)
                selectedWeightList.add(DeliveryChargeResponse(-1, "More"))
                weightDataAdapter.initLoad(selectedWeightList)
                weightDataAdapter.onItemClick = { _, model ->
                    if (model.weightRangeId == -1) {
                        binding?.weightSelectionLayout?.isVisible = true
                    } else {
                        binding?.weightSelectionLayout?.isVisible = false
                        selectWeight(model)
                    }
                }

            } else {
                deliveryTypeAdapter.clearList()
                weightDataAdapter.clearList()
            }

            val weightList: MutableList<String> = mutableListOf()
            weightList.add("ওজন (কেজি)")
            list.forEach {
                weightList.add(it.weight)
            }
            val weightAdapter = CustomSpinnerAdapter(requireContext(), R.layout.item_view_spinner_item, weightList)
            binding?.spinnerWeightSelection?.adapter = weightAdapter
            binding?.spinnerWeightSelection?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 != 0) {
                        val model2 = list[p2 - 1]
                        selectWeight(model2)
                    } else {
                        selectedWeight = ""
                        isWeightSelected = false
                        weightRangeId = 0
                    }
                }
            }


        })
    }

    private fun selectWeight(model: DeliveryChargeResponse) {
        selectedWeight = model.weight
        isWeightSelected = true
        weightRangeId = model.weightRangeId
    }
    //#endregion

    //#region Barcode Scanning
    private fun scanBarcode() {
        val intent = Intent(requireContext(), BarcodeScanningActivity::class.java).apply {
            putExtra("pattern", AppConstant.dtCodePattern)
        }
        barcodeScanningLauncher.launch(intent)
    }

    private val barcodeScanningLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedData = result.data?.getStringExtra("data") ?: return@registerForActivityResult
            if (isCheckPermission()){
                pickUpImage()
                courierOrdersId = scannedData
                binding?.scanResult?.text = scannedData
            }

        }
    }
    //#endregion

    //#region Image Capture
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

    private fun pickUpImage() {
        if (!isStoragePermissions()) {
            return
        }
        ImagePicker.with(this)
            .cameraOnly()
            //.crop(1.5f,1f)
            //.compress(512)
            .createIntent { intent ->
                startForQuickOrderImageResult.launch(intent)
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
    //#endregion

    //#region Collection
    private fun startPlaceOrderProcess() {
        if (!validation()){
            return
        }
        if (BuildConfig.DEBUG) {
            //courierOrdersId = "DT-513798"
            //binding?.scanResult?.text = courierOrdersId
            uploadParcelImage(quickOrderId)
            return
        }
        checkValidOrderId(quickOrderId)
    }

    private fun validation(): Boolean {

        quickOrderId = binding?.scanResult?.text.toString()
        val amountText = binding?.collectionAmount?.text?.toString() ?: ""
        val amount = amountText.toDoubleOrNull() ?: 0.0

        if (quickOrderId.isEmpty()) {
            val message = "Please Scan for order code"
            context?.toast(message)
            return false
        }

        if (districtId == 0) {
            val message = "Please Select District"
            context?.toast(message)
            return false
        }

        if (!isWeightSelected) {
            val message = "Please Select Weight"
            context?.toast(message)
            return false
        }

        if (deliveryRangeId == 0) {
            val message = "Please Select Service"
            context?.toast(message)
            return false
        }

        if (!isOrderTypeSelected) {
            val message = "Please select order type"
            context?.toast(message)
            return false
        }

        if (amountText.isEmpty()) {
            val message = if (isCollection) {
                "Please type collection amount"
            } else "Please type parcel actual amount"
            context?.toast(message)
            return false
        }

        if (isCollection && amount < deliveryCharge) {
            val message = "Collection amount can not be less than service charge"
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

    private fun checkValidOrderId(orderId: String){
        viewModel.checkIsQuickOrder(orderId).observe(viewLifecycleOwner, Observer { flag ->
            if (flag){
                uploadParcelImage(orderId)
            }else{
                context?.toast("দুঃখিত, কুইক অর্ডার কোডটি সঠিক নয়")
            }
        })
    }

    private fun uploadParcelImage(orderId: String) {
        if (quickOrderInfoImgUrl.isNullOrEmpty()){
            context?.toast("অনুগ্রহ করে ছবি সংগ্রহ করুন")
        }else{
            viewModel.uploadProfilePhoto(orderId, requireContext(), quickOrderInfoImgUrl ?: "").observe(viewLifecycleOwner, Observer { uploadFlag ->
                if (uploadFlag){
                    context?.toast("Parcel image uploaded")
                    placeOrder()
                }
            })
        }
    }

    private fun placeOrder(){
        val parcelImage = "https://static.ajkerdeal.com/images/dt/orderrequest/${courierOrdersId.lowercase(Locale.US)}.jpg"
        val amountText = binding?.collectionAmount?.text?.toString() ?: "0.0"
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val requestBody = QuickOrderUpdateRequest(
            courierOrdersId, currentOrderRequestId, courierUserId,
            status,
            districtId, thanaId, areaId,
            collectionDistrictId, collectionThanaId,
            deliveryRangeId,SessionManager.dtUserId,
            weightRangeId, selectedWeight,
            deliveryType, serviceType,
            deliveryCharge,
            collectionTimeSlotId,
            parcelImage,
            orderType,
            if (isCollection) amount else 0.0,
            amount,
            calculateCODCharge()
        )

        viewModel.updateQuickOrder(requestBody).observe(viewLifecycleOwner, Observer {
            context?.toast("Order place successFull")
            orderRequestIdChange()
            orderRequestIdStatusUpdate()
            clearData()
        })
    }

    private fun clearData(){
        binding?.scanResult?.text = ""
        quickOrderInfoImgUrl = ""
        binding?.invoicePic?.let {  view->
            Glide.with(view)
                .load(quickOrderInfoImgUrl)
                .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(view)
        }
    }

    private fun orderRequestIdChange() {
        currentTotalParcel++
        if (currentTotalParcel >= currentOrderAmountInOrderRequestId) {
            val index = orderRequestSelfList.indexOfFirst { it.orderRequestId == currentOrderRequestId }
            if (index != -1 && index != orderRequestSelfList.lastIndex) {
                currentOrderRequestId = orderRequestSelfList[index + 1].orderRequestId
                currentOrderAmountInOrderRequestId = orderRequestSelfList[index + 1].requestOrderAmount
                currentTotalParcel = 0
            }
        }
        Timber.d("orderRequestIdChangeDebug $currentOrderRequestId $currentOrderAmountInOrderRequestId $currentTotalParcel")
    }

    private fun getBreakableCharge() {
        viewModel.getBreakableCharge().observe(viewLifecycleOwner, Observer { model ->
            codChargeMin = model.codChargeMin
            codChargePercentageInsideDhaka = model.codChargeDhakaPercentage
            codChargePercentageOutsideDhaka = model.codChargePercentage
        })
    }

    private fun calculateCODCharge(): Double {
        return if (isCollection) {
            val amountText = binding?.collectionAmount?.text?.toString() ?: "0.0"
            val amount = amountText.toDoubleOrNull() ?: 0.0
            var payCODCharge = (amount / 100.0) * codChargePercentage
            if (payCODCharge < codChargeMin) {
                payCODCharge = codChargeMin.toDouble()
            }
            return payCODCharge
        } else {
            0.0
        }
    }

    private fun orderRequestIdStatusUpdate() {

        if (previousOrderAmountInOrderRequestId != currentOrderAmountInOrderRequestId) {
            val requestBody: MutableList<QuickOrderStatusUpdateRequest> = mutableListOf()
            val requestModel = QuickOrderStatusUpdateRequest(
                currentOrderAmountInOrderRequestId,
                SessionManager.dtUserId,
                status
            )
            requestBody.add(requestModel)
            updateOrderStatus(requestBody)
            previousOrderAmountInOrderRequestId = currentOrderAmountInOrderRequestId
        }
    }

    private fun updateOrderStatus(requestBody: List<QuickOrderStatusUpdateRequest>) {
        viewModel.updateQuickOrderStatus(requestBody).observe(viewLifecycleOwner, Observer { flag ->
            if (flag) {
                Timber.d("updateOrderStatus $flag")
            }
        })

    }
    //#endregion

}