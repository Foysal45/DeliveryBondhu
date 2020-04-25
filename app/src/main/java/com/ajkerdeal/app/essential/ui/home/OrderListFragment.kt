package com.ajkerdeal.app.essential.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajkerdeal.app.essential.databinding.FragmentOrderListBinding
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import org.koin.android.ext.android.inject
import timber.log.Timber

class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrderListBinding

    private val viewModel: HomeViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_order_list, container, false)
        return FragmentOrderListBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dataAdapter = OrderListParentAdapter()
        val layoutManagerLinear = LinearLayoutManager(requireContext())
        with(binding.recyclerView) {
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

        viewModel.loadOrderOrSearch()
        viewModel.pagingState.observe(viewLifecycleOwner, Observer {
            if (it.isInitLoad) {
                Timber.d("initData: ${it.dataList}")
                dataAdapter.loadInitData(it.dataList)
            } else {
                Timber.d("loadMoreData: ${it.dataList}")
                dataAdapter.loadMoreData(it.dataList)
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
            }
        })
    }

}
