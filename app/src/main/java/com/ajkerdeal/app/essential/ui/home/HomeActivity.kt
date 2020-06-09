package com.ajkerdeal.app.essential.ui.home

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ajkerdeal.app.essential.BuildConfig
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.broadcast.ConnectivityReceiver
import com.ajkerdeal.app.essential.services.LocationUpdatesService
import com.ajkerdeal.app.essential.ui.auth.LoginActivity
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.snackbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.inject
import timber.log.Timber

class HomeActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false

    private val viewModel: HomeActivityViewModel by inject()

    private val PERMISSION_REQUEST_CODE = 8620
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var parent: CoordinatorLayout
    private lateinit var receiver: MyReceiver
    private var foregroundService: LocationUpdatesService? = null
    private var mBound: Boolean = false
    private var toggle: Boolean = false
    private var snackBar: Snackbar? = null
    private lateinit var connectivityReceiver : ConnectivityReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        parent = findViewById(R.id.parent)
        navController = findNavController(R.id.nav_host_fragment)
        FirebaseMessaging.getInstance().subscribeToTopic("EssentialDeliveryTopic")
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                SessionManager.firebaseToken = token ?: ""
                Timber.d("FirebaseToken:\n${token}")
            }
        }

        if (!SessionManager.isLogin) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        receiver = MyReceiver()
        connectivityReceiver = ConnectivityReceiver()
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.nav_dashboard) {
            super.onBackPressed()
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "অ্যাপটি বন্ধ করতে আবার প্রেস করুন", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000L)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is TextInputEditText || view is EditText) {
                if (!isPointInsideView(event.rawX, event.rawY, view)) {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        // point is inside view bounds
        return ((x > viewX && x < (viewX + view.width)) && (y > viewY && y < (viewY + view.height)))
    }

    fun logout() {

        viewModel.clearFirebaseToken(SessionManager.userId)
        SessionManager.clearSession()
        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun locationToggle() {
        if (!toggle) {
            if (isLocationPermission()) {
                foregroundService?.requestLocationUpdates()
                toggle = true
            }
        } else {
            foregroundService?.removeLocationUpdates()
            toggle = false
        }
    }

    fun startLocationUpdate(intervalInMinute: Int = 20) {
        if (isLocationPermission()) {
            foregroundService?.setLocationInterval(intervalInMinute)
            foregroundService?.requestLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(LocationUpdatesService.ACTION_BROADCAST))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, LocationUpdatesService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onStop() {
        if (mBound) {
            unbindService(serviceConnection)
            mBound = false
        }
        ConnectivityReceiver.connectivityReceiverListener = null
        unregisterReceiver(connectivityReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        foregroundService?.removeLocationUpdates()
        super.onDestroy()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder =  service as LocationUpdatesService.LocalBinder
            foregroundService = binder.getServerInstance()
            mBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            foregroundService = null
            mBound = false
        }
    }

    inner class MyReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location: Location? = intent?.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {
                Timber.d("MyReceiver $location")
            }
        }
    }

    private fun isLocationPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            return if (permission1 != PackageManager.PERMISSION_GRANTED) {
                val permission1Rationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                if (permission1Rationale) {
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
                }
                false
            } else {
                true
            }
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startLocationUpdate()
                    } else {
                        val permission1Rationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        if (permission1Rationale) {
                            parent.snackbar("Location permission is needed for core functionality", actionName = "Ok") {
                                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
                            }
                        } else {
                            parent.snackbar("Permission was denied, but is needed for core functionality", actionName = "Settings") {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected) {
            snackBar = Snackbar.make(parent, "ইন্টারনেট কানেকশন সমস্যা হচ্ছে", Snackbar.LENGTH_INDEFINITE)
            snackBar?.show()
        } else {
            snackBar?.dismiss()
        }
    }

}
