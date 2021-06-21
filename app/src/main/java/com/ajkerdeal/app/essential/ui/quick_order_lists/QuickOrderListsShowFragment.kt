package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderListRequesst
import com.ajkerdeal.app.essential.databinding.FragmentQuickOrderListsShowBinding
import com.ajkerdeal.app.essential.ui.quick_order_scan.QuickOrderViewModel
import com.ajkerdeal.app.essential.utils.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class QuickOrderListsShowFragment : Fragment() {

    private var binding: FragmentQuickOrderListsShowBinding? = null
    private var dataAdapter: QuickOrderListParentAdapter = QuickOrderListParentAdapter()
    private val viewModel: QuickOrderViewModel by inject()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentQuickOrderListsShowBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initClickLister()
        fetchData()

    }

    private fun initView(){
        binding?.recyclerView?.let { view ->
            with(view) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = dataAdapter
            }
        }
    }

    private fun initClickLister(){
        binding!!.swipeRefresh.setOnRefreshListener {
            binding!!.swipeRefresh.isRefreshing = false
            if (SessionManager.isOffline) return@setOnRefreshListener
            Timber.d("loadOrderOrSearch called from swipe refresh")
            fetchData()

        }

        /*dataAdapter.onOrderListExpand = { parentModel, state ->
            if (state) {
                if (!isValidCoordinate(parentModel.latitude) || !isValidCoordinate(parentModel.longitude)) {
                    if (!parentModel.isLocationUpdated) {
                        updateMerchantLocation(parentModel)
                        parentModel.isLocationUpdated = true
                    }
                }
            }
        }*/
    }

    private fun fetchData(){
        if (SessionManager.isOffline) {
            binding!!.emptyView.text = "আপনি এখন Unavailable আছেন"
            binding!!.emptyView.visibility = View.VISIBLE
        } else {
            viewModel.getQuickOrders(QuickOrderListRequesst(14)).observe(viewLifecycleOwner, Observer { quickOrderList->
                Timber.d("debugData fetchData$quickOrderList")
                dataAdapter.loadInitData(quickOrderList)
            })
        }
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }
}

