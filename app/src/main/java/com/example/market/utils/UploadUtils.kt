package com.example.market.utils
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import com.example.market.DetailsFragment
//import com.example.market.uploadImageFile
//import com.imagekit.ImageKitCallback
//import com.imagekit.android.entity.UploadError
//import com.imagekit.android.entity.UploadResponse
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.lang.Exception
//
//fun uploadImageFromPath(path: String,result:(imageUri: String?) -> Unit,imageName: String?=null): Runnable {
//    val runnable = Runnable {
//        var bitmap = getBitmapFromPath(path)
//
//        if (bitmap!=null) {
//            var fileName = imageName
//
//            if (fileName==null) {
//                fileName = System.currentTimeMillis().toString()
//            }
//
//            uploadImageFile(bitmap,fileName,object : ImageKitCallback{
//                override fun onSuccess(uploadResponse: UploadResponse?) {
//
//                    bitmap?.recycle()
//                    bitmap = null
//
//                    if (uploadResponse!=null) {
//                        result(uploadResponse.name)
//                    } else {
//                        result(null)
//                    }
//                }
//
//                override fun onError(uploadError: UploadError) {
//                    result(null)
//                    log("Upload error ${uploadError.message}")
//                }
//            })
//        }
//    }
//    DetailsFragment.threadQueue.postRunnable(runnable)
//    return runnable
//}
//
//fun getBitmapFromPath(path: String): Bitmap? {
//    try {
//        return BitmapFactory.decodeFile(path)
//    }catch (e: Exception){
//
//    }
//    return null
//}