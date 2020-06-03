package com.ajkerdeal.app.essential.ui.webview

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.ajkerdeal.app.essential.R
import timber.log.Timber

object ChromeCustomTabBrowser {

    fun launch(context: Context, url: String, block: ((fallbackUrl: String) -> Unit)? = null) {

        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
        builder.setShowTitle(true)
        builder.addDefaultShareMenuItem()
        val customTabsIntent = builder.build()
        val customTabHelper = CustomTabHelper()
        val packageName = customTabHelper.getPackageNameToUse(context)
        Timber.d("customTabHelper $packageName")
        if (packageName != null) {
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } else {
            block?.invoke(url)
            openBrowser(context, url)
        }
    }

    fun openBrowser(context: Context, url: String){
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d(e)
        }
    }

}