package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderRequest
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatusUpdateRequest
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderListBinding
import com.ajkerdeal.app.essential.ui.quick_order_scan.QuickOrderViewModel
import com.ajkerdeal.app.essential.utils.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class QuickOrderListFragment : Fragment() {

    private var binding: FragmentQuickOrderListBinding? = null
    private var dataAdapter: QuickOrderListParentAdapter = QuickOrderListParentAdapter()
    private val viewModel: QuickOrderViewModel by inject()

    private var selectedOrderStatus: Int = 0
    private var dialog: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentQuickOrderListBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        fetchFilterData()
        initClickLister()
    }

    private fun initView(){

        binding?.recyclerView?.let { view ->
            with(view) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = dataAdapter
            }
        }

        viewModel.pagingState.observe(viewLifecycleOwner, Observer { state ->
            if (state.isInitLoad) {
                val list = state.dataList
                dataAdapter.loadInitData(list)
                if (list.isEmpty()) {
                    binding?.emptyView?.isVisible = true
                    binding?.emptyView?.text = "এই মুহূর্তে কোনো অর্ডার নেই"
                } else {
                    binding?.emptyView?.isVisible = false
                }
            }
        })

    }

    private fun initClickLister(){

        dataAdapter.onActionClicked = { model, actionModel, orderModel ->
            if (orderModel != null) {
                when (actionModel.statusUpdate) {
                    // Collect
                    44 -> {
                        val bundle = bundleOf(
                            "orderRequestSelfList" to (orderModel.orderRequestSelfList),
                            "requestOrderAmountTotal" to orderModel.requestOrderAmount,
                            "collectionTimeSlotId" to (orderModel.collectionTimeSlot.collectionTimeSlotId),
                            "courierUserId" to (model.courierUserId),
                            "collectionDistrictId" to model.districtsViewModel.districtId,
                            "collectionThanaId" to model.districtsViewModel.thanaId,
                            "status" to actionModel.statusUpdate,
                            "operationFlag" to 1
                        )
                        findNavController().navigate(R.id.nav_quickOrderList_orderCollection, bundle)
                    }
                    else -> {
                        val requestBody: MutableList<QuickOrderStatusUpdateRequest> = mutableListOf()
                        val requestModel = QuickOrderStatusUpdateRequest(
                            orderModel.orderRequestId ?: 0,
                            SessionManager.dtUserId,
                            actionModel.statusUpdate
                        )
                        requestBody.add(requestModel)
                        updateOrderStatus(requestBody)
                    }
                }
            } else {
                when (actionModel.statusUpdate) {
                    // Collect
                    44 -> {
                        val bundle = bundleOf(
                            "orderRequestSelfList" to (model.orderRequestList.first().orderRequestSelfList),
                            "requestOrderAmountTotal" to model.orderRequestList.first().requestOrderAmount,
                            "collectionTimeSlotId" to (model.orderRequestList.first().collectionTimeSlot.collectionTimeSlotId),
                            "courierUserId" to (model.courierUserId),
                            "collectionDistrictId" to model.districtsViewModel.districtId,
                            "collectionThanaId" to model.districtsViewModel.thanaId,
                            "status" to actionModel.statusUpdate,
                            "operationFlag" to 1
                        )
                        findNavController().navigate(R.id.nav_quickOrderList_orderCollection, bundle)
                    }
                    else -> {
                        val requestBody: MutableList<QuickOrderStatusUpdateRequest> = mutableListOf()
                        model.orderRequestList.forEach { orderModel ->
                            orderModel.orderRequestSelfList.forEach { orderRequest ->
                                val requestModel = QuickOrderStatusUpdateRequest(
                                    orderRequest.orderRequestId,
                                    SessionManager.dtUserId,
                                    actionModel.statusUpdate
                                )
                                requestBody.add(requestModel)
                            }
                        }
                        updateOrderStatus(requestBody)
                    }
                }
            }

        }

        dataAdapter.onCall = { number, altNumber ->
            if (!number.isNullOrEmpty() && !altNumber.isNullOrEmpty()) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("কোন নাম্বার এ কল করতে চান")
                val numberLists = arrayOf(number, altNumber)
                builder.setItems(numberLists) { _, which ->
                    when (which) {
                        0 -> {
                            goToCallOption(numberLists[0])
                        }
                        1 -> {
                            goToCallOption(numberLists[1])
                        }
                    }
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                goToCallOption(number!!)
            }
        }

        binding?.swipeRefresh?.setOnRefreshListener {
            binding?.swipeRefresh?.isRefreshing = false
            if (SessionManager.isOffline) return@setOnRefreshListener
            Timber.d("loadOrderOrSearch called from swipe refresh")
            fetchOrderData(selectedOrderStatus)
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
                        binding?.progressBar?.isVisible = state.isShow
                    }
                }

            }
        })

    }

    private fun fetchFilterData() {
        viewModel.fetchQuickOrderStatus().observe(viewLifecycleOwner, Observer { list ->

            val statusName = list.map { it.buttonName }
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_item_selected, statusName)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding?.spinner?.adapter = arrayAdapter

            binding?.spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == -1) return
                    val model = list[position]
                    selectedOrderStatus = model.statusUpdate

                    fetchOrderData(selectedOrderStatus)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        })
    }

    private fun fetchOrderData(orderStatus: Int) {
        if (SessionManager.isOffline) {
            binding?.emptyView?.text = "আপনি এখন Unavailable আছেন"
            binding?.emptyView?.isVisible = true
        } else {
            viewModel.getQuickOrders(QuickOrderRequest(SessionManager.dtUserId, orderStatus))
        }
    }

    private fun updateOrderStatus(requestBody: List<QuickOrderStatusUpdateRequest>) {

        viewModel.updateQuickOrderStatus(requestBody).observe(viewLifecycleOwner, Observer { flag ->
            if (flag) {
                context?.toast("Order updated")
                fetchOrderData(selectedOrderStatus)
            }
        })

    }

    private fun goToCallOption(number: String) {
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

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

