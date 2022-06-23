package com.example.market
//
//import android.graphics.Bitmap
//import android.graphics.Color
//import com.example.market.utils.log
//import com.example.market.viewUtils.toast
//import com.imagekit.ImageKit
//import com.imagekit.ImageKitCallback
//import com.imagekit.android.entity.TransformationPosition
//import com.org.market.ApplicationLoader
//
//const val imageKitIndex = "imageKit"
//
//fun uploadImageFile(bitmap: Bitmap, fileName: String, callback: ImageKitCallback) {
//    ImageKit.getInstance().uploader().upload(
//        file = bitmap,
//        fileName = imageKitIndex + fileName,
//        useUniqueFilename = true,
//        folder = "/Images",
//        imageKitCallback = callback
//    )
//}
//
//fun isImageKit(fileName: String): Boolean {
//    try {
//        return fileName.startsWith(imageKitIndex)
//    } catch (e: Exception) {
//        com.org.market.log(e)
//    }
//    return false
//}
//
//fun getUrlForImage(
//    path: String,
//    width: Int,
//    height: Int,
//    circleCrop: Boolean = false,
//    radius: Int? = null,
//    blur: Int? = null,
//): String {
//    var kit = ImageKit.getInstance()
//        .url(
//            path = "/Images/$path",
//            transformationPosition = TransformationPosition.QUERY
//        )
//        .height(height)
//        .width(width)
//
//    if (blur != null) {
//        kit = kit.blur(40)
//    }
//    if (circleCrop) {
//        kit = kit.radius(80)
//    }
//    if (radius != null) {
//        kit = kit.radius(radius = radius)
//    }
//    return kit.create().also {
//        log("Image kit Url: $it")
//    }
//}
//
//fun initImageKit() {
//    ImageKit.init(
//        context = ApplicationLoader.applicationContext,
//        publicKey = "public_1ATYFWvJtgtudSz9o6oJXifiXX8=",
//        urlEndpoint = "https://ik.imagekit.io/startup",
//        transformationPosition = TransformationPosition.PATH,
//        authenticationEndpoint = "https://www.pythonanywhere.com/user/oybek1234/files/home/oybek1234/mysite/auth"
//    )
//}