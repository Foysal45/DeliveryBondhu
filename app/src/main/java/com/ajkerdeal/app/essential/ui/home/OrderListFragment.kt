package com.ajkerdeal.app.essential.ui.home

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentOrderListBinding
import com.ajkerdeal.app.essential.ui.auth.LoginActivity
import com.ajkerdeal.app.essential.utils.*
import kotlinx.android.synthetic.main.fragment_order_list.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrderListBinding

    private val viewModel: HomeViewModel by inject()

    private var dialog: ProgressDialog? = null

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
        dataAdapter.onActionClicked = { model ->
            viewModel.updateOrderStatus()
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
                            binding.progressBar.visibility = View.VISIBLE
                        } else {
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                }
            }
        })

        binding.acceptFilterSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                requireContext().toast(getString(R.string.development))
        }

        binding.searchBtn.setOnClickListener {
            hideKeyboard()
            val key = binding.searchET.text.toString()
            if (key.isNotEmpty()) {
                binding.chipsGroup.visibility = View.VISIBLE
                searchKey.text = key
                searchKey.setOnClickListener {
                    binding.searchET.text.clear()
                    binding.chipsGroup.visibility = View.GONE
                    viewModel.loadOrderOrSearch()
                }

                viewModel.loadOrderOrSearch(searchKey = key, type = SearchType.Product)
            }
            //requireContext().toast(getString(R.string.development))
        }

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

        binding.logoutBtn.setOnClickListener {
            SessionManager.clearSession()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

}
