package com.ajkerdeal.app.essential.ui.home

import android.app.ProgressDialog
import android.content.Intent
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
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.collection.CollectionData
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.databinding.FragmentOrderListBinding
import com.ajkerdeal.app.essential.ui.dialog.CollectionAdapter
import com.ajkerdeal.app.essential.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import timber.log.Timber

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

    private var serviceTye: String = ""

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
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e: Exception) {
                requireContext().toast("Could not find an activity to place the call")
            }
        }
        dataAdapter.onActionClicked = { model, actionModel, orderModel  ->

            val requestBody: MutableList<StatusUpdateModel> = mutableListOf()
            var instructions: String? = null
            var collectionPointAvailable = 0
            var bondhuCharge = 0

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
                }
                requestBody.add(statusModel)
                instructions = orderModel.collectionSource?.sourceMessageData?.instructions
                collectionPointAvailable = actionModel.collectionPointAvailable
                bondhuCharge = orderModel.bondhuCharge

                if (actionModel.isPaymentType == 1) {

                    val paymentData = "${SessionManager.mobile},$bondhuCharge"
                    val key = "3byamAfK"
                    Timber.d("Encryption plainData $paymentData")
                    val encryptedData = Cryptography.Encrypt(paymentData, key)
                    Timber.d("Encryption encrypted $encryptedData")
                    //Timber.d("Encryption decrypted: ${AESEncryptionClass.decryptMessage(encryptedData)}")

                    val url = "${AppConstant.GATEWAY_bKASH_SINGLE}?CID=${orderModel.couponId}&ID=$encryptedData"
                    val bundle = bundleOf(
                        "url" to url,
                        "updateModel" to requestBody
                    )
                    findNavController().navigate(R.id.nav_action_orderList_webView, bundle)
                } else {
                    viewModel.updateOrderStatus(requestBody).observe(viewLifecycleOwner, Observer {
                        if (it) {
                            if (!instructions.isNullOrEmpty()) {
                                orderDialog(instructions!!)
                            }
                            viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
                        }
                    })
                }

            } else {

                model.orderList?.forEach { orderModel ->
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
                    }
                    requestBody.add(statusModel)
                }
                instructions = model.collectionSource?.sourceMessageData?.instructions

                viewModel.updateOrderStatus(requestBody).observe(viewLifecycleOwner, Observer {
                    if (it) {
                        if (!instructions.isNullOrEmpty()) {
                            orderDialog(instructions!!)
                        }
                        viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
                    }
                })
            }



            /*if (collectionPointAvailable == 1) {
                collectionDialog(orderModel?.couponId?.toIntOrNull() ?: 0, orderModel?.collectionPointId ?: 0) {
                    viewModel.updateOrderStatus(requestBody).observe(viewLifecycleOwner, Observer {
                        if (it) {
                            viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product)
                        }
                    })
                }
            }*/

        }
        dataAdapter.onPictureClicked = {orderModel ->
            pictureDialog(orderModel)
        }

        //viewModel.loadOrderOrSearch()
        viewModel.pagingState.observe(viewLifecycleOwner, Observer {
            if (it.isInitLoad) {
                Timber.d("pagingState init")
                Timber.d("initData: ${it.dataList}")
                dataAdapter.loadInitData(it.dataList)
                firstCall = 20
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

        viewModel.loadFilterStatus(serviceTye).observe(viewLifecycleOwner, Observer { list->
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
                        //if (selectedStatus != filterStatus) {
                            filterStatus = selectedStatus
                            dtStatus = selectedDTStatus
                            dataAdapter.clearData()
                            Timber.d("loadOrderOrSearch called from filter spinner")

                        binding!!.appBarLayout.filterName.text = model.statusName
                        collectionFlag = model.collectionFilter

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
                        //binding!!.appBarLayout.countTV.text = "০টি"
                            viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)

                        //}

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
                    viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, serviceType = serviceTye)
                }
                binding!!.appBarLayout.searchKey.setOnCloseIconClickListener {
                    binding!!.appBarLayout.searchKey.performClick()
                }
                binding!!.appBarLayout.countTV.text = "০টি"
                viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
            }
            //requireContext().toast(getString(R.string.development))
        }

        binding!!.swipeRefresh.setOnRefreshListener {
            binding!!.swipeRefresh.isRefreshing = false
            Timber.d("loadOrderOrSearch called from swipe refresh")
            viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
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
                        viewModel.loadOrderOrSearch(firstCall, 20, statusId = filterStatus, dtStatusId = dtStatus, flag = collectionFlag, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
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
        }

        /*binding!!.appBarLayout.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val previousFlag = collectionFlag
                collectionFlag = when(tab?.position) {
                    0 -> {
                        dataAdapter.isCollectionPoint = 1
                        1
                    }
                    1 -> {
                        dataAdapter.isCollectionPoint = 0
                        0
                    }
                    else -> 0
                }

                if (previousFlag != collectionFlag) {
                    viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
                }
            }
        })*/

    }

    override fun onStart() {
        super.onStart()
        //Timber.d("onStart called")
        if (filterStatus != "-1") {
            Timber.d("loadOrderOrSearch called from onStart")
            viewModel.loadOrderOrSearch(flag = collectionFlag, statusId = filterStatus, dtStatusId = dtStatus, searchKey = searchKey, type = SearchType.Product, serviceType = serviceTye)
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

    private fun collectionDialog( couponId: Int, collectionPoint: Int = 0, listener: ((type: Int) -> Unit)? = null) {

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

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }
}
