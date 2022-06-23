package com.example.market.binding

//import android.text.TextUtils
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.animation.AnticipateInterpolator
//import android.view.animation.AnticipateOvershootInterpolator
//import androidx.annotation.LayoutRes
//import androidx.core.view.doOnNextLayout
//import androidx.databinding.DataBindingUtil
//import androidx.databinding.ViewDataBinding
//import com.example.market.CURRENCY_UZS
//import com.example.market.utils.currency
//import com.example.market.utils.getCurrency
//import com.example.market.viewUtils.toast
//import java.text.DecimalFormat
//
///**
// * Currency formatter
//
///**
// * Base inflater for all views
// */
//lateinit var appInflater: LayoutInflater
///**
// * Inflates binding types
// */
//fun <T: ViewDataBinding> inflateBinding(
//    parent: ViewGroup?,
//    @LayoutRes layId: Int,
//    attachToParent: Boolean = false
//) :T {
//    return DataBindingUtil.inflate(
//        appInflater,
//        layId,
//        parent,
//        attachToParent
//    )
//}

//fun View.playPopupAnimation() {
//    doOnNextLayout {
//        scaleX = 0.8f
//        scaleY = 0.8f
//        alpha = 0f
//        animate()
//            .scaleY(1f)
//            .scaleX(1f)
//            .alpha(1f)
//            .setInterpolator(AnticipateOvershootInterpolator(2f))
//            .setDuration(300)
//            .start()
//    }
//}
//
///**
// * Checks texts same or is empty
// * */
//fun checkIsNotEmpty(
//    oldText: CharSequence?,
//    newText: String?
//) : Boolean {
//    return (oldText.toString()==newText|| TextUtils.isEmpty(newText))
//}
//var dollarRate = 0.000093
//
///**
// * Formats currency
// */
//fun formatCurrency(long: Long): String {
//    return if (long==0L) "Bepul" else formatter.format(if (currency== CURRENCY_UZS) long else (long.toDouble() * dollarRate)) + " ${getCurrency()}"
//}
