package com.org.ui.actionbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.forEach
import androidx.databinding.OnRebindCallback
import com.ActionBar.log
import com.example.market.R
import com.example.market.databinding.ActionBarBinding
import com.org.market.dp
import com.org.ui.LaunchActivity
import com.org.ui.components.ImageViewLoader
import com.org.ui.components.inflateBinding
import com.org.ui.components.load
import com.org.ui.components.visibleOrGone

class ActionBar {

    var backButtonTouchEnabled = true
    var backButtonIsVisible = true
    set(value) {
        field = value
        actionBarBinding().backButton.visibleOrGone(value)
    }

    var menuItems = hashMapOf<Int, MenuItem>()
    var parentFragment: BaseFragment<*>? = null
    private var menuItemsPadding = dp(16f)

    fun onPause() {
        menuItems.values.forEach {
            it.dismissDialog()
        }
    }

    fun actionBarBinding(): ActionBarBinding = try {
        mActionBarBinding!!
    } catch (e: Exception) {
        log("ActionBar binding is null," +
                " do not call this method after onDestroyView")
        throw e
    }

    fun view() = mActionBarBinding?.root
    fun parent() = view()?.parent
    fun context() = view()?.context

    companion object {
        const val TAG = "ActionBar"
        const val MENU = "menu"
    }

    var mActionBarBinding: ActionBarBinding? =
        inflateBinding<ActionBarBinding>(
            null,
            R.layout.action_bar
        ).apply {
            mActionBarBinding = this
            root.tag = TAG
            backButton.tag = MENU

            addOnRebindCallback(object : OnRebindCallback<ActionBarBinding>() {
                override fun onBound(binding: ActionBarBinding?) {
                    super.onBound(binding)
                    binding?.menuContainer?.forEach {
                        it.apply {
                            if (tag == MENU && this is ImageView) {
                                background = createsSelectorDrawable(getThemeColor(R.attr.colorBackground))
                                imageTintList = ColorStateList.valueOf(getThemeColor(R.attr.colorOnPrimaryHigh))
                            }
                        }
                    }
                }
            })
            backButton.apply {
                isFocusable = true
                isClickable = true

                setOnClickListener {
                    if (backButtonTouchEnabled) {
                        (context() as LaunchActivity).onBackPressed()
                    }
                }
            }
        }

    var title = ""
        set(value) {
            field = value
            actionBarBinding().titleView.text = value
        }

    var subtitle = ""
        set(value) {
            field = value
            actionBarBinding().subtitleView.apply {
                visibleOrGone(value.isNotEmpty())
                text = value
            }
        }

    fun setPhoto(photoUrl: String) {
        setPhoto(url = photoUrl)
    }

    fun setPhoto(drawableRes: Int) {
        setPhoto(res = drawableRes)
    }

    fun setPhoto(drawable: Drawable?) {
        setPhoto(avatarDrawable = drawable)
    }

    private fun setPhoto(url: String? = null, avatarDrawable: Drawable? = null, res: Int? = null) {
        val isNull = url == null && avatarDrawable == null && res == null
        actionBarBinding().avatarView.apply {
            visibleOrGone(!isNull)
            if (!isNull) {
                actionBarBinding().avatarView.load(
                    url = url,
                    drawableResource = res,
                    circleCrop = true
                )
            }
        }
    }

    fun getMenuItem(id: Int) = menuItems[id]

    fun addMenuItem(id: Int,iconResource: Int): MenuItem {
        actionBarBinding().menuContainer.apply {
            return MenuItem(context).apply {
                tag = MENU
                background = createsSelectorDrawable(getThemeColor(R.attr.colorBackground))
                imageTintList = ColorStateList.valueOf(getThemeColor(R.attr.colorOnPrimaryHigh))

                with(menuItemsPadding) {
                    setPadding(this, 0, this, 0)
                }
                setImageResource(iconResource)
                menuItems[id] = this
                addView(this,
                    0,
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT))
            }
        }
    }

    fun addSubItem(
        parentId: Int,
        id: Int,
        drawableRes: Int,
        text: String,
        onClick: (view: View) -> Unit,
    ) {
        getMenuItem(parentId)?.addSubItem(id, drawableRes, text, onClick)
    }

    class MenuItem constructor(context: Context) : ImageViewLoader(context) {

        private var popDialog: PopupDialog? = null
        private var popWindowLayout: PopupWindowLayout? = null

        init {
            isClickable = true
            isFocusable = true
        }
        fun dismissDialog() {
            popDialog?.dismiss()
        }

        fun onDestroy() {
            popDialog = null
            popWindowLayout = null
        }

        init {
            isClickable = true
            isFocusable = true

            setOnClickListener { clickedView ->
                popDialog?.show(clickedView)
            }
        }

        fun createPopupDialog(): PopupWindowLayout =
            popWindowLayout ?: PopupWindowLayout(context).also {
                popWindowLayout = it
                popDialog = PopupDialog(it)
            }

        fun addSubItem(id: Int, drawableRes: Int, text: String, onClick: (view: View) -> Unit) {
            createPopupDialog().apply {
                addItem(id, text, drawableRes) {
                    onClick(it)
                    popDialog?.dismiss()
                }
            }
        }
    }
}
