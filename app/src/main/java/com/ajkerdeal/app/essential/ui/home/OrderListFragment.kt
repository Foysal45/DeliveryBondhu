package com.ajkerdeal.app.essential.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestAD
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.api.models.print.PrintData
import com.ajkerdeal.app.essential.api.models.print.PrintModel
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.api.models.weight.UpdatePriceWithWeightRequest
import com.ajkerdeal.app.essential.databinding.FragmentOrderListBinding
import com.ajkerdeal.app.essential.printer.template.PrintInvoice
import com.ajkerdeal.app.essential.services.ImageUploadWorker
import com.ajkerdeal.app.essential.ui.home.weight_selection.WeightSelectionBottomSheet
import com.ajkerdeal.app.essential.ui.print_dialog.PrintSelectionBottomSheet
import com.ajkerdeal.app.essential.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import org.koin.android.ext.android.inject
import timber.log.Timber

@SuppressLint("SetTextI18n")
class OrderListFragment : Fragment() {

    private var binding: FragmentOrderListBinding? = null

    private val viewModel: HomeViewModel by inject()

    private var dialog: ProgressDialog? = null

    // Lazy loading
    private var isLoading = false
    private val visibleThreshold = 2
    private var totalCount: Int = 0
    private var firstCall: Int = 0

    private var searchKey: String = "-1"
    private var filterStatus: String = "-1"
    private var dtStatus: String = "-1"
    private var collectionFlag: Int = 1
    private var lastFilterIndex: Int = -1
    private var customType: String = "no"

    private var serviceTye: String = ""

