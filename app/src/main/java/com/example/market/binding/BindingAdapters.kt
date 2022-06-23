package com.example.market.binding

//import android.content.Context
//import android.graphics.drawable.Drawable
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.annotation.LayoutRes
//import androidx.databinding.Bindable
//import androidx.databinding.BindingAdapter
//import androidx.databinding.BindingMethod
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
//import com.example.market.*
//import com.example.market.comment.Comment
//import com.example.market.models.Order
//import com.example.market.models.Product
//import com.example.market.viewUtils.ImageViewLoader
//import com.example.market.viewUtils.toast

//
///**
// * Thumbnail crossfade duration
// * */
//private const val DRAWABLE_CROSSFADE_DURATION = 200
//
///**
// * Drawable transition options with crossfade
// * */
//private val DRAWABLE_TRANSITION_CROSSFADE = DrawableTransitionOptions.withCrossFade(
//    DRAWABLE_CROSSFADE_DURATION)
//
//@BindingAdapter("commentUserName")
//fun commentUserName(textView: TextView,comment: Comment) {
//    comment.apply {
//        textView.text = if (parentCommentId!=null&&parentCommentId!=repliedToId) {
//            "$userName > $repliedTo"
//        } else userName
//    }
//}
//
///**
// * Drawable thumbnail size
// * */
//private const val THUMBNAIL_SIZE = 0.007f
//
//@BindingAdapter("url","drawableResource","placeholder","circleCrop","fade","overrideWidth","overrideHeight","thumbnail","aspectRatio","centerCrop",requireAll = false)
//fun ImageView.load(
//    url: String?=null,
//    drawableResource:Int?=null,
//    placeHolder: Drawable?=null,
//    drawableToLoad: Drawable?=null,
//    circleCrop: Boolean = false,
//    fade: Boolean ?=null,
//    overrideWidth: Int ?=null,
//    overrideHeight: Int ?= null,
//    thumbnail: Boolean?=null,
//    aspectRatio:Float?=null,
//    centerCrop: Boolean = false
//) {
//    if (this is ImageViewLoader) {
//        this.url = url
//        this.drawableResource = drawableResource
//        this.placeHolder = placeHolder
//        this.drawableToLoad = drawableToLoad
//        this.circleCrop = circleCrop
//        this.fade = fade
//        this.overrideHeight = overrideHeight
//        this.overrideWidth = overrideWidth
//        this.thumbnail = thumbnail
//        this.aspectRatio = aspectRatio
//        canRequestLayout = false
//        requestLayout = true
//        requestLayout()
//    } else {
//        Glide.with(MyApplication.appContext).load(url).into(this)
//    }
//}
//
//fun increaseOrderCount(order: Order, increase: Boolean, view: TextView?=null) {
//    order.apply {
//        count = if (increase) count + 1 else if (count > 1) count - 1 else count
//        view?.let { setOrderCount(count,it) }
//    }
//}
//
//fun setOrderCount(count: Int,view: TextView) {
//    view.apply {
//        text = count.toString()
//        animate().scaleY(1.2f).scaleX(1.2f).setDuration(200).withEndAction {
//            animate().scaleX(1f).scaleY(1f).duration = 200
//        }
//    }
//}
///**
// * Sets text into textview check wether texts are same
// */
//@BindingAdapter("safeText")
//fun TextView.safeText(newText: String){
//    if (checkIsNotEmpty(text,newText)) return
//
//    text = newText
//}
///**
// * Sets top product
// */
//@BindingAdapter("top")
//fun TextView.top(top: String) {
//    if (checkIsNotEmpty(text,top)) {
//        visibleOrGone(false)
//        return
//    }
//
//    visibleOrGone(true)
//    text = top
//}
//
//@BindingAdapter("cost")
//fun TextView.cost(cost: String) {
//    text =  formatCurrency(cost.toLong())
//}
//
//@BindingAdapter("shipping")
//fun TextView.shipping(shipping: String) {
//    text = try {
//        formatCurrency(shipping.toLong())
//    }catch (e:Exception) {
//        shipping
//    }
//}
//
//val defaultShipping = ShippingLocation().apply {
//    cost = "----"
//    adress = "Qayerga dostavka?"
//}
//
//@BindingAdapter("shippingLocationCost")
//fun TextView.shippingLocationCost(shippingLocationCost: Any?=null) {
//    val shipping = currentUser?.shippingLocation
//
//    if (shipping!=null) {
//        shipping(shipping.cost)
//    } else {
//        text =  defaultShipping.cost
//    }
//}
//
//@BindingAdapter("shippingLocationAdress")
//fun TextView.shippingLocationAdress(shippingLocationAdress: Any?=null){
//    val shipping = currentUser?.shippingLocation
//
//    text = if (shipping!=null) {
//        shipping.adress
//    } else {
//        defaultShipping.adress
//    }
//}
//
//fun parseCardNumber(cardNumber: Long): String {
//    val buffer = StringBuffer()
//    var offset = 0
//    cardNumber.toString().forEachIndexed { _, c ->
//        if(offset == 4) {
//            buffer.append(' ')
//            offset = 0
//        }
//        offset +=1
//        buffer.append(c)
//    }
//    return buffer.toString().trim()
//}
//
//@BindingAdapter("shippingType")
//fun TextView.shippingType(shippingType: Any ?= null) {
//    val location = currentUser?.shippingLocation
//
//    text = location?.type ?: defaultShipping.type
//}
//
//@BindingAdapter("shippingSpendTime")
//fun TextView.shippingSpendTime(time: Any ?= null) {
//    val location = currentUser?.shippingLocation
//    val spendTime = location?.timeSpendMinute ?: 24
//    text = "* $spendTime minut" + if (location == null) " (kamida)" else " (uzogi bilan)"
//}
//
//@BindingAdapter("discount")
//fun TextView.discount(product:Product) {
//    text = formatCurrency(((product.narxi.toLong() * product.discount.toLong())/100)+product.narxi.toLong())
//}
//
//@BindingAdapter("percent","percentChange")
//fun TextView.percent(percent: String,percentChange: Boolean){
//    val p = if (percentChange) "-" else "+"
//    text = "$p$percent%"
//}
//
//@BindingAdapter("visibleOrGone")
//fun View.visibleOrGone(visible: Boolean){
//    visibility = if (visible) View.VISIBLE else View.GONE
//}
//fun hashTag(text: String) = "#$text"
//
//
//
//

