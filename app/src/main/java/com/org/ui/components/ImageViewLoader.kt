package com.org.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.google.android.exoplayer2.util.Log
import com.google.android.material.imageview.ShapeableImageView
import com.org.market.*

open class ImageViewLoader @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    deffStyle: Int = 0,
) : ShapeableImageView(context, attributeSet, deffStyle) {

    companion object {
        const val DEFAULT_ANIM_DURATION = 300
        const val THUMB_SIZE = 0.07f
        const val DEFAULT_PLACEHOLDER_URL =
            "https://www.uidownload.com/files/791/397/85/purple-blurred-background.jpg"
    }

    var imageLoadCallback: ImageLoadCallback? = null

    interface ImageLoadCallback {
        fun onImageLoaded(resource: Drawable?)
        fun onResourceReady(resource: Drawable?)
        fun onImageCleared()
        fun onLoadFailed(exception: java.lang.Exception?)
    }

    data class Photo(
        var photoUrl: String = "",
        var drawableRes: Int = -1,
        var placeHolderUrl: String = "",
        var placeHolderRes: Int = -1,
        @FloatRange(from = 0.8, to = 2.0) var scaleRatio: Float = 1f,
        var circleCrop: Boolean = false,
        var fade: Boolean = true,
        var radius: Int? = null,
        var blur: Int? = null,
        var thumbnail: Boolean = false,
    )

    var animationDuration = DEFAULT_ANIM_DURATION

    var photo = Photo()

    private var requestBuilder: RequestBuilder<Drawable>? = null

    var glideManager = Glide.with(context)

    var isLoading = MutableLiveData<Boolean>()
    var fit = true

    private var isCleared = false
    private var inited = false

    fun init() {
        inited = true
    }

    fun clear() {
        glideManager.clear(this)
        isCleared = true
        dataSet = false
    }

    init {
        init()
    }

    private var dataSet = false
    private var photoChanged = false

    fun load(photo: Photo, fit: Boolean = true) {
        this.fit = fit
        if (this.photo != photo) {
            this.photo = photo

            dataSet = true
            if (!fit) {
                startLoad(photo)
            }
            photoChanged = true
            ignoreLayout = false
            super.requestLayout()
        }
    }

    override fun invalidate() {
        invalidate(left,top,right,bottom)
    }

    fun fullInvalidate() {
        super.invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clear()
    }

    //If photo cleared reload
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        requestBuilder?.into(this)
    }

    private fun startLoad(photo: Photo) {
        if (dataSet) {
            val rBuilder = RequestCreator.createRequestBuilderFor(glideManager, photo, this, null)
            requestBuilder = rBuilder.apply {
                into(this@ImageViewLoader)
            }
        }
    }

    private var ignoreLayout = false

    fun checkReLayout(): Boolean {
        if (measuredWidth <= 0 || measuredHeight <=0) {
            return true
        }
        return !ignoreLayout
    }

    override fun requestLayout() {
        if (checkReLayout()) {
            super.requestLayout()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        ignoreLayout = true
        super.setImageDrawable(drawable)
        ignoreLayout = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (fit) {
            if (photo.scaleRatio > 0) {
                setMeasuredDimension(measuredWidth, (measuredWidth * photo.scaleRatio).toInt())
            }
            if (photoChanged) {
                startLoad(photo)
                photoChanged = false
            }
        }
    }
}

object RequestCreator {
    fun createRequestBuilderFor(
        requestManager: RequestManager,
        photo: ImageViewLoader.Photo,
        view: View,
        listener: RequestListener<Drawable>? = null,
    ): RequestBuilder<Drawable> {
        return createRequestBuilderFor(requestManager.asDrawable(), photo, view, listener)
    }

    fun createRequestBuilderFor(
        rBuilder: RequestBuilder<Drawable>,
        photo: ImageViewLoader.Photo,
        view: View,
        listener: RequestListener<Drawable>? = null,
    ): RequestBuilder<Drawable> {
        var measuredWidth = view.measuredWidth
        var measuredHeight = view.measuredHeight

        val measured = measuredHeight > 0 && measuredWidth > 0

        var photoUrl = photo.photoUrl
        var isImageKit = false
        if (photoUrl.isNotEmpty()) {
            if (isImageKit(photoUrl).also { isImageKit = it }) {
                if (!measured) {
                    measuredHeight = 1
                    measuredWidth = 1
                }
                photoUrl =
                    getUrlForImage(photoUrl,
                        measuredWidth,
                        measuredHeight,
                        photo.circleCrop,
                        photo.radius,
                        photo.blur)
            }
        }

        var requestBuilder = rBuilder.load(
            when {
                photoUrl.isNotEmpty() -> photoUrl
                photo.drawableRes != -1 -> photo.drawableRes
                photo.placeHolderRes != 1 -> photo.placeHolderRes
                photo.placeHolderUrl.isNotEmpty() -> photo.placeHolderUrl
                else -> ImageViewLoader.DEFAULT_PLACEHOLDER_URL
            }

        )
        if (!isImageKit) {
            if (photo.circleCrop) {
                requestBuilder = requestBuilder.circleCrop()
            }
            if (measured) {
                requestBuilder = requestBuilder.override(measuredWidth, measuredHeight)
            }
            //Add blur and radius
        }

        if (photo.fade) {
            requestBuilder =
                requestBuilder.transition(DrawableTransitionOptions.withCrossFade(
                    ImageViewLoader.DEFAULT_ANIM_DURATION))
        }
        if (photo.thumbnail) {
            requestBuilder = requestBuilder.thumbnail(ImageViewLoader.THUMB_SIZE)
        }
        if (photo.placeHolderRes != -1) {
            requestBuilder = requestBuilder.placeholder(photo.placeHolderRes)
        }
        if (photo.placeHolderUrl.isNotEmpty()) {
            requestBuilder = requestBuilder.thumbnail(Glide.with(view as ImageView).load(photo.placeHolderUrl))
        }
        requestBuilder = requestBuilder.listener(listener)
        return requestBuilder
    }
}

@BindingAdapter(
    "url",
    "drawableResource",
    "placeholder",
    "circleCrop",
    "fade",
    "thumbnail",
    "aspectRatio",
    "placeHolderUrl",
    requireAll = false)

fun ImageView.load(
    url: String?=null,
    drawableResource: Int?=null,
    @LayoutRes placeHolder: Int?=null,
    circleCrop: Boolean = false,
    fade: Boolean = true,
    thumbnail: Boolean = true,
    @FloatRange(from = 0.8, to = 2.0) aspectRatio: Float = 1f,
    placeHolderUrl: String?=null
) {
    if (this is ImageViewLoader) {
        val loadUrl = url ?: ""
        val drawableRes = drawableResource ?: -1
        val placeHolderRes = placeHolder ?: -1
        val pHolderUrl = placeHolderUrl ?: ""

        val photo = ImageViewLoader.Photo(
            photoUrl = loadUrl,
            drawableRes = drawableRes,
            placeHolderRes = placeHolderRes,
            circleCrop = circleCrop,
            fade = fade,
            thumbnail = thumbnail,
            scaleRatio = aspectRatio,
            placeHolderUrl = pHolderUrl
        )
        load(photo, true)
    } else {
        Glide.with(ApplicationLoader.applicationContext).load(url).into(this)
    }
}