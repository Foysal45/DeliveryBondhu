package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.app.ProgressDialog
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

            when (actionModel.statusUpdate) {
                // Collect
                44 -> {
                    val bundle = bundleOf(
                        "orderRequestId" to (orderModel?.orderRequestId ?: 0),
                        "collectionTimeSlotId" to (orderModel?.collectionTimeSlotId ?: 0),
                        "courierUserId" to (model.courierUserId),
                        "collectionDistrictId" to model.districtsViewModel.districtId,
                        "collectionThanaId" to model.districtsViewModel.thanaId,
                        "status" to actionModel.statusUpdate
                    )
                    findNavController().navigate(R.id.nav_quickOrderList_orderCollection, bundle)
                }
                else -> {
                    val requestBody: MutableList<QuickOrderStatusUpdateRequest> = mutableListOf()
                    val requestModel = QuickOrderStatusUpdateRequest(
                        orderModel?.orderRequestId ?: 0,
                        SessionManager.dtUserId,
                        actionModel.statusUpdate
                    )
                    requestBody.add(requestModel)
                    updateOrderStatus(requestBody)
                }
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

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

