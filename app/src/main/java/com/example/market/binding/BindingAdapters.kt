package com.example.market.binding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.market.*
import com.example.market.comment.Comment
import com.example.market.model.Product
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.ImageViewLoader
import com.example.market.viewUtils.OnMeasureCallback
import com.example.market.viewUtils.toast


/**
 * Thumbnail crossfade duration
 * */
private const val DRAWABLE_CROSSFADE_DURATION = 200

/**
 * Drawable transition options with crossfade
 * */
private val DRAWABLE_TRANSITION_CROSSFADE = DrawableTransitionOptions.withCrossFade(
    DRAWABLE_CROSSFADE_DURATION)

@BindingAdapter("commentUserName")
fun commentUserName(textView: TextView,comment: Comment) {
    comment.apply {
        textView.text = if (parentCommentId!=null&&parentCommentId!=repliedToId) {
            "$userName > $repliedTo"
        } else userName
    }
}
/**
 * Drawable thumbnail size
 * */
private const val THUMBNAIL_SIZE = 0.007f

@BindingAdapter("url","drawableResource","placeholder","circleCrop","fade","overrideWidth","overrideHeight","thumbnail","aspectRatio","centerCrop",requireAll = false)
fun ImageView.load(
    url: String?=null,
    drawableResource:Int?=null,
    placeHolder: Int?=null,
    circleCrop: Boolean?=null,
    fade: Boolean ?=null,
    overrideWidth: Int ?=null,
    overrideHeight: Int ?= null,
    thumbnail: Boolean?=null,
    aspectRatio:Float?=null,
    centerCrop: Boolean = false
) {
    if (this is ImageViewLoader) {
        this.url = url
        this.drawableResource = drawableResource
        this.placeHolder = placeHolder
        this.circleCrop = circleCrop
        this.fade = fade
        this.overrideHeight = overrideHeight
        this.overrideWidth = overrideWidth
        this.thumbnail = thumbnail
        this.aspectRatio = aspectRatio
        requestLayout = true
        requestLayout()
    } else {
        Glide.with(MyApplication.appContext).load(url).into(this)
    }
}

/**
 * Sets text into textview check wether texts are same
 */
@BindingAdapter("safeText")
fun TextView.safeText(newText: String){
    if (checkIsNotEmpty(text,newText)) return

    text = newText
}
/**
 * Sets top product
 */
@BindingAdapter("top")
fun TextView.top(top: String) {
    if (checkIsNotEmpty(text,top)) {
        visibleOrGone(false)
        return
    }

    visibleOrGone(true)
    text = top
}

@BindingAdapter("cost")
fun TextView.cost(cost: String) {
    text =  formatCurrency(cost.toLong())
}

@BindingAdapter("shipping")
fun TextView.shipping(shipping: String) {
    text = try {
        "Yetkazib berish: " + formatCurrency(shipping.toLong())
    }catch (e:Exception) {
        shipping
    }
}

val defaultShipping = ShippingLocation().apply {
    shippingCost = "Now shipping is more cheaper!"
    adress = "We offer fast shipping it takes only for 1 hour to shipp your item \nSet your location to know cost of shipping"
}

@BindingAdapter("shippingLocationCost")
fun TextView.shippingLocationCost(shippingLocationCost: Any?=null) {
    val shipping = currentUser?.shippingLocation

    if (shipping!=null) {
        shipping(shipping.shippingCost)
    } else {
        text =  defaultShipping.shippingCost
    }
}

@BindingAdapter("shippingLocationAdress")
fun TextView.shippingLocationAdress(shippingLocationAdress: Any?=null){
    val shipping = currentUser?.shippingLocation

    text = if (shipping!=null) {
        shipping.adress
    } else {
        defaultShipping.adress
    }
}

@BindingAdapter("discount")
fun TextView.discount(product:Product) {
    text = formatCurrency(((product.narxi.toLong() * product.discount.toLong())/100)+product.narxi.toLong())
}

@BindingAdapter("percent","percentChange")
fun TextView.percent(percent: String,percentChange: Boolean){
    val p = if (percentChange) "-" else "+"
    text = "$p$percent%"
}

@BindingAdapter("visibleOrGone")
fun View.visibleOrGone(visible: Boolean){
    visibility = if (visible) View.VISIBLE else View.GONE
}
fun hashTag(text: String) = "#$text"




