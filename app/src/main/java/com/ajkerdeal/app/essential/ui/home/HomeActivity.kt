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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import androidx.work.*
import com.ajkerdeal.app.essential.BuildConfig
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.chat.ChatUserData
import com.ajkerdeal.app.essential.api.models.chat.FirebaseCredential
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestAD
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.broadcast.ConnectivityReceiver
import com.ajkerdeal.app.essential.fcm.FCMData
import com.ajkerdeal.app.essential.services.DistrictCacheWorker
import com.ajkerdeal.app.essential.services.LocationUpdateWorker
import com.ajkerdeal.app.essential.services.LocationUpdatesService
import com.ajkerdeal.app.essential.ui.auth.LoginActivity
import com.ajkerdeal.app.essential.ui.chat.ChatConfigure
import com.ajkerdeal.app.essential.ui.location.LocationUsesBottomSheet
import com.ajkerdeal.app.essential.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

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
    private lateinit var connectivityReceiver: ConnectivityReceiver

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var currentLocation: Location? = null
    private lateinit var gpsUtils: GpsUtils
    private var isGPS: Boolean = false

    private var navigationMenuId: Int = 0
    private var menuItem: MenuItem? = null

    private lateinit var appUpdateManager: AppUpdateManager
    private val requestCodeAppUpdate = 21720

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

        FirebaseMessaging.getInstance().subscribeToTopic("BondhuTopic")
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().subscribeToTopic("BondhuTopicTest")
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
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
        appUpdateManager()
        periodicLocationUpdate()
        syncDistrict()
        showLocationConsent()
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
            Toast.makeText(this, "????????????????????? ???????????? ???????????? ???????????? ??????????????? ????????????", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000L)
        }
    }

    private fun drawerListener() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                when (navigationMenuId) {
                    R.id.nav_dashboard -> {
                        NavigationUI.onNavDestinationSelected(menuItem!!, navController)
                    }
                    R.id.nav_profile -> {
                        navController.navigate(R.id.nav_profile)
                        menuItem?.isChecked = true
                    }
                    R.id.nav_chat -> {
                        goToChatActivity()
                    }
                    R.id.nav_quick_order_lists -> {
                        navController.navigate(R.id.nav_quick_order_lists)
                    }
                    R.id.nav_setting -> {
                        navController.navigate(R.id.nav_setting)
                    }
                    R.id.nav_policy -> {

                        val bundle = bundleOf(
                            "url" to AppConstant.POLICY,
                            "title" to "??????????????????????????? ???????????????"
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
            Timber.d("BundleLog ${intent.extras?.bundleToString()}")
            val model: FCMData? = intent.getParcelableExtra("data")
            Timber.d("onNewIntent $model")
            if (model != null) {
                if (model.serviceType?.isNotEmpty() == true) {
                    val bundle = bundleOf("serviceType" to model.serviceType)
                    navController.navigate(R.id.nav_orderList, bundle)
                }
                intent.removeExtra("data")
            } else {
                val bundleExt = intent.extras
                if (bundleExt != null) {
                    val notificationType = bundleExt.getString("notificationType")
                    if (!notificationType.isNullOrEmpty()) {
                        val fcmModel: FCMData = FCMData(
                            0,
                            bundleExt.getString("notificationType"),
                            bundleExt.getString("title"),
                            bundleExt.getString("body"),
                            bundleExt.getString("imageUrl"),
                            bundleExt.getString("bigText"),
                            "",
                            bundleExt.getString("serviceType")
                        )
                        Timber.d("BundleLog FCMData $fcmModel")
                        //goToNotificationPreview(fcmModel)
                        val bundle = bundleOf("fcmData" to fcmModel)
                        navController.navigate(R.id.nav_notification_preview, bundle)
                    }
                }
                intent.removeExtra("notificationType")
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

        cancelPeriodicLocationUpdate()
        cancelSyncDistrict()
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
        checkStalledUpdate()
        checkLocationEnable()
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
            val binder = service as LocationUpdatesService.LocalBinder
            foregroundService = binder.getServerInstance()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            foregroundService = null
            mBound = false
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
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
                        turnOnGPS()
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

    private fun isCheckPermission(): Boolean {
        val permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionArray = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return when {
            permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED -> {
                true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                false
            }
            else -> {
                requestMultiplePermissions.launch(permissionArray)
                false
            }
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var isCameraGranted: Boolean = false
        var isStorageGranted: Boolean = false
        permissions.entries.forEach { permission ->
            if (permission.key == Manifest.permission.CAMERA) {
                isCameraGranted = permission.value
            }
            if (permission.key == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                isStorageGranted = permission.value
            }
        }
        if (isCameraGranted /*&& isStorageGranted*/) {

        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected) {
            snackBar = Snackbar.make(parent, "??????????????????????????? ????????????????????? ?????????????????? ???????????????", Snackbar.LENGTH_INDEFINITE)
            snackBar?.show()
        } else {
            snackBar?.dismiss()
        }
    }


    private fun navHeaderData() {
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

        userPic.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            navController.navigate(R.id.nav_profile)
        }
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

    /* fun updateMerchantLocation(model: MerchantLocationRequest) {

         foregroundService?.recreateLocationRequest()
         Handler().postDelayed({
             Timber.tag("LocationLog").d("location refreshed")
             if (currentLocation != null) {
                 model.latitude = currentLocation?.latitude.toString()
                 model.longitude = currentLocation?.longitude.toString()
                 viewModel.updateMerchantLocation(model).observe(this, Observer {
                     if (it) {
                         this.toast("????????????????????? ??????????????? ??????????????????")
                     } else {
                         this.toast("??????????????? ???????????? ?????????????????? ???????????????, ???????????? ?????????????????? ????????????")
                     }
                 })
             } else {
                 this.toast("?????????????????? ???????????? ??????????????? ???????????????, ???????????? ?????? ???????????? ?????????????????? ????????????")
             }
         }, 300L)

     }*/

    fun updateLocationAD(model: LocationUpdateRequestAD) {

        foregroundService?.recreateLocationRequest()
        Handler().postDelayed({
            Timber.tag("LocationLog").d("location refreshed")
            if (currentLocation != null) {
                model.latitude = currentLocation?.latitude.toString()
                model.Longitude = currentLocation?.longitude.toString()
                viewModel.updateLocationAD(model).observe(this, Observer {
                    if (it) {
                        this.toast("????????????????????? ??????????????? ??????????????????")
                    } else {
                        this.toast("??????????????? ???????????? ?????????????????? ???????????????, ???????????? ?????????????????? ????????????")
                    }
                })
            } else {
                this.toast("?????????????????? ???????????? ??????????????? ???????????????, ???????????? ?????? ???????????? ?????????????????? ????????????")
            }
        }, 300L)

    }

    fun updateLocationDT(model: LocationUpdateRequestDT) {

        foregroundService?.recreateLocationRequest()
        Handler().postDelayed({
            Timber.tag("LocationLog").d("location refreshed")
            if (currentLocation != null) {
                model.latitude = currentLocation?.latitude.toString()
                model.longitude = currentLocation?.longitude.toString()
                viewModel.updateLocationDT(model).observe(this, Observer {
                    if (it) {
                        this.toast("????????????????????? ??????????????? ??????????????????")
                    } else {
                        this.toast("??????????????? ???????????? ?????????????????? ???????????????, ???????????? ?????????????????? ????????????")
                    }
                })
            } else {
                this.toast("?????????????????? ???????????? ??????????????? ???????????????, ???????????? ?????? ???????????? ?????????????????? ????????????")
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
                        this.toast("????????????????????? ??????????????? ??????????????????")
                    } else {
                        this.toast("??????????????? ???????????? ?????????????????? ???????????????, ???????????? ?????????????????? ????????????")
                    }
                })*/
            } else {
                this.toast("?????????????????? ???????????? ??????????????? ???????????????, ???????????? ?????? ???????????? ?????????????????? ????????????")
            }
        }, 300L)

    }

    fun turnOnGPS() {
        gpsUtils.turnGPSOn {
            isGPS = it
            viewModel.isGPS.value = it
            isCheckPermission()
        }
    }

    private fun showLocationConsent() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission1 == PackageManager.PERMISSION_GRANTED) return
        if (SessionManager.isLocationConsentShown) return

        val tag = LocationUsesBottomSheet.tag
        val dialog = LocationUsesBottomSheet.newInstance()
        dialog.show(supportFragmentManager, tag)
        dialog.onItemSelected = { flag ->
            dialog.dismiss()
            if (flag) {
                SessionManager.isLocationConsentShown = true
                if (isLocationPermission()) {
                    turnOnGPS()
                }
            } else {
                this.toast("App need location permission to work properly", Toast.LENGTH_LONG)
            }
        }
    }

    private fun checkLocationEnable() {
        if (SessionManager.isLocationConsentShown) {
            if (isLocationPermission()) {
                turnOnGPS()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstant.GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                isGPS = true
                viewModel.isGPS.value = true
                isCheckPermission()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                isGPS = false
                viewModel.isGPS.value = false
            }
        }
    }

    fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun appUpdateManager() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, requestCodeAppUpdate)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkStalledUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // For IMMEDIATE
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, requestCodeAppUpdate)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @KoinApiExtension
    private fun periodicLocationUpdate() {

        if (SessionManager.workManagerUUID.isNotEmpty()) return

        val data = Data.Builder()
            .putString("action", "updateLocationAfter15Min")
            .build()

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /*val request = OneTimeWorkRequestBuilder<LocationUpdateWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .addTag("updateLocation").setInitialDelay(1, TimeUnit.MINUTES)
            .build()*/
        val request = PeriodicWorkRequestBuilder<LocationUpdateWorker>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS
        )
            .setConstraints(constraints)
            .setInputData(data)
            .addTag("updateLocation").setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        val requestUUID = request.id
        val workManager = WorkManager.getInstance(this)
        workManager.cancelAllWork()
        //workManager.beginUniqueWork("updateLocation", ExistingWorkPolicy.REPLACE, request).enqueue()
        workManager.enqueue(request)
        workManager.getWorkInfoByIdLiveData(requestUUID).observe(this, Observer { workInfo ->
            if (workInfo != null) {
                val result = workInfo.outputData.getString("work_result")
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Timber.d("WorkManager getWorkInfoByIdLiveDataObserve onSuccess resultMsg: $result")
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    Timber.d("WorkManager getWorkInfoByIdLiveDataObserve onFailed resultMsg: $result")
                }
            }
        })
        SessionManager.workManagerUUID = requestUUID.toString()
        Timber.d("LocationUpdateWorker enqueue with $requestUUID")
    }

    @KoinApiExtension
    private fun syncDistrict() {

        if (SessionManager.workManagerDistrictUUID.isNotEmpty()) return

        val data = Data.Builder()
            .putBoolean("sync", true)
            .build()

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /*val request = OneTimeWorkRequestBuilder<DistrictCacheWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .addTag("districtSync").setInitialDelay(5, TimeUnit.SECONDS)
            .build()*/
        val request = PeriodicWorkRequestBuilder<DistrictCacheWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInputData(data)
            .addTag("districtSync").setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        val requestUUID = request.id
        val workManager = WorkManager.getInstance(this)
        //workManager.beginUniqueWork("districtSync", ExistingWorkPolicy.REPLACE, request).enqueue()
        workManager.enqueue(request)
        workManager.getWorkInfoByIdLiveData(requestUUID).observe(this, Observer { workInfo ->
            if (workInfo != null) {
                val result = workInfo.outputData.getString("work_result")
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Timber.d("WorkManager getWorkInfoByIdLiveDataObserve onSuccess resultMsg: $result")
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    Timber.d("WorkManager getWorkInfoByIdLiveDataObserve onFailed resultMsg: $result")
                }
            }
        })
        SessionManager.workManagerDistrictUUID = requestUUID.toString()
    }

    private fun cancelPeriodicLocationUpdate() {
        if (SessionManager.workManagerUUID.isNotEmpty()) {
            val workManager = WorkManager.getInstance(this)
            workManager.cancelWorkById(UUID.fromString(SessionManager.workManagerUUID))
            SessionManager.workManagerUUID = ""
        }
    }

    private fun cancelSyncDistrict() {
        if (SessionManager.workManagerDistrictUUID.isNotEmpty()) {
            val workManager = WorkManager.getInstance(this)
            workManager.cancelWorkById(UUID.fromString(SessionManager.workManagerDistrictUUID))
            SessionManager.workManagerDistrictUUID = ""
        }
    }

    private fun goToChatActivity() {
        val firebaseCredential = FirebaseCredential(
            firebaseWebApiKey = BuildConfig.FirebaseWebApiKey
        )
        val senderData = ChatUserData(SessionManager.dtUserId.toString(), SessionManager.userName, SessionManager.mobile,
            imageUrl = SessionManager.userPic,
            role = "bondhu",
            fcmToken = SessionManager.firebaseToken
        )
        ChatConfigure(
            "dt-bondhu",
            senderData,
            firebaseCredential = firebaseCredential
        ).config(this)
    }

}
