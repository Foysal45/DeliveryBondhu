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
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.repository.AppRepository

import com.ajkerdeal.app.essential.utils.SessionManager
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


class ProfileUpdateWorker(private val context: Context, private val parameters: WorkerParameters): CoroutineWorker(context, parameters), KoinComponent, ProgressRequestBody.UploadCallback {

    private val repository: AppRepository by inject()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationId: Int = 23220
    private var maxProgress: Int = 100
    private var isSuccess: Boolean = false
    private var resultMsg: String? = ""
    private val mediaTypeMultipart = "multipart/form-data".toMediaTypeOrNull()
    private val mediaTypeText = "text/plain".toMediaTypeOrNull()
    private val gson = Gson()

    override suspend fun doWork(): Result {
        createNotification()

        val data = parameters.inputData
        val model = data.getString("Data") ?: "{\"BondhuId\":${SessionManager.userId}}"
        val profileUri = data.getString("profileUri") ?: ""
        val nidUri = data.getString("nidUri") ?: ""
        val drivingUri = data.getString("drivingUri") ?: ""

        Timber.d("model: $model profileUri: $profileUri nidUri: $nidUri drivingUri: $drivingUri")

        try {
            val responseBodyModel: ProfileData = gson.fromJson(model, ProfileData::class.java)
            val responseProfileData = repository.updateProfile(responseBodyModel)
            when (responseProfileData) {
                is NetworkResponse.Success -> {
                    resultMsg = responseProfileData.body.data?.message
                    isSuccess = true
                }
                is NetworkResponse.ServerError -> {
                    resultMsg = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                }
                is NetworkResponse.NetworkError -> {
                    resultMsg = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                }
                is NetworkResponse.UnknownError -> {
                    resultMsg = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                    Timber.d(responseProfileData.error)
                }
            }.exhaustive
        } catch (e: Exception) {
            Timber.d(e)
        }

        if (profileUri.isNotEmpty() || nidUri.isNotEmpty() || drivingUri.isNotEmpty()) {
            var file1: MultipartBody.Part? = null
            var file2: MultipartBody.Part? = null
            var file3: MultipartBody.Part? = null

            val dataRequestBody = model.toRequestBody(mediaTypeText)

            if (profileUri.isNotEmpty()){
                val file = File(profileUri)
                if (file.exists()) {
                    val compressedFile = Compressor.compress(context, file)
                    //val requestFile = compressedFile.asRequestBody(mediaTypeMultipart)
                    val requestFile = ProgressRequestBody(compressedFile, mediaTypeMultipart, this)
                    file1 = MultipartBody.Part.createFormData("file1", "profileimage.jpg", requestFile)
                }
            }

            if (nidUri.isNotEmpty()){
                val file = File(nidUri)
                if (file.exists()) {
                    val compressedFile = Compressor.compress(context, file)
                    //val requestFile = compressedFile.asRequestBody(mediaTypeMultipart)
                    val requestFile = ProgressRequestBody(compressedFile, mediaTypeMultipart, this)
                    file2 = MultipartBody.Part.createFormData("file2", "nid.jpg", requestFile)
                }
            }

            if (drivingUri.isNotEmpty()){
                val file = File(drivingUri)
                if (file.exists()) {
                    val compressedFile = Compressor.compress(context, file)
                    //val requestFile = compressedFile.asRequestBody(mediaTypeMultipart)
                    val requestFile = ProgressRequestBody(compressedFile, mediaTypeMultipart, this)
                    file3 = MultipartBody.Part.createFormData("file3", "drivinglicense.jpg", requestFile)
                }
            }

            val response = repository.updateProfile(dataRequestBody, file1, file2, file3)
            when (response) {
                is NetworkResponse.Success -> {
                    if (response.body.data == true) {
                        resultMsg = "প্রোফাইল আপডেট হয়েছে"
                        isSuccess = true
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

        //delay(5000)

        return if (isSuccess) {
            completeProgress("Completed")
            notificationManager.cancel(notificationId)
            val outputData = Data.Builder().putString("work_result", "$resultMsg").build()
            Result.success(outputData)
        } else {
            completeProgress("Failed")
            val outputData = Data.Builder().putString("work_result", "$resultMsg").build()
            Result.failure(outputData)
        }
    }

    private suspend fun updateProfile() {


    }

    private fun createNotification(){

        val id = "channel1"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, "Profile Update", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle("Updating Profile")
            .setTicker("Delivery Bondhu")
            .setContentText("please wait")
            .setSmallIcon(R.drawable.ic_logo_hand)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(10, 0, true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateProgress(progress: Int) {
        notificationBuilder.setContentText("$progress/$maxProgress")
        notificationBuilder.setProgress(maxProgress, progress, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun completeProgress(msg: String = ""){
        notificationBuilder.setContentText(msg)
        notificationBuilder.setOngoing(false)
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.d("updateProgress $percentage")
        updateProgress(percentage)
    }

}