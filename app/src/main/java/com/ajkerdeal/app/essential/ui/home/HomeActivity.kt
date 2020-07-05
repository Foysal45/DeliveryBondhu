package com.ajkerdeal.app.essential.ui.home

import android.Manifest
import android.app.Activity
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
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.ajkerdeal.app.essential.BuildConfig
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.broadcast.ConnectivityReceiver
import com.ajkerdeal.app.essential.fcm.FCMData
import com.ajkerdeal.app.essential.services.LocationUpdatesService
import com.ajkerdeal.app.essential.ui.auth.LoginActivity
import com.ajkerdeal.app.essential.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.inject
import timber.log.Timber

class HomeActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private var doubleBackToExitPressedOnce = false

    private val viewModel: HomeActivityViewModel by inject()

    private val PERMISSION_REQUEST_CODE = 8620
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var parent: FrameLayout

    private lateinit var receiver: MyReceiver
    private var foregroundService: LocationUpdatesService? = null
    private var mBound: Boolean = false
    private var toggle: Boolean = false
    private var snackBar: Snackbar? = null
    private lateinit var connectivityReceiver : ConnectivityReceiver

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var currentLocation: Location? = null
    private lateinit var gpsUtils: GpsUtils
    private var isGPS: Boolean = false

    private var navigationMenuId: Int = 0
    private var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        parent = findViewById(R.id.parent)
        navController = findNavController(R.id.nav_host_fragment)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_dashboard), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { item ->
            navigationMenuId = item.itemId
            menuItem = item
            drawerLayout.closeDrawer(GravityCompat.START)
            /*val handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (handled) {
                val parent = navView.parent
                if (parent is DrawerLayout) {
                    parent.closeDrawer(navView)
                }
            }*/

            return@setNavigationItemSelectedListener true
        }
        drawerListener()
        onNewIntent(intent)

        FirebaseMessaging.getInstance().subscribeToTopic("BondhuTopic").addOnSuccessListener {
            Timber.d("Firebase subscribeToTopic: BondhuTopic")
        }
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

        navHeaderData()

        gpsUtils = GpsUtils(this)
        turnOnGPS()
    }

    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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

    private fun drawerListener() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                when(navigationMenuId) {
                    R.id.nav_dashboard -> {
                        NavigationUI.onNavDestinationSelected(menuItem!!, navController)
                    }
                    R.id.nav_profile -> {
                        navController.navigate(R.id.nav_profile)
                        menuItem?.isChecked = true
                    }
                    R.id.nav_policy -> {

                        val bundle = bundleOf(
                            "url" to AppConstant.POLICY,
                            "title" to "প্রাইভেসি পলিসি"
                        )
                        navController.navigate(R.id.nav_webView, bundle)
                        menuItem?.isChecked = true
                    }
                    R.id.nav_logout -> {
                        logout()
                    }
                }
                navigationMenuId = 0
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            val model = intent.getParcelableExtra<FCMData>("data")
            Timber.d("onNewIntent $model")
            if (model != null) {
                if (model.serviceType?.isNotEmpty() == true) {
                    val bundle = bundleOf("serviceType" to model.serviceType)
                    navController.navigate(R.id.nav_action_dashboard_orderList, bundle)
                }
            }
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
        //return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

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

    fun startLocationUpdate(intervalInMinute: Int = 20, locationDistanceInMeter: Int = 20) {
        if (isLocationPermission()) {
            foregroundService?.setLocationInterval(intervalInMinute)
            foregroundService?.setLocationDifference(locationDistanceInMeter)
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
                Timber.tag("LocationLog").d("current location broadcast ${location.latitude},${location.longitude}")
                currentLocation = location
            }
        }
    }

    fun isLocationPermission(): Boolean {

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
                            }.show()
                        } else {
                            parent.snackbar("Permission was denied, but is needed for core functionality", actionName = "Settings") {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(intent)
                            }.show()
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


    fun navHeaderData() {
        val navHeaderView = navView.getHeaderView(0)
        val parentHeader: ConstraintLayout = navHeaderView.findViewById(R.id.parent)
        val userPic: ImageView = navHeaderView.findViewById(R.id.userPic)
        val userName: TextView = navHeaderView.findViewById(R.id.name)
        val userPhone: TextView = navHeaderView.findViewById(R.id.phone)

        userName.text = SessionManager.userName
        userPhone.text = SessionManager.mobile
        Glide.with(this)
            .load(SessionManager.userPic)
            .apply(RequestOptions().placeholder(R.drawable.ic_person_circle).error(R.drawable.ic_person_circle).circleCrop())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(userPic)
    }

    fun availabilityState(status: Boolean) {

        val switch: SwitchMaterial? = navView.menu.findItem(R.id.action_switch).actionView.findViewById(R.id.switchMaterial) as SwitchMaterial
        switch?.setOnCheckedChangeListener(null)
        switch?.isChecked = status
        switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.updateUserStatus("false", 1)
            } else {
                viewModel.updateUserStatus("true", 1)
            }
        }
    }

    fun updateMerchantLocation(model: MerchantLocationRequest) {
        //toast("Under Development lat: ${currentLocation?.latitude} lnt: ${currentLocation?.longitude}", Toast.LENGTH_SHORT)

        foregroundService?.recreateLocationRequest()
        Handler().postDelayed({
            Timber.tag("LocationLog").d("location refreshed")
            if (currentLocation != null) {
                model.latitude = currentLocation?.latitude.toString()
                model.longitude = currentLocation?.longitude.toString()
                viewModel.updateMerchantLocation(model).observe(this, Observer {
                    if (it) {
                        this.toast("সফলভাবে আপডেট হয়েছে")
                    } else {
                        this.toast("কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন")
                    }
                })
            } else {
                this.toast("লোকেশন এখনো পাওয়া যায়নি, একটু পর আবার চেষ্টা করুন")
            }
        }, 300L)

    }

    fun updateStatusLocation(model: StatusLocationRequest) {

        foregroundService?.recreateLocationRequest()
        Handler().postDelayed({
            Timber.tag("LocationLog").d("location refreshed")
            if (currentLocation != null) {
                model.latitude = currentLocation?.latitude.toString()
                model.longitude = currentLocation?.longitude.toString()
                viewModel.updateStatusLocation(model)/*.observe(this, Observer {
                    if (it) {
                        this.toast("সফলভাবে আপডেট হয়েছে")
                    } else {
                        this.toast("কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন")
                    }
                })*/
            } else {
                this.toast("লোকেশন এখনো পাওয়া যায়নি, একটু পর আবার চেষ্টা করুন")
            }
        },300L)

    }

    fun turnOnGPS() {
        gpsUtils.turnGPSOn {
            isGPS = it
            viewModel.isGPS.value = it
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstant.GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                isGPS = true
                viewModel.isGPS.value = true
            } else if (resultCode == Activity.RESULT_CANCELED) {
                isGPS = false
                viewModel.isGPS.value = false
            }
        }
    }

    fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }
}
