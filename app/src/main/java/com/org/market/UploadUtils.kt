package com.org.market

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ActionBar.log
import com.imagekit.ImageKitCallback
import com.imagekit.android.entity.UploadError
import com.imagekit.android.entity.UploadResponse
import java.lang.Exception

var uploadQueue = DispatchQueue("Upload queue")

inline fun uploadImageFromPath(path: String, imageName: String?=null, crossinline result:(imageUri: String?) -> Unit): Runnable {
    val runnable = Runnable {

        var bitmap = getBitmapFromPath(path)

        if (bitmap != null) {
            var fileName = imageName

            if (fileName == null) {
                fileName = System.currentTimeMillis().toString()
            }

            uploadImageFile(bitmap, fileName, object : ImageKitCallback {
                override fun onSuccess(uploadResponse: UploadResponse?) {

                    bitmap?.recycle()
                    bitmap = null

                    if (uploadResponse != null) {
                        result(uploadResponse.name)
                    } else {
                        result(null)
                    }
                }

                override fun onError(uploadError: UploadError) {
                    result(null)
                    log("Upload error ${uploadError.message}")
                    toast("Error ${uploadError.message}")
                }
            })
        }
    }
    uploadQueue.postRunnable(runnable)
    return runnable
}

fun getBitmapFromPath(path: String): Bitmap? {
    try {
        return BitmapFactory.decodeFile(path)
    }catch (e: Exception){

    }
    return null
}