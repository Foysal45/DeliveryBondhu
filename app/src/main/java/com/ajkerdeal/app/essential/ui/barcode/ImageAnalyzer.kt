package com.ajkerdeal.app.essential.ui.barcode

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.fragment.app.FragmentManager
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class ImageAnalyzer(var listener: ((data: String) -> Unit)) : ImageAnalysis.Analyzer {

    override fun analyze(images: ImageProxy) {
        scanBarCode(images)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun scanBarCode(images: ImageProxy) {
        images.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, images.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage).addOnCompleteListener {
                images.close()
                if (it.isSuccessful) {
                    readBarCode(it.result as List<Barcode>)
                } else {
                    it.exception?.printStackTrace()
                }
            }

        }
    }

    private fun readBarCode(barcode: List<Barcode>) {
        for (barcodes in barcode) {
            when (barcodes.valueType) {
                Barcode.TYPE_TEXT -> {
                    listener.invoke(barcodes.rawValue ?: "")
                }
            }
        }

    }

}