package com.example.market

import android.graphics.Bitmap
import com.example.market.utils.log
import com.imagekit.ImageKit
import com.imagekit.ImageKitCallback
import com.imagekit.android.entity.TransformationPosition

fun uploadImageFile(bitmap: Bitmap,fileName: String,callback: ImageKitCallback) {
    ImageKit.getInstance().uploader().upload(
        file = bitmap
        , fileName = imageKitIndex+fileName
        , useUniqueFilename = true
        , folder = "/Images"
        , imageKitCallback = callback
    )
}

const val imageKitIndex = "imageKit"

fun isImageKit(fileName: String): Boolean{
    try {
        val index = fileName.substring(0, imageKitIndex.length)
        if (imageKitIndex==index) {
            return true
        }
    } catch (e: Exception) {

    }
    return false
}

fun getUrlForImage(path:String,width: Int,height: Int,blur: Int?=null): String {
    var kit = ImageKit.getInstance()
        .url(
            path = "/Images/$path",
            transformationPosition = TransformationPosition.QUERY
        )
        .height(height)
        .width(width)

    if (blur!=null) {
        kit.quality(50)
        kit.progressive(true)
        kit = kit.blur(40)
    }
    return kit.create().also {
        log("Image kit Url: $it")
    }
}

fun initImageKit() {
    ImageKit.init(
        context = MyApplication.appContext,
        publicKey = "public_1ATYFWvJtgtudSz9o6oJXifiXX8=",
        urlEndpoint = "https://ik.imagekit.io/startup",
        transformationPosition = TransformationPosition.PATH,
        authenticationEndpoint = "https://www.pythonanywhere.com/user/oybek1234/files/home/oybek1234/mysite/auth"
    )
}