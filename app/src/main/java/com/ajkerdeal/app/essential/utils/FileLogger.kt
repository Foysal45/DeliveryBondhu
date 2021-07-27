package com.ajkerdeal.app.essential.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.ajkerdeal.app.essential.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

fun logInFile(context: Context, log: String, fileName: String = "log.txt") {

    CoroutineScope(Dispatchers.IO).launch {

        val outputDirectory = context.getString(R.string.app_name_folder)
        val relativeLocation = Environment.DIRECTORY_DOWNLOADS + "/" + outputDirectory
        val storageDir: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), outputDirectory)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val filePath = "${storageDir.absolutePath}/$fileName"
        Timber.d("filePath $filePath")
        val file = File(filePath)
        if (file.exists()) {
            try {
                file.appendText("$log\n")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

        } else {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.DownloadColumns.DISPLAY_NAME, fileName)
                put(MediaStore.DownloadColumns.MIME_TYPE, "text/plain")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.DownloadColumns.RELATIVE_PATH, relativeLocation)
                    //put(MediaStore.DownloadColumns.IS_PENDING, 1)
                }
            }
            val contentUri = MediaStore.Files.getContentUri("external")
            try {
                val fileUri: Uri? = resolver.insert(contentUri, contentValues)
                fileUri?.let { uri ->
                    resolver.openFileDescriptor(uri, "w").use { pfd ->
                        FileOutputStream(pfd!!.fileDescriptor).use {
                            it.write("$log\n".toByteArray())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.DownloadColumns.IS_PENDING, 0)
                    resolver.update(contentUri, contentValues, null, null)
                }*/
            }
        }
    }
}