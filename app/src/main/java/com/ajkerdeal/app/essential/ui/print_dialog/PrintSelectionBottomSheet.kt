package com.ajkerdeal.app.essential.ui.print_dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.databinding.FragmentPrintSelectionBinding
import com.ajkerdeal.app.essential.utils.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.concurrent.thread

@SuppressLint("SetTextI18n")
class PrintSelectionBottomSheet: BottomSheetDialogFragment(), CompoundButton.OnCheckedChangeListener {

    private var binding: FragmentPrintSelectionBinding? = null
    private lateinit var orderList: List<OrderModel>

    var onPrintClicked: ((orderList: List<OrderModel>) -> Unit)? = null

    private lateinit var dataAdapter: OrderListPrintAdapter

    companion object {
        fun newInstance(orderList: List<OrderModel>): PrintSelectionBottomSheet = PrintSelectionBottomSheet().apply {
            this.orderList = orderList
        }
        val tag: String = PrintSelectionBottomSheet::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onStart() {
        super.onStart()

        val dialog: BottomSheetDialog? = dialog as BottomSheetDialog?
        dialog?.setCanceledOnTouchOutside(true)
        val bottomSheet: FrameLayout? = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        //val metrics = resources.displayMetrics

        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            thread {
                activity?.runOnUiThread {
                    val dynamicHeight = binding?.parent?.height ?: 500
                    BottomSheetBehavior.from(bottomSheet).peekHeight = dynamicHeight
                }
            }
            with(BottomSheetBehavior.from(bottomSheet)) {
                //state = BottomSheetBehavior.STATE_COLLAPSED
                skipCollapsed = false
                isHideable = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPrintSelectionBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataAdapter = OrderListPrintAdapter()
        dataAdapter.initData(orderList)
        with(binding?.recyclerview!!) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dataAdapter
        }

        dataAdapter.onSelected = { selectedCount, totalItemCount ->

            if (selectedCount == totalItemCount) {
                if (binding?.selectAllCheck?.isChecked == false) {
                    binding?.selectAllCheck?.setOnCheckedChangeListener(null)
                    binding?.selectAllCheck?.isChecked = true
                    binding?.selectAllCheck?.setOnCheckedChangeListener(this)
                }
            } else {
                if (binding?.selectAllCheck?.isChecked == true) {
                    binding?.selectAllCheck?.setOnCheckedChangeListener(null)
                    binding?.selectAllCheck?.isChecked = false
                    binding?.selectAllCheck?.setOnCheckedChangeListener(this)
                }
            }
        }

        binding?.selectAllCheck?.setOnCheckedChangeListener(this)

        binding?.printBtn?.setOnClickListener {
            val list = dataAdapter.getSelectedItems()
            if (list.isNotEmpty()) {
                onPrintClicked?.invoke(list)
            } else {
                context?.toast("কমপক্ষে ১টি প্রোডাক্ট সিলেক্ট করুন")
            }
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            dataAdapter.selectAll()
        } else {
            dataAdapter.clearSelections()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}