package com.example.market.viewUtils
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.market.MyApplication
import com.example.market.binding.load
import com.example.market.getUrlForImage
import com.example.market.isImageKit
import com.example.market.utils.log
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ImageViewLoader @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, deffStyle: Int= 0) : androidx.appcompat.widget.AppCompatImageView(context,attributeSet,deffStyle) {
    var requestLayout = false
    var onMeasureCallback: OnMeasureCallback?=null
    var loadRunnable: Runnable?=null
    var requestBuilder: RequestBuilder<Drawable>?=null
    var url: String?=null
    var drawableResource:Int?=null
    var placeHolder: Int?=null
    var circleCrop: Boolean?=null
    var fade: Boolean ?=null
    var overrideWidth: Int ?=null
    var overrideHeight: Int ?= null
    var thumbnail: Boolean?=null
    var thumbnailBlobObejct: Blob?=null
    var aspectRatio:Float?=null
    set(value) {
        if (value!=null) {
            field = value
            mAspectRatio = value
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun loadRequest() {
        if (url!=null||drawableResource!=null||placeHolder!=null){
            var loadUrl = url

            if (loadUrl!=null&& isImageKit(loadUrl)) {
                loadUrl = getUrlForImage(loadUrl,measuredWidth,measuredHeight)
                log("ImageViewLoader $loadUrl")
            }

            requestBuilder = Glide.with(context).load(if (loadUrl!=null&&loadUrl.isNotEmpty()) loadUrl else if (drawableResource!=null&&drawableResource!=0) drawableResource else placeHolder)

            if (overrideHeight!=null&&overrideWidth!=null) {
                requestBuilder = requestBuilder!!.override(overrideWidth!!,overrideHeight!!)
            }
            if (placeHolder!=null) {
                requestBuilder = requestBuilder!!.placeholder(placeHolder!!)
            }
            if (circleCrop!=null) {
                requestBuilder = requestBuilder!!.circleCrop()
            }
            requestBuilder = requestBuilder!!.transition(DrawableTransitionOptions.withCrossFade())

            if (thumbnailBlobObejct!=null) {
                GlobalScope.launch {
                    val byteArray = thumbnailBlobObejct!!.toBytes()
                    val drawable = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size).toDrawable(resources)
                    requestBuilder = requestBuilder!!.placeholder(drawable)
                    post {
                        requestBuilder?.into(this@ImageViewLoader)
                    }
                }
            }else {
                requestBuilder?.into(this)
            }
        }
    }

    /**
     * Don't request layout if aspect ratio not changed
      */
    override fun requestLayout() {
        if (requestLayout){
            super.requestLayout()
            requestLayout = false
        }
    }

    var mAspectRatio = -1f
        set(value) {
            /**
             * If values same not request layout
             */
            if (value == field){
                return
            }
            /**
             * Set new value
             */
            field = value

            /**
             * Aspect ratio is changed request layout and invalidate
             */
            requestLayout = true

            requestLayout()

            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mAspectRatio!=-1f) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                    widthMeasureSpec,
                    MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                    (widthMeasureSpec * mAspectRatio).toInt(),
                    MeasureSpec.EXACTLY
                )
            )
            /**
             * Configure aspect ratio of view
             */
            setMeasuredDimension(
                measuredWidth,
                (measuredWidth * mAspectRatio).toInt()
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        loadRequest()
        onMeasureCallback?.onMeasure(this,measuredWidth,measuredHeight)
    }
}
interface OnMeasureCallback {
    fun onMeasure(view: View,measuredWidth: Int,measuredHeight: Int)
}