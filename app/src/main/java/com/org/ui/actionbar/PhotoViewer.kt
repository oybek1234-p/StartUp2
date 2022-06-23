package com.org.ui.actionbar

import android.content.Context
import android.widget.ImageView
import com.org.market.getUrlForImage
import com.org.market.isImageKit
import com.org.ui.components.ImageViewLoader
import com.org.ui.components.load
import com.stfalcon.imageviewer.StfalconImageViewer

fun <T> openPhotoViewer(context: Context, view: ImageView, photos: ArrayList<T>) {
    if (photos.isEmpty()) return
    StfalconImageViewer.Builder(context, photos
    ) { imageView, image ->
        var url = ""
        var drawableRes = 0

        if (image is String) {
            url = if (isImageKit(image)) {
                getUrlForImage(image, imageView.width, imageView.height)
            } else {
                image
            }
        }
        if (image is Int) {
            drawableRes = image
        }
        val placeHolderUrl = if (view is ImageViewLoader) { view.photo.photoUrl } else ""
        imageView.load(url = url, drawableResource = drawableRes, placeHolderUrl = placeHolderUrl)
    }.withTransitionFrom(view).show(true)
}

fun openPhotoViewer(context: Context, view: ImageView, photoUrl: String) {
    if (photoUrl.isNotEmpty()) return
    openPhotoViewer(context, view, arrayListOf(photoUrl))
}

fun openPhotoViewer(context: Context, view: ImageView, drawableRes: Int) {
    if (drawableRes<=0) return
    openPhotoViewer(context,view,drawableRes)
}
