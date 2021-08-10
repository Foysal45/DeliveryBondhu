package com.ajkerdeal.app.essential.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.repository.AppRepository
import com.haroldadmin.cnradapter.NetworkResponse
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
class DistrictCacheWorker(private val context: Context, private val parameters: WorkerParameters): CoroutineWorker(context, parameters), KoinComponent {

    private val repository: AppRepository by inject()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationId: Int = 10821
    private var isSuccess: Boolean = false
    private var resultMsg: String? = ""

    override suspend fun doWork(): Result {

        val data = parameters.inputData

        createNotification()

        syncDistrictData()

        return if (isSuccess) {
            notificationManager.cancel(notificationId)
            val outputData = Data.Builder()
                .putString("work_result", "$resultMsg")
                .build()
            Timber.d("outputData resultMsg: $resultMsg")
            Result.success(outputData)
        } else {
            val outputData = Data.Builder().putString("work_result", "$resultMsg").build()
            Result.failure(outputData)
        }
    }

    private suspend fun syncDistrictData() {

        val response = repository.loadAllDistricts()
        if (response is NetworkResponse.Success) {
            val dataList = response.body.model
            Timber.d("districtList ${dataList.map { it.districtId }}")
            repository.deleteAllDistrict()
            repository.insert(dataList)
            isSuccess = true
        }
    }

    private suspend fun createNotification() {

        val id = "channel3"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, "District Sync", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle("District sync")
            .setTicker(context.getString(R.string.app_name))
            .setContentText("please wait")
            .setSmallIcon(R.drawable.ic_logo_hand)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(10, 0, true)

        //notificationManager.notify(notificationId, notificationBuilder.build())

        val foregroundInfo = ForegroundInfo(notificationId,notificationBuilder.build())
        setForeground(foregroundInfo)
    }
}