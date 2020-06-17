package com.ajkerdeal.app.essential.utils

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import timber.log.Timber
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(private val file: File,
                          private val mediaType : MediaType?,
                          private val callback: UploadCallback): RequestBody() {

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
        private val mediaTypeMultipart = "multipart/form-data".toMediaTypeOrNull()
        private val mediaTypeText = "text/plain".toMediaTypeOrNull()
    }


    //override fun contentType(): MediaType? = "$contentType/*".toMediaType()
    override fun contentType(): MediaType? = mediaType

    override fun writeTo(sink: BufferedSink) {

        Timber.d("writeTo called")
        val length = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L
        fileInputStream.use { inputStream ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                handler.post(ProgressUpdater(uploaded, length))
            }
        }
    }

    inner class ProgressUpdater(private val uploaded: Long, private val total: Long) : Runnable {
        override fun run() {
            Timber.d("onProgressUpdate $uploaded/$total")
            callback.onProgressUpdate((100 * uploaded / total).toInt())
        }
    }


    interface UploadCallback {
        fun onProgressUpdate(percentage: Int)
    }
}