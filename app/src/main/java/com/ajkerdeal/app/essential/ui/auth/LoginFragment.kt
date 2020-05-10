package com.ajkerdeal.app.essential.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentLoginBinding
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import org.koin.android.ext.android.inject

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    private val viewModel: AuthViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_login, container, false)
        return FragmentLoginBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.viewModel = viewModel

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ViewState.ShowMessage -> {
                    requireContext().toast(state.message)
                }
                is ViewState.KeyboardState -> {
                    hideKeyboard()
                }
                is ViewState.NextState -> {
                    (activity as LoginActivity).goToHome()
                }
            }
        })

        binding?.registration?.setOnClickListener {
            findNavController().navigate(R.id.action_login_signUp)
        }

        binding?.forgetPassword?.setOnClickListener {
            findNavController().navigate(R.id.action_login_resetPassword)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
        binding = null
    }

}
