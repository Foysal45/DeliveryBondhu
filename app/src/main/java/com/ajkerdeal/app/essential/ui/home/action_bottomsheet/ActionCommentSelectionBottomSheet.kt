package com.ajkerdeal.app.essential.ui.home.action_bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentActionCommentSelectionBottomSheetBinding
import com.ajkerdeal.app.essential.utils.CustomSpinnerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.concurrent.thread

class ActionCommentSelectionBottomSheet: BottomSheetDialogFragment() {

    private var binding: FragmentActionCommentSelectionBottomSheetBinding? = null
    var onItemSelected: ((comment: String) -> Unit)? = null

    private var selectedType = 0
    private var flag = 0

    companion object {
        fun newInstance(flag: Int): ActionCommentSelectionBottomSheet = ActionCommentSelectionBottomSheet().apply {
            this.flag = flag
        }
        val tag: String = ActionCommentSelectionBottomSheet::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog: BottomSheetDialog? = dialog as BottomSheetDialog?
        dialog?.setCanceledOnTouchOutside(true)
        val bottomSheet: FrameLayout? = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            thread {
                activity?.runOnUiThread {
                    val dynamicHeight = binding?.parent?.height ?: 500
                    BottomSheetBehavior.from(bottomSheet).peekHeight = dynamicHeight
                }
            }
            with(BottomSheetBehavior.from(bottomSheet)) {
                skipCollapsed = true
                isHideable = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentActionCommentSelectionBottomSheetBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSpinner()

    }

    private fun setUpSpinner() {

        val actionCommentList: MutableList<String> = mutableListOf()
        if (flag == 0){
            actionCommentList.clear()
            actionCommentList.add("সিলেক্ট করুন")
            actionCommentList.add("কাস্টমার সিটির বাহিরে পরে জানাবে")
            actionCommentList.add("পর্যাপ্ত টাকা নাই পরে নিবে")
            actionCommentList.add("কাস্টমার বাসরবাহিরে আছে পরে নিবে")
            actionCommentList.add("আগামীকাল প্রোডাক্ট নিবেন")
            actionCommentList.add("কাস্টমার বাসরবাহিরে আছে পরে নিবে")
            actionCommentList.add("কাস্টমার সিটির বাহিরে পরে জানাবে")
            actionCommentList.add("কাস্টমার ফোন রিসিভ করে না")
            actionCommentList.add("কাস্টমার নাম্বার বন্ধ")
            actionCommentList.add("মার্চেন্ট এর সাথে কথা বলে জানাবে")
            actionCommentList.add("কিছুদিন পরে নিবেন")
            actionCommentList.add("ফোন এ পাওয়া যাচ্ছে না")
            actionCommentList.add("পরে জানাবে")
            actionCommentList.add("কাস্টমার ফোন ধরেনি")
        }else{
            actionCommentList.clear()
            actionCommentList.add("সিলেক্ট করুন")
            actionCommentList.add("কাস্টমার আগ্রহী না")
            actionCommentList.add("কাস্টমার অর্ডার করেনি")
            actionCommentList.add("কাস্টমার এর ঠিকানা ভুল")
            actionCommentList.add("কাস্টমার এর নম্বর ভুল")
            actionCommentList.add("কাস্টমার পছন্দ করে নি")
            actionCommentList.add("কাস্টমার সিটির বাহিরে")
            actionCommentList.add("কাস্টমার সিদ্ধান্ত পরিবর্তন করেছে")
            actionCommentList.add("দেরিতে ডেলিভারি দেয়ার জন্য প্রোডাক্ট নিবে না")
            actionCommentList.add("নষ্ট প্রোডাক্ট")
            actionCommentList.add("প্রোডাক্ট এর মিল পাওয়া যায় নি")
            actionCommentList.add("প্রোডাক্ট কোয়ালিটি খারাপ")
            actionCommentList.add("মুল্য ঠিক নেই")
            actionCommentList.add("সাইজ ঠিক নাই")
        }
        val spinnerAdapter = CustomSpinnerAdapter(requireContext(), R.layout.item_view_spinner_item, actionCommentList)
        binding?.spinnerActionType?.adapter = spinnerAdapter
        binding?.spinnerActionType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedType = position
                if (position != 0){
                    onItemSelected?.invoke(actionCommentList[position])
                }

            }
        }

    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}