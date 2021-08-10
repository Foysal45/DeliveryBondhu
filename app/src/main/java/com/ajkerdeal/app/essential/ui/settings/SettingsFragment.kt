package com.ajkerdeal.app.essential.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.*
import com.ajkerdeal.app.essential.databinding.FragmentSettingsBinding
import com.ajkerdeal.app.essential.services.DistrictCacheWorker
import com.ajkerdeal.app.essential.utils.toast
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SettingsFragment: Fragment() {

    private var binding: FragmentSettingsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSettingsBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.syncBtn?.setOnClickListener {
            syncLocationData()
        }

    }

    private fun syncLocationData() {
        binding?.syncBtn?.isEnabled = false
        context?.toast("Syncing data from server")

        val data = Data.Builder()
            .putBoolean("sync", true)
            .build()

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val request = OneTimeWorkRequestBuilder<DistrictCacheWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .addTag("districtSyncNow")
            .build()
        /*val request = PeriodicWorkRequestBuilder<DistrictCacheWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInputData(data)
            .addTag("districtSync").setInitialDelay(5, TimeUnit.SECONDS)
            .build()*/

        val requestUUID = request.id
        val workManager = WorkManager.getInstance(requireContext())
        workManager.beginUniqueWork("districtSyncNow", ExistingWorkPolicy.REPLACE, request).enqueue()
        //workManager.enqueue(request)
        workManager.getWorkInfoByIdLiveData(requestUUID).observe(viewLifecycleOwner, Observer { workInfo ->
            if (workInfo != null) {
                val result = workInfo.outputData.getString("work_result")
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Timber.d("WorkManager getWorkInfoByIdLiveDataObserve onSuccess resultMsg: $result")
                    context?.toast("Successfully data synced from server")
                    binding?.syncBtn?.isEnabled = true
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    Timber.d("WorkManager getWorkInfoByIdLiveDataObserve onFailed resultMsg: $result")
                    context?.toast("Something went wrong when syncing data from server!")
                    binding?.syncBtn?.isEnabled = true
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}