package com.ajkerdeal.app.essential.ui.bar_code_scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.utils.CustomSpinnerAdapter
import com.ajkerdeal.app.essential.utils.toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.button.MaterialButton
import com.google.zxing.integration.android.IntentIntegrator
import com.theartofdev.edmodo.cropper.CropImage
import timber.log.Timber
import java.io.File

class BarCodeScannerActivity : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var scannerResultTV: TextView
    private lateinit var spinnerSizeType: Spinner
    private lateinit var imageView: ImageView
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_scanner)

        initView()
        initClickLister()
        setUpSpinner()
        startScanner()

    }

    private fun initView(){
        title = findViewById(R.id.invoiceTitle)
        scannerResultTV = findViewById(R.id.scannerResultTV)
        spinnerSizeType = findViewById(R.id.spinnerSizeType)
        imageView = findViewById(R.id.invoicePic)
        saveButton = findViewById(R.id.invoiceInfoSaveBtn)
    }


    private fun  initClickLister(){
        scannerResultTV.setOnClickListener {
            startScanner()
        }
        imageView.setOnClickListener {
            ImagePicker.cameraOnly().start(this)
        }
    }

    private fun startScanner(){
        val intentIntegrator = IntentIntegrator(this@BarCodeScannerActivity)
        intentIntegrator.setBeepEnabled(true)
        intentIntegrator.setOrientationLocked(true)
        intentIntegrator.setCameraId(0)
        intentIntegrator.setPrompt("SCAN")
        intentIntegrator.setBarcodeImageEnabled(false)
        intentIntegrator.initiateScan()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val scannerResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val imageCaptureResult = ImagePicker.shouldHandle(requestCode, resultCode, data)
        if (scannerResult != null) {
            if (scannerResult.contents == null) {
                this.toast("cancelled")
            } else {
                scannerResultTV.text = "Scanned Result: ${scannerResult.contents}"//String.format("Scanned Result: %s", scannerResult)
                this.toast("Scanned : ${scannerResult.contents}")
                ImagePicker.cameraOnly().start(this)

            }
        } else if (imageCaptureResult){
            val image: Image? = ImagePicker.getFirstImageOrNull(data)
            image?.path?.let {
                Timber.d("ImageLog ImagePickerPath: $it")

                val uri = FileProvider.getUriForFile(this, "com.ajkerdeal.app.essential.fileprovider", File(it))
                Timber.d("ImageLog ImagePickerUri: $requestCode, $uri")

                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView)

                val builder = CropImage.activity()
                builder.start( this)
            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                Timber.d("ImageLog cropPhotoURI $uri")
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_banner_place).error(R.drawable.ic_banner_place))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val msg = result.error
                Timber.d("Error $msg")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setUpSpinner() {

        val sizeList: MutableList<String> = mutableListOf()
        sizeList.add("Select Size")
        sizeList.add("Small")
        sizeList.add("Medium")
        sizeList.add("Large")

        val spinnerAdapter = CustomSpinnerAdapter(this, R.layout.item_view_spinner_item, sizeList)
        spinnerSizeType.adapter = spinnerAdapter
        spinnerSizeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }
        }

    }


}