package com.example.market.toast
//
//import android.animation.ValueAnimator
//import android.content.Context
//import android.os.Looper
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import com.example.market.MyApplication
//import com.example.market.R
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.ToastLayoutBinding
//import com.google.android.material.snackbar.Snackbar
//import java.lang.ref.WeakReference
//
//object ToastController {
//    data class Toast(val iconRes: Int,
//                     val lottie: Boolean, val message: String, val actionIconRes: Int,
//                     val actionName: String, val actionOnClick: (view: View) -> Unit, val duration: Long,
//                     val enqueue: Boolean)
//
//    private var handler = android.os.Handler(Looper.getMainLooper())
//
//    private var currentToast: Toast?=null
//    private var nextToast: Toast?=null
//
//    private var showRunnable: Runnable?=null
//    private var cancellRunnable: Runnable?=null
//
//    private var toastBinding: ToastLayoutBinding?=null
//
//    private var animator = ValueAnimator.ofFloat()
//
//    private var isShow = false
//
//    fun show(parent: ViewGroup,toast: Toast){
//        nextToast = toast
//
//    }
//
//    fun show(parent: ViewGroup) {
//        if (nextToast==null) {
//            return
//        }
//        currentToast = nextToast
//
//        val binding = createBinding(parent)
//
//
//        setData(currentToast!!)
//
//    }
//
//
//
//    fun setData(toast: Toast){
//        toastBinding?.apply {
//
//            iconView.apply {
//                if (toast.lottie) {
//                    setAnimation(toast.iconRes)
//                    repeatCount = 1
//                    playAnimation()
//                }
//            }
//
//            messageView.text = toast.message
//            actionIconView.setImageResource(toast.actionIconRes)
//            actionNameView.text = toast.actionName
//            actionNameView.setOnClickListener(toast.actionOnClick)
//        }
//    }
//
//    fun createBinding(parent: ViewGroup) = if (toastBinding!=null) toastBinding!! else (inflateBinding(parent,R.layout.toast_layout) as ToastLayoutBinding).also { toastBinding = it }
//}