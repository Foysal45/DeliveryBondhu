package com.ajkerdeal.app.essential.ui.profile

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.*
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.profile.AreaInfo
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.api.models.profile.profile_DT.ProfileDataDT
import com.ajkerdeal.app.essential.databinding.FragmentProfileBinding
import com.ajkerdeal.app.essential.services.ProfileUpdateWorker
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.ui.home.HomeActivity
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker

import com.google.gson.Gson
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private val viewModel: ProfileViewModel by inject()
    private val gson = Gson()

    private var contentType: Int = 0
    private var districtId = 0
    private var thanaId = 0
    private var districtName = ""
    private var thanaName = ""
    private var areaName = ""
    private var areaId = 0
    private var postCode = 0
    private var profileMobile: String? = ""
    private var nameOld: String? = ""
    private var bKashAccount: String? = ""

    private var profileUri: String? = ""
    private var nidUri: String? = ""
    private var drivingUri: String? = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //setHasOptionsMenu(true)
        return FragmentProfileBinding.inflate(inflater).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.lifecycleOwner = viewLifecycleOwner
        viewModel.loadProfile().observe(viewLifecycleOwner, Observer { model ->

            SessionManager.userName = model.name ?: "আপনার নাম"
            profileMobile = model.mobile
            nameOld = model.name
            bKashAccount = model.bKashAccountNumber

            binding!!.name.setText(nameOld)
            binding!!.mobile.setText(profileMobile)
            binding!!.alterMobile.setText(model.alternativeMobile)
            binding!!.bKashAccount.setText(bKashAccount)
            SessionManager.bkashMobileNumber = bKashAccount ?: ""

            val areaInfo = model.areaInfo!![0]
            districtId = areaInfo.districtId
            districtName = areaInfo.districtName ?: ""
            thanaId = areaInfo.thanaId
            thanaName = areaInfo.thanaName ?: ""
            areaId = areaInfo.areaId
            postCode = areaInfo.postCode

            binding!!.district.setText(districtName)
            binding!!.thana.setText(thanaName)

            Glide.with(this)
                .load(model?.imageInfo?.profileImage)
                .apply(RequestOptions().placeholder(R.drawable.ic_person_circle).error(R.drawable.ic_person_circle).circleCrop())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding!!.userPic)
            Glide.with(this)
                .load(model?.imageInfo?.nid)
                .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding!!.NIDPic)
            Glide.with(this)
                .load(model?.imageInfo?.drivingImage)
                .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding!!.drivingPic)
        })

        binding!!.district.setOnClickListener {

            binding!!.district.isEnabled = false
            viewModel.loadLocationList().observe(viewLifecycleOwner, Observer { response ->

                val districtModelList = response.districtInfo
                val districtNameList = districtModelList.map { it.districtBng }

                val dialog = LocationSelectionDialog.newInstance(districtNameList as MutableList<String>)
                dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                dialog.onLocationPicked = { position, value ->
                    districtName = value
                    binding?.district?.setText(value)
                    if (position in 0..districtModelList.size) {
                        districtId = districtModelList[position].districtId
                    }
                }
                binding!!.district.isEnabled = true
            })
        }

        binding!!.thana.setOnClickListener {

            if (districtId == 0) {
                context?.toast("বর্তমান কর্মস্থান নির্বাচন করুন")
            } else {
                binding!!.thana.isEnabled = false
                viewModel.loadLocationList(districtId).observe(viewLifecycleOwner, Observer { response ->

                    val thanaModelList = response.districtInfo[0].thanaHome
                    val thanaNameList = thanaModelList.map { it.thanaBng }

                    val dialog = LocationSelectionDialog.newInstance(thanaNameList as MutableList<String>)
                    dialog.show(childFragmentManager, LocationSelectionDialog.tag)
                    dialog.onLocationPicked = { position, value ->
                        //context?.toast(value)
                        thanaName = value
                        binding?.thana?.setText(value)
                        if (position in 0..thanaModelList.size) {
                            val model = thanaModelList[position]
                            thanaId = model.thanaId
                            postCode = model.postalCode.toIntOrNull() ?: 0
                            //areaId = 0
                        }
                    }
                    binding!!.thana.isEnabled = true
                })
            }
        }

        binding!!.userPic.setOnClickListener {
            contentType = 0
            ImagePicker.with(this)
                .cameraOnly()
                .cropSquare()
                .compress(512)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding!!.NIDPic.setOnClickListener {
            contentType = 1
            //ImagePicker.cameraOnly().start(this)
            ImagePicker.with(this)
                .cameraOnly()
                .crop(1.5f,1f)
                .compress(512)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding!!.drivingPic.setOnClickListener {
            contentType = 2
            //ImagePicker.cameraOnly().start(this)
            ImagePicker.with(this)
                .cameraOnly()
                .crop(1.5f,1f)
                .compress(512)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding!!.saveBtn.setOnClickListener {
            updateProfile()
        }

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ViewState.ShowMessage -> {
                    requireContext().toast(state.message)
                }
                is ViewState.KeyboardState -> {
                    hideKeyboard()
                }
                is ViewState.ProgressState -> {

                    if (state.isShow) {
                        binding?.progressBar?.visibility = View.VISIBLE
                    } else {
                        binding?.progressBar?.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun updateProfile() {


        if (binding!!.name.text.toString().trim().isEmpty()) {
            val message = "আপনার নাম লিখুন"
            context?.toast(message)
            return
        }

        val newBkashNumber = binding!!.bKashAccount.text.toString().trim()
        if (newBkashNumber.isEmpty()) {
            val message = "আপনার বিকাশ একাউন্ট নম্বর লিখুন"
            context?.toast(message)
            return
        }

        if (newBkashNumber.length != 11) {
            val message = "আপনার সঠিক বিকাশ একাউন্ট নম্বর লিখুন"
            context?.toast(message)
            Timber.d("check number ${newBkashNumber.length}")
            return
        }

        if (districtId == 0) {
            val message = "বর্তমান কর্মস্থান নির্বাচন করুন"
            context?.toast(message)
            return
        }

        if (thanaId == 0) {
            val message = "এরিয়া নির্বাচন করুন"
            context?.toast(message)
            return
        }

        context?.toast("প্রোফাইল আপডেট হচ্ছে, অপেক্ষা করুন")
        binding?.progressBar?.visibility = View.VISIBLE
        binding!!.saveBtn.isEnabled = false

        val model = ProfileData().apply {
            bondhuId = SessionManager.userId
            name = binding!!.name.text.toString().trim()
            mobile = profileMobile
            alternativeMobile = binding!!.alterMobile.text.toString().trim()
            bKashAccountNumber = binding!!.bKashAccount.text.toString().trim()
            areaInfo = listOf(AreaInfo(districtId, thanaId, postCode, areaId, districtName, thanaName))

            isProfileImage = !profileUri.isNullOrEmpty()
            isDrivingLicense = !drivingUri.isNullOrEmpty()
            isNID = !nidUri.isNullOrEmpty()
        }
        val modelDT = ProfileDataDT().apply {
            bondhuId = SessionManager.dtUserId
            name = binding!!.name.text.toString().trim()
            mobile = profileMobile
            alternativeMobile = binding!!.alterMobile.text.toString().trim()
            bkashMobileNumber = binding!!.bKashAccount.text.toString().trim()
            areaInfo = listOf(com.ajkerdeal.app.essential.api.models.profile.profile_DT.AreaInfo(districtId, districtName, thanaId, thanaName, postCode, areaId))
        }
        //viewModel.updateProfile(model)
        SessionManager.bkashMobileNumber = binding!!.bKashAccount.text.toString().trim()

        val data = Data.Builder()
            .putString("Data", gson.toJson(model))
            .putString("DataDT", gson.toJson(modelDT))
            .putString("profileUri", profileUri)
            .putString("nidUri", nidUri)
            .putString("drivingUri", drivingUri)
            .build()

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<ProfileUpdateWorker>().setConstraints(constraints).setInputData(data).build()
        WorkManager.getInstance(requireContext()).beginUniqueWork("sync", ExistingWorkPolicy.KEEP, request).enqueue()
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner, Observer { workInfo ->
            if (workInfo != null){
                if (workInfo.state == WorkInfo.State.SUCCEEDED){
                    //context?.toast("প্রোফাইল আপডেট হয়েছে")
                    binding?.progressBar?.visibility = View.GONE
                    binding?.saveBtn?.isEnabled = true
                    profileUri = ""
                    nidUri = ""
                    drivingUri = ""

                    (activity as HomeActivity).onBackPressed()

                } else if (workInfo.state == WorkInfo.State.FAILED){
                    //context?.toast("কোথাও কোনো সমস্যা হচ্ছে")
                    binding?.progressBar?.visibility = View.GONE
                    binding?.saveBtn?.isEnabled = true
                }
                val result = workInfo.outputData.getString("work_result")
                context?.toast(result)

            }
        })

    }

    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.data!!
                when (contentType) {
                    0 -> {
                        profileUri = uri.path
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().circleCrop())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding!!.userPic)


                        updateProfile()
                    }
                    1 -> {
                        nidUri = uri.path
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding!!.NIDPic)

                        updateProfile()
                    }
                    2 -> {
                        drivingUri = uri.path
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding!!.drivingPic)

                        updateProfile()
                    }
                }


            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                context?.toast(ImagePicker.getError(data))
            }
        }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image: Image? = ImagePicker.getFirstImageOrNull(data)
            image?.path?.let {
                Timber.d("ImageLog ImagePickerPath: $it")
                *//*MediaScannerConnection.scanFile(requireContext(), arrayOf(it), null) { path, uri ->
                    Timber.d("uri: $uri")
                }*//*
                val uri = FileProvider.getUriForFile(requireContext(), "com.ajkerdeal.app.essential.fileprovider", File(it))
                Timber.d("ImageLog ImagePickerUri: $uri")

                val builder = CropImage.activity(uri)
                if (contentType == 0) {
                    builder.setAspectRatio(1, 1)
                } else {
                    builder.setAspectRatio(800, 500)
                }
                builder.start(requireContext(), this)
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                Timber.d("ImageLog cropPhotoURI $uri")
                when (contentType) {
                    0 -> {
                        profileUri = uri.path
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().circleCrop())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding!!.userPic)


                        updateProfile()
                    }
                    1 -> {
                        nidUri = uri.path
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding!!.NIDPic)

                        updateProfile()
                    }
                    2 -> {
                        drivingUri = uri.path
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding!!.drivingPic)

                        updateProfile()
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val msg = result.error
                Timber.d(msg)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }*/

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {

                updateProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

}