package com.ajkerdeal.app.essential.ui.home.weight_selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentWeightSelectionBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.concurrent.thread

class WeightSelectionBottomSheet(): BottomSheetDialogFragment() {

    private var binding: FragmentWeightSelectionBottomSheetBinding? = null
    private val viewModel: WeightSelectionViewModel by inject()
    private var dataAdapter: WeightSelectionAdapter = WeightSelectionAdapter()
    var onActionClicked: ((action: Int) -> Unit)? = null

    companion object {
        fun newInstance(): WeightSelectionBottomSheet = WeightSelectionBottomSheet().apply {

        }
        val tag: String = WeightSelectionBottomSheet::class.java.name
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
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            thread {
                activity?.runOnUiThread {
                    val dynamicHeight = binding?.parent?.height ?: 500
                    BottomSheetBehavior.from(bottomSheet).peekHeight = dynamicHeight
                }
            }
            with(BottomSheetBehavior.from(bottomSheet)) {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = false

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return FragmentWeightSelectionBottomSheetBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView?.let { recyclerView ->
            with(recyclerView) {
                setHasFixedSize(false)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = dataAdapter
               // addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            }
        }

        viewModel.fetchWeightRange().observe(viewLifecycleOwner, Observer { lists->
            dataAdapter.loadInitData(lists)

        })

        dataAdapter.onItemClicked = { model->
            Timber.d("ClickedWeight ${model.weight}")
        }

    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}