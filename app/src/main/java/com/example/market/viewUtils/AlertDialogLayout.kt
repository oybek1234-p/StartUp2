package com.example.market.viewUtils

//import android.view.Gravity
//import android.view.View
//import com.example.market.Alert
//import com.example.market.R
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.AlertDialogBinding
//
//class AlertDialogLayout(var view: View?) {
//    private var binding: AlertDialogBinding? = inflateBinding(null, R.layout.alert_dialog)
//
//    private var popWindowLayout: PopupWindowLayout? = PopupWindowLayout(view!!.context).apply {
//        addView(binding!!.root)
//    }
//
//    var popupDialog: PopupDialog?= PopupDialog(popWindowLayout!!, DIALOG_ANIMATION_ALERT_DIALOG).apply {
//    }
//
//    private var alertData: Alert?=null
//
//    private fun setData(alert: Alert) {
//        binding?.apply {
//            data = alert.also { alertData = it }
//            executePendingBindings()
//        }
//    }
//
//    fun dismiss() {
//        popupDialog?.dismiss()
//    }
//
//    fun showDialog(alert: Alert) {
//        setData(alert)
//
//        popupDialog?.show(view!!,Gravity.CENTER,0,0,true)
//    }
//
//    fun getBindingView() = binding!!.root
//
//    fun getBinding() = binding!!
//}