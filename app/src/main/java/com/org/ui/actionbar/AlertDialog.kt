package com.org.ui.actionbar

import android.content.Context
import android.view.Gravity
import com.example.market.R
import com.example.market.databinding.AlertDialogBinding
import com.org.market.findActivity
import com.org.ui.components.inflateBinding
import com.org.ui.components.visibleOrGone
import com.org.ui.LaunchActivity

data class Alert(
    var title: String,
    var message: String,
    var cancelName: String,
    var actionName: String,
    var cancelOnClick: (() -> Unit)?,
    var actionOnClick: (() -> Unit)?,
    var iconUrl: String?=null,
    var iconResource: Int?=null,
    var isPhoto: Boolean = true
)

open class AlertDialog(var context: Context) {
    private var binding: AlertDialogBinding ?= null
    private var popupWindowLayout: PopupWindowLayout ?= null
    private var popDialog: PopupDialog ?= null
    private var isCreated = false
    var destroyOnDismiss = true
    var dismissOnButtonClick = true

    init {
        findActivity(context)?.apply {
            if (this is LaunchActivity) {
                alertDialog = this@AlertDialog
            }
        }
    }

    private var alert = Alert(
        "Title",
        "Message",
        "",
        "",null,null)

    fun setCustomAlert(alert: Alert) {
        this.alert = alert
        notifyUi()
    }

    fun isShowing() = popDialog?.isShowing == true

    fun notifyUi(forceBind: Boolean = false) {
        binding?.apply {
            data = alert
            if (forceBind || isShowing()) {
                executePendingBindings()
            }
        }
    }

    fun setAlert(alert: Alert) {
        this.alert = alert
        notifyUi()
    }

    fun setIsPhoto(isPhoto: Boolean) {
        alert.isPhoto = isPhoto
        notifyUi()
    }

    fun setImageUrl(url: String) {
        alert.iconUrl = url
        notifyUi()
    }

    fun setImageResource(res: Int) {
        alert.iconResource = res
        notifyUi()
    }

    fun setPositiveButton(name: String,onClick: (() -> Unit)?) {
        alert.apply {
            actionName = name
            actionOnClick = onClick
        }
        notifyUi()
    }

    fun setNegativeButton(name: String,onClick: (() -> Unit)?) {
        alert.apply {
            cancelName = name
            cancelOnClick = onClick
        }
        notifyUi()
    }

    fun setTitle(title: String) {
        alert.title = title
        notifyUi()
    }

    fun setMessage(message: String) {
        alert.message = message
        notifyUi()
    }

    fun destroy() {
        popDialog?.apply {
            isCreated = false
            if (isShowing) {
                dismiss()
            }
            popupWindowLayout = null
            binding = null
            popDialog = null
        }
    }

    protected open fun create() {
        if (isCreated) return
        binding = inflateBinding<AlertDialogBinding>(null, R.layout.alert_dialog).also { binding->
            popupWindowLayout = PopupWindowLayout(context).apply {
                addView(binding.root)
                binding.apply {
                    cancelButton.setOnClickListener {
                        data?.cancelOnClick?.invoke()
                        dismiss()
                    }
                    actionButton.setOnClickListener {
                        data?.actionOnClick?.invoke()
                        dismiss()
                    }
                }
                popDialog = PopupDialog(this, DIALOG_ANIMATION_ALERT_DIALOG).apply {
                    setOnDismissListener {
                        if (destroyOnDismiss) {
                            destroy()
                        }
                    }
                }

                isCreated = true
                onCreateView(binding)
            }
        }
    }

    fun dismiss() {
        popDialog?.dismiss()
    }

    fun check() {
        binding?.apply {
            alert.apply {
                val needIcon = iconResource == null && iconUrl == null
                iconView.visibleOrGone(!needIcon)
                cancelButton.visibleOrGone(actionName.isNotEmpty())
                if (actionName.isEmpty()) {
                    actionButton.text = context.getString(R.string.ok)
                }
            }
        }
    }

    fun show(): AlertDialog {
        create()
        notifyUi(true)
        check()
        popDialog?.show()
        return this
    }

    open fun onCreateView(binding: AlertDialogBinding) {
        //
    }
}
