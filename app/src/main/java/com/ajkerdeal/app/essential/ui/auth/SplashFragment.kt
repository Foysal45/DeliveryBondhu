package com.ajkerdeal.app.essential.ui.auth

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.utils.SessionManager


class SplashFragment : Fragment() {

    private var isTimeOut: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Handler().postDelayed({
            isTimeOut = true
            goToDestination()
        }, 2000L)
    }

    override fun onResume() {
        super.onResume()
        if (isTimeOut) {
            goToDestination()
        }
    }

    private fun goToDestination() {

        if (SessionManager.isLogin) {
            (activity as LoginActivity).goToHome()
        } else {
            findNavController().navigate(R.id.action_splash_login)
        }
    }


}
