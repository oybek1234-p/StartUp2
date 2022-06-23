package com.example.market.utils
//
//import android.graphics.Bitmap
//import android.graphics.Matrix
//import android.graphics.drawable.Drawable
//import androidx.annotation.DrawableRes
//import androidx.core.content.ContextCompat
//import com.example.market.MyApplication
//import com.example.market.R
//import java.util.*
//
//val productPlaceholderWhite: Drawable? = getDrawable(R.drawable.youtube_background_item)
//val circleDrawable: Drawable? = getDrawable(R.drawable.circle)
//val heartDrawableConfeti = getDrawable(R.drawable.heart_confetti)
//
///**
// * Gets drawable from resource
// */
//fun getDrawable(@DrawableRes resourceId: Int?): Drawable? {
//    if(resourceId==null) {
//        log("Get drawable resId $resourceId is null")
//        return null
//    }
//    return ContextCompat.getDrawable(MyApplication.appContext, resourceId)
//}
//
//fun rotateBitmap(rotateValue: Float,bitmap: Bitmap): Bitmap {
//    val matrix = Matrix().apply { postRotate(rotateValue) }
//    val rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,false)
//    if (bitmap!=rotatedBitmap&&!bitmap.isRecycled) {
//        bitmap.recycle()
//    }
//    return rotatedBitmap
//}
//
//fun cropCenterBitmap(bitmap: Bitmap) : Bitmap {
//    var width = bitmap.width
//    val height = bitmap.height
//    if (width>height) {
//        width = height
//    }
//    return Bitmap.createBitmap(bitmap,(bitmap.width - width)/2,(bitmap.height - width)/2,width,width)
//}
