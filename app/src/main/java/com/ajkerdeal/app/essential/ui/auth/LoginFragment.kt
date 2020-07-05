package com.ajkerdeal.app.essential.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.databinding.FragmentLoginBinding
import com.ajkerdeal.app.essential.ui.webview.ChromeCustomTabBrowser
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import com.bumptech.glide.Glide
import com.google.firebase.iid.FirebaseInstanceId
import org.koin.android.ext.android.inject
import timber.log.Timber

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    private val viewModel: AuthViewModel by inject()
    private var webUrl: String = ""

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

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                viewModel.firebaseToken.value = token ?: ""
                Timber.d("FirebaseToken:\n${token}")
            }
        }

        viewModel.features().observe(viewLifecycleOwner, Observer {

            Glide.with(requireContext())
                .load(it.bannerUrl)
                .into(binding!!.bannerIV)

            webUrl = it.webUrl ?: "http://deliverybondhu.com/"

        })

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
            //findNavController().navigate(R.id.action_login_signUp)
            val bundle = bundleOf(
                "title" to "রেজিস্ট্রেশন",
                "OTPType" to 2
            )
            findNavController().navigate(R.id.action_login_resetPassword, bundle)
        }

        binding?.forgetPassword?.setOnClickListener {
            val bundle = bundleOf(
                "title" to "পাসওয়ার্ড রিসেট",
                "OTPType" to 1
            )
            findNavController().navigate(R.id.action_login_resetPassword, bundle)
        }

        binding?.bannerIV?.setOnClickListener {
            goToWeb(webUrl)
        }
        /*binding?.moreDetails?.setOnClickListener {
            goToWeb(webUrl)
        }*/
    }

    private fun goToWeb(url: String) {

        //val url = "http://deliverybondhu.com/"
        ChromeCustomTabBrowser.launch(requireContext(), url) { fallbackUrl ->
            findNavController().navigate(R.id.nav_action_login_webView, bundleOf(
                "url" to url,
                "title" to "ফিচারস"
            ))
        }
    }


    override fun onDestroyView() {
        viewModel.clearLogin()
        super.onDestroyView()
        binding?.unbind()
        binding = null
    }

}
