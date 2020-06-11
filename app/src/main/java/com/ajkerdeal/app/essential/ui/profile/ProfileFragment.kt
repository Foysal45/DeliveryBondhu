package com.ajkerdeal.app.essential.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ajkerdeal.app.essential.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.theartofdev.edmodo.cropper.CropImage
import timber.log.Timber

class ProfileFragment: Fragment() {

    private var binding: FragmentProfileBinding? = null
    private var contentType: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProfileBinding.inflate(inflater).also {
            binding = it
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode,resultCode, data)) {
            val image: Image? = ImagePicker.getFirstImageOrNull(data)
            image?.path?.let {
                Timber.d("path: $it")
                MediaScannerConnection.scanFile(requireContext(), arrayOf(it),null) { path, uri ->
                    Timber.d("uri: $uri")

                    val builder = CropImage.activity(uri)
                    if (contentType == 0) {
                        builder.setAspectRatio(1,1)
                    } else {
                        builder.setAspectRatio(800,500)
                    }
                    builder.start(requireContext(), this)
                }
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                when(contentType) {
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