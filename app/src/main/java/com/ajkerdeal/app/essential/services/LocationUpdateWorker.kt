package com.ajkerdeal.app.essential.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.ajkerdeal.app.essential.api.models.user_status.LocationUpdateRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.logInFile
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@KoinApiExtension
class LocationUpdateWorker(private val context: Context, private val parameters: WorkerParameters): ListenableWorker(context, parameters), KoinComponent  {

    private val repository: AppRepository by inject()

    // Location
    private var updateIntervalInMillis = 1000L * 60 // 1 min
    private var fastUpdateIntervalInMillis = updateIntervalInMillis / 2

    private val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            CoroutineScope(Dispatchers.Default).launch {
                getLocation(completer)
            }
        }
    }

    private fun getLocation(completer: CallbackToFutureAdapter.Completer<Result>) {
        if (!isPermissionGranted()) {
            Timber.d("LocationUpdateWorker isPermissionGranted false")
            completer.set(Result.failure(Data.EMPTY))
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val locationRequest = LocationRequest.create().apply {
            interval = updateIntervalInMillis
            fastestInterval = fastUpdateIntervalInMillis
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000L * 60 // 1 min
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val location = result.lastLocation
                Timber.d("LocationUpdateWorker location ${location.latitude}, ${location.longitude}")
                //remove after test
                //logInFile(applicationContext, "${sdf.format(Date().time)} location ${location.latitude}, ${location.longitude}")
                if (SessionManager.userId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val requestBody = LocationUpdateRequest(SessionManager.userId, location.latitude.toString(), location.longitude.toString())
                        when (val response = repository.updateUserLocation(requestBody)) {
                            is NetworkResponse.Success -> {
                                if (response.body.data!! > 0) {
                                    Timber.d("LocationUpdateWorker location synced with server")
                                    completer.set(Result.success(Data.EMPTY))
                                } else {
                                    completer.set(Result.failure(Data.EMPTY))
                                }
                            }
                            is NetworkResponse.ServerError -> {
                                completer.set(Result.failure(Data.EMPTY))
                            }
                            is NetworkResponse.NetworkError -> {
                                completer.set(Result.failure(Data.EMPTY))
                            }
                            is NetworkResponse.UnknownError -> {
                                completer.set(Result.failure(Data.EMPTY))
                            }
                        }
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
            Timber.d("LocationUpdateWorker SecurityException")
        }
    }

    private fun isPermissionGranted(): Boolean {
        return !(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    }

}