    private var imageUploadMerchantId: String = ""
    private var imageUploadOrderIdList: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_order_list, container, false)
        return FragmentOrderListBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        serviceTye = arguments?.getString("serviceType", "collectionanddelivery") ?: "collectionanddelivery"

        val dataAdapter = OrderListParentAdapter()
        val layoutManagerLinear = LinearLayoutManager(requireContext())
        with(binding!!.recyclerView) {
            setHasFixedSize(true)
            layoutManager = layoutManagerLinear
            adapter = dataAdapter
        }
        dataAdapter.onCall = { number: String? ->

            try {
                val zoiperAvailable = isPackageInstalled(requireContext().packageManager, "com.zoiper.android.app")
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (zoiperAvailable) {
                    intent.setPackage("com.zoiper.android.app")
                }
                startActivity(intent)
            } catch (e: Exception) {
                requireContext().toast("Could not find an activity to place the call")
            }
        }
        dataAdapter.onActionClicked = { model, actionModel, orderModel ->

            val requestBody: MutableList<StatusUpdateModel> = mutableListOf()
            var instructions: String? = null
            var bondhuCharge = 0
            var couponIds: String = ""

            if (orderModel != null) {
                val statusModel = StatusUpdateModel().apply {
                    couponId = orderModel.couponId
                    isDone = actionModel.updateStatus
                    comments = actionModel.statusMessage ?: ""
                    orderDate = orderModel.orderDate ?: ""
                    merchantId = orderModel.merchantId
                    dealId = orderModel.dealId.toIntOrNull() ?: 0
                    customerId = orderModel.customerId?.toIntOrNull() ?: 0
                    deliveryDate = orderModel.deliveryDate ?: ""
                    commentedBy = SessionManager.userId
                    pODNumber = orderModel.pODNumber ?: ""
                    type = serviceTye
                }
                requestBody.add(statusModel)
                instructions = orderModel.collectionSource?.sourceMessageData?.instructions
                bondhuCharge = orderModel.bondhuCharge
                couponIds = orderModel.couponId
            } else {
                model.orderList?.forEachIndexed { index, orderModel ->
                    val statusModel = StatusUpdateModel().apply {
                        couponId = orderModel.couponId
                        isDone = actionModel.updateStatus
                        comments = actionModel.statusMessage ?: ""
                        orderDate = orderModel.orderDate ?: ""
                        merchantId = orderModel.merchantId
                        dealId = orderModel.dealId.toIntOrNull() ?: 0
                        customerId = orderModel.customerId?.toIntOrNull() ?: 0
                        deliveryDate = orderModel.deliveryDate ?: ""
                        commentedBy = SessionManager.userId
                        pODNumber = orderModel.pODNumber ?: ""
                        type = serviceTye
                    }
                    requestBody.add(statusModel)
                    if (index == model.orderList?.lastIndex) {
                        couponIds += orderModel.couponId
                    } else {
                        couponIds += "${orderModel.couponId},"
                    }

                }
                instructions = model.collectionSource?.sourceMessageData?.instructions
            }
            Timber.d("statusUpdateLog $couponIds $bondhuCharge $instructions")

            when {
                // Go to payment is success then update status
                actionModel.isPaymentType == 1 -> {
                    goToPaymentGateway(couponIds, bondhuCharge, requestBody)
                }
                // Show popup dialog first the update status
                actionModel.popUpDialogType == 1 -> {
                    alert("কনফার্ম করুন", "আপনি কি মার্চেন্টের কাছে গিয়েছেন?", true, "হ্যাঁ", "না") {
                        val requestModel = StatusLocationRequest(model.merchantId, SessionManager.userId)
                        if (it == AlertDialog.BUTTON_POSITIVE) {
                            requestModel.confirmation = "yes"
                        } else {
                            requestModel.confirmation = "no"
                        }
                        (activity as HomeActivity).updateStatusLocation(requestModel)
                        updateStatus(requestBody, instructions)
                    }.show()
                }
                actionModel.popUpDialogType == 2 -> {
                    alert("কনফার্ম করুন", "আপনি কি এই পরিবর্তন সম্পর্কে নিশ্চিত?", true, "হ্যাঁ", "না") {
                        if (it == AlertDialog.BUTTON_POSITIVE) {
                            updateStatus(requestBody, instructions)
                        }
                    }.show()
                }
                // update status
                else -> {
                    updateStatus(requestBody, instructions)
                }
            }

        }
        dataAdapter.onPictureClicked = { orderModel ->
            pictureDialog(orderModel)
        }
        dataAdapter.onOrderListExpand = { parentModel, state ->
            if (state) {
                if (!isValidCoordinate(parentModel.latitude) || !isValidCoordinate(parentModel.longitude)) {
                    if (!parentModel.isLocationUpdated) {
                        updateMerchantLocation(parentModel)
                        parentModel.isLocationUpdated = true
                    }
                }
            }
        }
        dataAdapter.onLocationUpdate = { parentModel ->
            alert("মার্চেন্টের লোকেশন সেট", "আপনি কি এখন ${parentModel.name} এর ঠিকানায় আছেন?", true, "হ্যা", "না") {
                if (it == Dialog.BUTTON_POSITIVE) {
                    updateMerchantLocation(parentModel)
                }
            }.show()
        }
        dataAdapter.onLocationReport = { parentModel ->
            Timber.d("parentDataModel ${parentModel}")
            if (isValidCoordinate(parentModel.latitude) && isValidCoordinate(parentModel.longitude)) {
                val gmmIntentUri = Uri.parse("geo:${parentModel.latitude},${parentModel.longitude}?q=${parentModel.latitude},${parentModel.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                mapIntent.resolveActivity(requireContext().packageManager)?.let {
                    startActivity(mapIntent)
                }
            } else {
                context?.toast("জিপিএস লোকেশন সঠিক নয়")
            }
        }
        dataAdapter.onPrintClicked = { model ->
            printInvoice(model)
        }
        dataAdapter.onQRCodeClicked = { model: OrderModel ->
            showQRCode(model)
        }
        dataAdapter.onWeightUpdateClicked = { model1, model2 ->
            goToWeightSelectionBottomSheet(model1, model2)
        }
        dataAdapter.onUploadClicked = { model ->
            imageUploadMerchantId = model.merchantId.toString()
            imageUploadOrderIdList = model.orderList?.joinToString(",") { it.couponId } ?: "orderIds"
            addPictureDialog() {
                when(it) {
                    1 -> {
                        pickImageFromCamera()
                    }
                    2 -> {
                        pickImageFromGallery()
                    }
                }
            }
        }

        //viewModel.loadOrderOrSearch()
        viewModel.pagingState.observe(viewLifecycleOwner, Observer {
            if (it.isInitLoad) {
                Timber.d("pagingState init")
                Timber.d("initData: ${it.dataList}")
                dataAdapter.loadInitData(it.dataList)
                firstCall = 20
                binding!!.emptyView.visibility = View.GONE

            } else {
                Timber.d("pagingState load more")
                isLoading = false
                firstCall += 20
                Timber.d("loadMoreData: ${it.dataList}")
                dataAdapter.loadMoreData(it.dataList)
            }
            totalCount = it.totalCount
            binding!!.appBarLayout.countTV.text = "${DigitConverter.toBanglaDigit(totalCount)}টি"
        })

        viewModel.loadFilterStatus(serviceTye).observe(viewLifecycleOwner, Observer { list ->
            Timber.d("$list")
            val filterList = list.filter { it.flag == 1 }
            val statusName = filterList.map { it.statusName }
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_item_selected, statusName)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding!!.appBarLayout.spinner.adapter = arrayAdapter

            Timber.d("lastFilterIndex $lastFilterIndex")
            if (lastFilterIndex != -1 && (lastFilterIndex in 0..filterList.size)) {
                binding!!.appBarLayout.spinner.setSelection(lastFilterIndex)
            }

            binding!!.appBarLayout.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    if (position in 0..filterList.size) {
                        val model = filterList[position]
                        val selectedStatus = model.status
                        val selectedDTStatus = model.dtStatus
                        lastFilterIndex = position

                        filterStatus = selectedStatus
                        dtStatus = selectedDTStatus
                        dataAdapter.clearData()
                        Timber.d("loadOrderOrSearch called from filter spinner")

                        binding!!.appBarLayout.filterName.text = model.statusName
                        collectionFlag = model.collectionFilter
                        customType = model.customType
                        /*val collectionSwitchFlag = model.collectionFilter
                        if (collectionSwitchFlag == 1) {
                            binding!!.appBarLayout.tabLayout.visibility = View.VISIBLE
                            collectionFlag = 1
                        } else {
                            binding!!.appBarLayout.tabLayout.visibility = View.GONE
                            collectionFlag = 0
                            binding!!.appBarLayout.tabLayout.getTabAt(0)?.select()
                        }*/

                        dataAdapter.isCollectionPoint = collectionFlag
                        dataAdapter.isCollectionPointGroup = model.collectionFilter
                        dataAdapter.allowLocationAdd = model.allowLocationAdd
                        dataAdapter.allowPrint = model.allowPrint
                        dataAdapter.allowImageUpload = model.allowImageUpload
                        dataAdapter.isCollectionTimerShow = model.isCollectionTimerShow
                        dataAdapter.isWeightUpdateEnable = model.isWeightUpdateEnable
                        if (SessionManager.isOffline && model.isUnavailableShow) {
                            binding!!.emptyView.text = "আপনি এখন Unavailable আছেন"
                            binding!!.emptyView.visibility = View.VISIBLE
                        } else {
                            binding!!.emptyView.visibility = View.GONE
                            viewModel.loadOrderOrSearch(
                                flag = collectionFlag,
                                statusId = filterStatus,
                                dtStatusId = dtStatus,
                                searchKey = searchKey,
                                type = SearchType.Product,
                                serviceType = serviceTye,
                                customType = customType
                            )
                        }

                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ViewState.ShowMessage -> {
                    requireContext().toast(state.message)
                }
                is ViewState.KeyboardState -> {
                    hideKeyboard()
                }
                is ViewState.ProgressState -> {
                    if (state.type == 0) {
                        if (state.isShow) {
                            if (dialog == null) {
                                dialog = progressDialog()
                            }
                            dialog?.show()
                        } else {
                            dialog?.dismiss()
                        }
                    } else if (state.type == 1) {
                        if (state.isShow) {
                            binding!!.progressBar.visibility = View.VISIBLE
                        } else {
                            binding!!.progressBar.visibility = View.GONE
                        }
                    }
                }
                is ViewState.EmptyViewState -> {
                    dataAdapter.clearData()
                    binding!!.appBarLayout.countTV.text = "০টি"
                    binding!!.emptyView.text = "এই মুহূর্তে কোনো অর্ডার নেই"
                    binding!!.emptyView.visibility = View.VISIBLE
                }
            }
        })

        binding!!.appBarLayout.searchBtn.setOnClickListener {
            hideKeyboard()
            searchKey = binding!!.appBarLayout.searchET.text.toString()
            if (searchKey.isNotEmpty()) {
                binding!!.appBarLayout.chipsGroup.visibility = View.VISIBLE
                binding!!.appBarLayout.searchKey.text = searchKey
                binding!!.appBarLayout.searchKey.setOnClickListener {
                    searchKey = "-1"
                    binding!!.appBarLayout.searchET.text.clear()
                    binding!!.appBarLayout.chipsGroup.visibility = View.GONE
                    binding!!.appBarLayout.countTV.text = "০টি"
                    viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, serviceType = serviceTye, customType = customType)
                }
                binding!!.appBarLayout.searchKey.setOnCloseIconClickListener {
                    binding!!.appBarLayout.searchKey.performClick()
                }
                binding!!.appBarLayout.countTV.text = "০টি"
                viewModel.loadOrderOrSearch(
                    flag = collectionFlag,
                    statusId = filterStatus,
                    dtStatusId = dtStatus,
                    searchKey = searchKey,
                    type = SearchType.Product,
                    serviceType = serviceTye,
                    customType = customType
                )
            }
            //requireContext().toast(getString(R.string.development))
        }

        binding!!.swipeRefresh.setOnRefreshListener {
            binding!!.swipeRefresh.isRefreshing = false
            if (SessionManager.isOffline) return@setOnRefreshListener
            Timber.d("loadOrderOrSearch called from swipe refresh")
            viewModel.loadOrderOrSearch(
                flag = collectionFlag,
                statusId = filterStatus,
                dtStatusId = dtStatus,
                searchKey = searchKey,
                type = SearchType.Product,
                serviceType = serviceTye,
                customType = customType
            )
            /*binding!!.searchET.text.clear()
            if (binding!!.chipsGroup.visibility == View.VISIBLE) {
                binding!!.chipsGroup.visibility = View.GONE
            }*/
        }

        binding!!.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    Timber.d("addOnScrollListener onScrolled: $dx $dy")
                    val currentItemCount = layoutManagerLinear.itemCount
                    val lastVisibleItem = layoutManagerLinear.findLastVisibleItemPosition()
                    Timber.d("onScrolled: \nItemCount: $currentItemCount  <= lastVisible: $lastVisibleItem firstCall : $firstCall  < TotalDeal : $totalCount  ${!isLoading}")
                    if (!isLoading && currentItemCount <= lastVisibleItem + visibleThreshold && firstCall < totalCount) {
                        isLoading = true
                        Timber.d("loadOrderOrSearch called from lazy loading")
                        viewModel.loadOrderOrSearch(
                            firstCall,
                            20,
                            statusId = filterStatus,
                            dtStatusId = dtStatus,
                            flag = collectionFlag,
                            searchKey = searchKey,
                            type = SearchType.Product,
                            serviceType = serviceTye,
                            customType = customType
                        )
                    }
                }
            }
        })

        binding!!.appBarLayout.searchET.hint = getString(R.string.search_hint)
        binding!!.appBarLayout.searchET.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->

            val imeAction = when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH -> true
                else -> false
            }
            //val eventType = event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            return@OnEditorActionListener if (imeAction) {
                binding!!.appBarLayout.searchBtn.performClick()
                true
            } else false
        })

        when (serviceTye) {
            AppConstant.SERVICE_TYPE_COLLECTION_DELIVERY -> {
                collectionFlag = 1 // default
                //binding!!.appBarLayout.tabLayout.visibility = View.VISIBLE
            }
            AppConstant.SERVICE_TYPE_COLLECTION -> {
                collectionFlag = 1
                //binding!!.appBarLayout.tabLayout.visibility = View.GONE
            }
            AppConstant.SERVICE_TYPE_DELIVERY -> {
                collectionFlag = 0
                //binding!!.appBarLayout.tabLayout.visibility = View.GONE
            }
            AppConstant.SERVICE_TYPE_RETURN -> {
                collectionFlag = 0
            }
        }
    }

    private fun updateStatus(requestBody: MutableList<StatusUpdateModel>, message: String?) {
        viewModel.updateOrderStatus(requestBody).observe(viewLifecycleOwner, Observer {
            if (it) {
                if (!message.isNullOrEmpty()) {
                    orderDialog(message)
                }
                viewModel.loadOrderOrSearch(
                    flag = collectionFlag,
                    statusId = filterStatus,
                    dtStatusId = dtStatus,
                    searchKey = searchKey,
                    type = SearchType.Product,
                    serviceType = serviceTye,
                    customType = customType
                )
            }
        })
    }

    private fun goToPaymentGateway(couponIds: String, paybackChange: Int, requestBody: MutableList<StatusUpdateModel>) {

        var query = ""
        if (SessionManager.bkashMobileNumber.isNotEmpty()) {
            try {
                val paymentData = "${SessionManager.bkashMobileNumber},$paybackChange"
                val key = "3byamAfK"
                val encryptedData = Cryptography.Encrypt(paymentData, key)
                Timber.d("Encryption plainData $paymentData")
                Timber.d("Encryption encrypted $encryptedData")
                query = "&ID=$encryptedData"
            } catch (e: Exception) {
                Timber.d(e)
            }
        }

        val url = "${AppConstant.GATEWAY_bKASH_SINGLE}?CID=$couponIds$query"
        val bundle = bundleOf(
            "url" to url,
            "updateModel" to requestBody,
            "title" to "পেমেন্ট"
        )
        findNavController().navigate(R.id.nav_action_orderList_webView, bundle)
    }

    override fun onStart() {
        super.onStart()
        //Timber.d("onStart called")
        if (filterStatus != "-1") {
            Timber.d("loadOrderOrSearch called from onStart")
            viewModel.loadOrderOrSearch(
                flag = collectionFlag,
                statusId = filterStatus,
                dtStatusId = dtStatus,
                searchKey = searchKey,
                type = SearchType.Product,
                serviceType = serviceTye,
                customType = customType
            )
        }
    }

    private fun orderDialog(message: String, type: Int = 0, listener: ((type: Int) -> Unit)? = null) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_alert, null)
        builder.setView(view)
        val body: TextView = view.findViewById(R.id.body)
        val positiveBtn: TextView = view.findViewById(R.id.positiveBtn)
        val actionLayout: LinearLayout = view.findViewById(R.id.actionBtnLayout)
        val positiveBtn1: MaterialButton = view.findViewById(R.id.positiveBtn1)
        val negativeBtn: MaterialButton = view.findViewById(R.id.negativeBtn)

        body.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
        if (type == 1) {
            actionLayout.visibility = View.VISIBLE
            positiveBtn.visibility = View.GONE
        }

        val dialog = builder.create()
        dialog.show()
        positiveBtn.setOnClickListener {
            dialog.dismiss()
            listener?.invoke(0)
        }
        positiveBtn1.setOnClickListener {
            dialog.dismiss()
            listener?.invoke(0)
        }
        negativeBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun pictureDialog(model: OrderModel, listener: ((type: Int) -> Unit)? = null) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_product_overview, null)
        builder.setView(view)
        val title: TextView = view.findViewById(R.id.title)
        val productImage: ImageView = view.findViewById(R.id.image)
        val close: ImageView = view.findViewById(R.id.close)

        title.text = model.productTitle
        Glide.with(productImage)
            .load(model.imageUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_logo_essentials))
            .into(productImage)

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#B3000000")))
        dialog.show()
        close.setOnClickListener {
            dialog.dismiss()
        }
    }

    /*private fun collectionDialog( couponId: Int, collectionPoint: Int = 0, listener: ((type: Int) -> Unit)? = null) {

        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_collection, null)
        builder.setView(view)
        val body: TextView = view.findViewById(R.id.body)
        val positiveBtn: TextView = view.findViewById(R.id.positiveBtn)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        val collectionAdapter = CollectionAdapter()
        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = collectionAdapter
        }
        val dialog = builder.create()
        //dialog.show()
        positiveBtn.setOnClickListener {
            dialog.dismiss()
            listener?.invoke(0)
        }

        viewModel.loadCollectionList(couponId, collectionPoint).observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                collectionAdapter.loadData(it as MutableList<CollectionData>)
                dialog.show()
            } else {
                listener?.invoke(0)
            }
        })

    }*/

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }

    private fun printInvoice(model: OrderCustomer) {

        model.orderList?.let { list ->

            val tag = PrintSelectionBottomSheet.tag
            val dialog = PrintSelectionBottomSheet.newInstance(list)
            dialog.show(childFragmentManager, tag)
            dialog.onPrintClicked = { list ->
                dialog.dismiss()

                val orderDataList: MutableList<PrintData> = mutableListOf()
                list.forEach {
                    orderDataList.add(
                        PrintData(it.couponId, it.productQtn, it.productPrice)
                    )
                }
                val printModel: PrintModel = PrintModel().apply {
                    userName = SessionManager.userName
                    userPhone = SessionManager.mobile
                    merchantName = model.name
                    merchantPhone = model.mobileNumber
                    dataList = orderDataList
                }

                val printer = PrintInvoice(requireContext(), printModel)
                printer.print(true)
            }
        }

    }

    private fun showQRCode(model: OrderModel) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_qrcode, null)
        builder.setView(view)
        val title: TextView = view.findViewById(R.id.title)
        val productImage: ImageView = view.findViewById(R.id.image)
        val codeTV: TextView = view.findViewById(R.id.code)
        val close: ImageView = view.findViewById(R.id.close)

        title.text = model.productTitle
        codeTV.text = model.couponId
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(model.couponId, BarcodeFormat.CODE_128, 512, 256)
            val bitmap = Bitmap.createBitmap(512, 256, Bitmap.Config.RGB_565)
            for (i in 0..511) {
                for (j in 0..255) {
                    val color: Int = if (bitMatrix.get(i, j)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    bitmap.setPixel(i, j, color)
                }
            }
            Glide.with(productImage)
                .load(bitmap)
                .apply(RequestOptions().placeholder(R.drawable.ic_logo_ad))
                .into(productImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#B3000000")))
        dialog.show()
        close.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun addPictureDialog(listener: ((type: Int) -> Unit)? = null) {

        val builder = MaterialAlertDialogBuilder(requireContext())
        val layout = layoutInflater.inflate(R.layout.dialog_picker, null)
        builder.setView(layout)
        val takePhoto = layout.findViewById(R.id.action1) as LinearLayout
        val fromGallery = layout.findViewById(R.id.action2) as LinearLayout
        val dialog = builder.create()
        takePhoto.setOnClickListener {
            dialog.cancel()
            listener?.invoke(1)
        }
        fromGallery.setOnClickListener {
            dialog.cancel()
            listener?.invoke(2)
        }
        dialog.show()
    }

    private fun pickImageFromCamera() {
        ImagePicker.cameraOnly().start(this)
    }

    private fun pickImageFromGallery() {
        ImagePicker.create(this)
            .folderMode(true)
            .toolbarFolderTitle("ফোল্ডার নির্বাচন করুন")
            .toolbarImageTitle("ছবি নির্বাচন করুন")
            .includeVideo(false)
            .theme(R.style.ImagePickerTheme)
            .limit(1)
            //.language("bn")
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)){

            val imageList = ImagePicker.getImages(data)
            val pathList = imageList.map { it.path }
            val imageUrl = pathList.first()
            uploadImageDialog(imageUrl) { type ->
                if (type == 1) {
                    uploadFile(imageUrl)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImageDialog(imagePath: String, isShowOnly: Boolean = false, listener: ((type: Int) -> Unit)? = null) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val layout = layoutInflater.inflate(R.layout.dialog_image_upload, null)
        builder.setView(layout)
        val imageView: ImageView = layout.findViewById(R.id.image)
        val title: TextView = layout.findViewById(R.id.title)
        val action1: MaterialButton = layout.findViewById(R.id.action1)
        val action2: MaterialButton = layout.findViewById(R.id.action2)
        if (isShowOnly) {
            title.text = ""
            action1.visibility = View.GONE
        }
        val dialog = builder.create()

        Glide.with(imageView).load(imagePath).into(imageView)
        action1.setOnClickListener {
            listener?.invoke(1)
            dialog.dismiss()
        }
        action2.setOnClickListener {
            listener?.invoke(2)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun uploadFile(imageUrl: String) {

        binding?.progressBar?.visibility = View.VISIBLE
        val data = Data.Builder()
            .putString("merchantId", imageUploadMerchantId)
            .putString("orderIds", imageUploadOrderIdList)
            .putString("imageUrl", imageUrl)
            .build()

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<ImageUploadWorker>().setConstraints(constraints).setInputData(data).build()
        WorkManager.getInstance(requireContext()).beginUniqueWork("uploadReturnPic", ExistingWorkPolicy.KEEP, request).enqueue()
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner, Observer { workInfo ->
            if (workInfo != null){
                val result = workInfo.outputData.getString("work_result")
                if (workInfo.state == WorkInfo.State.SUCCEEDED){
                    //context?.toast("প্রোফাইল আপডেট হয়েছে")
                    binding?.progressBar?.visibility = View.GONE
                    val result = workInfo.outputData.getString("work_result")
                    val serverImageUrl = workInfo.outputData.getString("serverImageUrl") ?: ""
                    val msg = "$result\n$serverImageUrl"
                    binding?.parent?.snackbar(msg,Snackbar.LENGTH_INDEFINITE, "View") {
                        uploadImageDialog(serverImageUrl, true)
                    }?.show()
                } else if (workInfo.state == WorkInfo.State.FAILED){
                    //context?.toast("কোথাও কোনো সমস্যা হচ্ছে")
                    binding?.progressBar?.visibility = View.GONE
                    result?.let {
                        binding?.parent?.snackbar(it)
                    }
                }
            }
        })
    }

    private fun updateMerchantLocation(parentModel: OrderCustomer) {
        if (parentModel.orderList?.first()?.couponId!!.startsWith("DT-")){
            val modelDT = LocationUpdateRequestDT(
                    parentModel.collectAddressDistrictId,
                    parentModel.collectAddressThanaId,
                    0, parentModel.merchantId,
                    parentModel.address, parentModel.latitude,
                    parentModel.longitude
            )
            (activity as HomeActivity).updateLocationDT(modelDT)
        }else{
            val modelAD = LocationUpdateRequestAD(
                    parentModel.merchantId,
                    parentModel.latitude,
                    parentModel.longitude

            )
            (activity as HomeActivity).updateLocationAD(modelAD)
        }
    }

    //Weight Selection
    private fun goToWeightSelectionBottomSheet( model: OrderModel,  parentModel: OrderCustomer){
        val tag = WeightSelectionBottomSheet.tag
        val dialog = WeightSelectionBottomSheet.newInstance()
        dialog.show(childFragmentManager, tag)
        dialog.onActionClicked = {  weightRangeId->
            val requestBody = UpdatePriceWithWeightRequest(parentModel.collectAddressDistrictId, parentModel.collectAddressThanaId, 0, weightRangeId, model.couponId, model.deliveryRangeId)
            viewModel.updatePriceWithWeight(requestBody).observe(viewLifecycleOwner, Observer { isUpdatePrice ->
                if (isUpdatePrice) {
                    Toast.makeText(requireContext(), "দাম আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                }
            })
            dialog.dismiss()
        }
    }

}
