package com.ajkerdeal.app.essential.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ajkerdeal.app.essential.api.models.profile.AreaInfo
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.databinding.FragmentProfileBinding
import com.ajkerdeal.app.essential.ui.dialog.LocationSelectionDialog
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.hideKeyboard
import com.ajkerdeal.app.essential.utils.toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.theartofdev.edmodo.cropper.CropImage
import org.koin.android.ext.android.inject
import timber.log.Timber

class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private val viewModel: ProfileViewModel by inject()
    private var contentType: Int = 0
    private var districtId = 0
    private var thanaId = 0
    private var districtName = ""
    private var thanaName = ""
    private var areaId = 0
    private var postCode = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProfileBinding.inflate(inflater).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding!!.lifecycleOwner = viewLifecycleOwner
        viewModel.loadProfile().observe(viewLifecycleOwner, Observer { model ->

            binding!!.name.setText(model.name)
            binding!!.mobile.setText(model.mobile)
            binding!!.alterMobile.setText(model.alternativeMobile)

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
                .apply(RequestOptions().circleCrop())
                .into(binding!!.userPic)
            Glide.with(this)
                .load(model?.imageInfo?.nid)
                .into(binding!!.NIDPic)
            Glide.with(this)
                .load(model?.imageInfo?.drivingImage)
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

        binding!!.userPic.setOnClickListener {
            contentType = 0
            ImagePicker.cameraOnly().start(this)
        }
        binding!!.NIDPic.setOnClickListener {
            contentType = 1
            ImagePicker.cameraOnly().start(this)
        }
        binding!!.drivingPic.setOnClickListener {
            contentType = 2
            ImagePicker.cameraOnly().start(this)
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

        val model = ProfileData().apply {
            id = SessionManager.userId
            name = binding!!.name.text.toString().trim()
            mobile = binding!!.mobile.text.toString().trim()
            alternativeMobile = binding!!.alterMobile.text.toString().trim()
            areaInfo = listOf(AreaInfo(districtId, thanaId, postCode, areaId, districtName, thanaName))
        }
        viewModel.updateProfile(model)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image: Image? = ImagePicker.getFirstImageOrNull(data)
            image?.path?.let {
                Timber.d("path: $it")
                MediaScannerConnection.scanFile(requireContext(), arrayOf(it), null) { path, uri ->
                    Timber.d("uri: $uri")

                    val builder = CropImage.activity(uri)
                    if (contentType == 0) {
                        builder.setAspectRatio(1, 1)
                    } else {
                        builder.setAspectRatio(800, 500)
                    }
                    builder.start(requireContext(), this)
                }
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                when (contentType) {
                    0 -> {
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions().circleCrop())
                            .into(binding!!.userPic)
                    }
                    1 -> {
                        Glide.with(this)
                            .load(uri)
                            .into(binding!!.NIDPic)
                    }
                    2 -> {
                        Glide.with(this)
                            .load(uri)
                            .into(binding!!.drivingPic)
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val msg = result.error
                Timber.d(msg)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }
}