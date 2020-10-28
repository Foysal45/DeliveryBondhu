package com.ajkerdeal.app.essential.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.ProgressRequestBody
import com.ajkerdeal.app.essential.api.models.update_doc.UpdateDocRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.exhaustive
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageUploadWorker(private val context: Context, private val parameters: WorkerParameters): CoroutineWorker(context, parameters), KoinComponent, ProgressRequestBody.UploadCallback {

    private val repository: AppRepository by inject()
    private val gson: Gson by inject()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationId: Int = 23220
    private var maxProgress: Int = 100
    private var isSuccess: Boolean = false
    private var resultMsg: String? = ""
    private val mediaTypeMultipart = "multipart/form-data".toMediaTypeOrNull()
    private val mediaTypeText = "text/plain".toMediaTypeOrNull()
    private var isWorkerDone = false
    private var serverImageUrl: String = ""
    private var sdf = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.US)

    override suspend fun doWork(): Result {
        createNotification()

        val data = parameters.inputData
        val imageUrl = data.getString("imageUrl") ?: ""
        val merchantId = data.getString("merchantId") ?: "merchantId"
        val orderIds = data.getString("orderIds") ?: "orderIds"

        val file = File(imageUrl)
        if (file.exists()) {


            val compressedFile = Compressor.compress(context, file)
            val fileName = "${merchantId}_${sdf.format(Date())}.${compressedFile.extension}"

            val requestFile = ProgressRequestBody(compressedFile, mediaTypeMultipart, this)
            val fileMultipart = MultipartBody.Part.createFormData("Img", fileName, requestFile)

            val imageUrlRequest = "images/returnproducts".toRequestBody(mediaTypeText)
            val fileNameRequest = fileName.toRequestBody(mediaTypeText)

            val response = repository.imageUpload(imageUrlRequest, fileNameRequest, fileMultipart)
            when (response) {
                is NetworkResponse.Success -> {
                    if (response.body) {
                        resultMsg = "সফলভাবে ফাইল আপলোড হয়েছে"
                        serverImageUrl = "https://static.ajkerdeal.com/images/returnproducts/${fileName}"
                        Timber.d("serverImageUrl $serverImageUrl")

                        val response1 = repository.updateDocumentUrl(UpdateDocRequest(orderIds, serverImageUrl))
                        when (response1) {
                            is NetworkResponse.Success -> {
                                if (response1.body.data == true) {
                                    isSuccess = true
                                    resultMsg = "সফলভাবে ফাইল আপলোড হয়েছে"
                                } else {
                                    resultMsg = "কোথাও কোনো সমস্যা হচ্ছে"
                                }
                            }
                            is NetworkResponse.ServerError -> {
                                resultMsg = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                            }
                            is NetworkResponse.NetworkError -> {
                                resultMsg = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                            }
                            is NetworkResponse.UnknownError -> {
                                resultMsg = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                                Timber.d(response1.error)
                            }
                        }


                    } else {
                        resultMsg = "কোথাও কোনো সমস্যা হচ্ছে"
                    }
                }
                is NetworkResponse.ServerError -> {
                    resultMsg = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                }
                is NetworkResponse.NetworkError -> {
                    resultMsg = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                }
                is NetworkResponse.UnknownError -> {
                    resultMsg = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                    Timber.d(response.error)
                }
            }.exhaustive
        }


        return if (isSuccess) {
            isWorkerDone = true
            completeProgress("Completed")
            notificationManager.cancel(notificationId)
            val outputData = Data.Builder()
                .putString("work_result", "$resultMsg")
                .putString("serverImageUrl", serverImageUrl)
                .build()
            Timber.d("outputData resultMsg: $resultMsg serverImageUrl: $serverImageUrl")
            Result.success(outputData)
        } else {
            isWorkerDone = true
            completeProgress("Failed")
            val outputData = Data.Builder().putString("work_result", "$resultMsg").build()
            Result.failure(outputData)
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        updateProgress(percentage)
    }

    private fun createNotification() {

        val id = "channel2"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, "Product uploader", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle("Uploading file")
            .setTicker(context.getString(R.string.app_name))
            .setContentText("please wait")
            .setSmallIcon(R.drawable.ic_logo_hand)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(10, 0, true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateProgress(progress: Int) {
        if (isWorkerDone) {
            notificationManager.cancel(notificationId)
            return
        }
        notificationBuilder.setContentText("$progress/$maxProgress")
        notificationBuilder.setOngoing(true)
        notificationBuilder.setProgress(maxProgress, progress, false)
        if (progress == 100) {
            notificationBuilder.setContentText("Processing")
            notificationBuilder.setProgress(maxProgress, progress, true)
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun completeProgress(msg: String = "") {
        notificationBuilder.setContentText(msg)
        notificationBuilder.setOngoing(false)
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}