package com.ajkerdeal.app.essential.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentResetPasswordBinding
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import org.koin.android.ext.android.inject

class ResetPasswordFragment : Fragment() {

    private var binding: FragmentResetPasswordBinding? = null

    private val viewModel: AuthViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_reset_password, container, false)
        return FragmentResetPasswordBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.viewModel = viewModel

        val title = arguments?.getString("title", "") ?: ""
        val otpType = arguments?.getInt("OTPType", 1) ?: 1

        viewModel.otpType = otpType
        binding?.title?.text = title

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ViewState.ShowMessage -> {
                    requireContext().toast(state.message)
                }
                is ViewState.KeyboardState -> {
                    hideKeyboard()
                }
                is ViewState.NextState -> {
                    findNavController().navigate(R.id.action_resetPassword_OTPVerify)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
        binding = null
    }

}
