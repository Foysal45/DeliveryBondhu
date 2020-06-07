package com.ajkerdeal.app.essential.ui.webview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.repository.AppRepository
import org.koin.android.ext.android.inject
import timber.log.Timber

class WebViewFragment: Fragment() {

    private var url: String = ""
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    private val repository: AppRepository by inject()

    companion object {
        fun newInstance(url: String): WebViewFragment = WebViewFragment().apply {
            this.url = url
        }
        val tag: String = WebViewFragment::class.java.name
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webView)
        progressBar = view.findViewById(R.id.progressBar)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (url.isEmpty()) {
            url = arguments?.getString("url", "") ?: ""
            val requestBody: MutableList<StatusUpdateModel> = arguments?.getParcelableArrayList("updateModel") ?: mutableListOf()
            Timber.d("webView URL: $url")
            Timber.d("webView requestBody: $requestBody")
        }

        //url = "https://m.ajkerdeal.com/msingleorder/bkashtokenizedcheckoutforapp.aspx?CID=3845773&totalPoint=69011&vId=0&vType=0"

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        with(webView) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearHistory()
            isHorizontalScrollBarEnabled = false
            clearCache(true)
            addJavascriptInterface(WebAppInterface(requireContext(), repository, arguments), "Android")
            webViewClient = Callback()
        }

        webView.loadUrl(url)
    }


    inner class Callback : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            // Url base logic here
            /*val url = request?.url?.path
            if (url?.startsWith("intent://scan/") == true) {
                // Do Stuff
                return true
            }*/
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            Timber.d(error.toString())
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)

            val builder = AlertDialog.Builder(requireContext())
            var message = "SSL Certificate error."
            message = when (error?.primaryError) {
                SslError.SSL_UNTRUSTED -> "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> "The certificate has expired."
                SslError.SSL_IDMISMATCH -> "The certificate Hostname mismatch."
                SslError.SSL_NOTYETVALID -> "The certificate is not yet valid."
                else -> "SSL Error."
            }
            message += " Do you want to continue anyway?"

            builder.setTitle("SSL Certificate Error")
            builder.setMessage(message)
            builder.setPositiveButton("continue") { dialog, which -> handler?.proceed() }
            builder.setNegativeButton("cancel") { dialog, which -> handler?.cancel() }
            val dialog = builder.create()
            dialog.show()
        }

    }
}