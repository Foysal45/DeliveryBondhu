package com.ajkerdeal.app.essential.ui.home.parcel

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.databinding.FragmentParcelListBinding
import com.ajkerdeal.app.essential.ui.home.HomeViewModel
import com.ajkerdeal.app.essential.ui.home.OrderListParentAdapter
import com.ajkerdeal.app.essential.utils.SearchType
import com.ajkerdeal.app.essential.utils.hideKeyboard
import org.koin.android.ext.android.inject
import timber.log.Timber

class ParcelListFragment : Fragment() {

    private lateinit var binding: FragmentParcelListBinding
    private val viewModel: HomeViewModel by inject()

    private var dialog: ProgressDialog? = null

    // Lazy loading
    private var isLoading = false
    private val visibleThreshold = 5
    private var totalCount: Int = 0
    private var firstCall: Int = 0

    private var searchKey: String = "-1"
    private var flagAccepted: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_parcel_list, container, false)
        return FragmentParcelListBinding.inflate(inflater, container, false).also {
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

        binding.acceptFilterSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            flagAccepted = if (isChecked) 1 else 0
            viewModel.loadOrderOrSearch(flag = flagAccepted, searchKey = searchKey, type = SearchType.Product)
        }

        binding.searchBtn.setOnClickListener {
            hideKeyboard()
            searchKey = binding.searchET.text.toString()
            if (searchKey.isNotEmpty()) {
                binding.chipsGroup.visibility = View.VISIBLE
                binding.searchKey.text = searchKey
                binding.searchKey.setOnClickListener {
                    searchKey = "-1"
                    binding.searchET.text.clear()
                    binding.chipsGroup.visibility = View.GONE
                    viewModel.loadOrderOrSearch(flag = flagAccepted)
                }
                binding.searchKey.setOnCloseIconClickListener {
                    binding.searchKey.performClick()
                }

                viewModel.loadOrderOrSearch(flag = flagAccepted, searchKey = searchKey, type = SearchType.Product)
            }
            //requireContext().toast(getString(R.string.development))
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            viewModel.loadOrderOrSearch(flag = flagAccepted, searchKey = searchKey, type = SearchType.Product)
            /*binding.searchET.text.clear()
            if (binding.chipsGroup.visibility == View.VISIBLE) {
                binding.chipsGroup.visibility = View.GONE
            }*/
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    Timber.d("addOnScrollListener onScrolled: $dx $dy")
                    val currentItemCount = layoutManagerLinear.itemCount
                    val lastVisibleItem = layoutManagerLinear.findLastVisibleItemPosition()
                    Timber.d("onScrolled: \nItemCount: $currentItemCount  <= lastVisible: $lastVisibleItem firstCall : $firstCall  < TotalDeal : $totalCount  ${!isLoading}")
                    if (!isLoading && currentItemCount <= lastVisibleItem + visibleThreshold && firstCall < totalCount) {
                        isLoading = true
                        viewModel.loadOrderOrSearch(firstCall, 20, flag = flagAccepted, searchKey = searchKey, type = SearchType.Product)
                    }
                }
            }
        })

        binding.searchET.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->

            val imeAction = when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH -> true
                else -> false
            }
            //val eventType = event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            return@OnEditorActionListener if (imeAction) {
                binding.searchBtn.performClick()
                true
            } else false
        })
    }

}
