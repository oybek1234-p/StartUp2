package com.org.ui.actionbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.StateSet
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.annotation.AttrRes
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.ActionBar.log
import com.airbnb.lottie.LottieAnimationView
import com.example.market.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.googlecode.mp4parser.authoring.Edit
import com.org.market.*
import com.org.ui.LaunchActivity
import com.org.ui.cells.ConstraintLayoutCell
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlin.math.ceil
import kotlin.math.sqrt

private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)

data class Theme(val themeResId: Int, val photoRes: Int, val name: String, val baseColor: Int)

var themes = ArrayList<Theme>().apply {
    add(Theme(R.style.Theme_StartUp, R.drawable.light_theme_image, "Light", 0))
    add(Theme(R.style.Theme_StartUp2, R.drawable.night_theme_image, "Night", 0))
}
var currentThemeInfo = themes.first()

@BindingAdapter("backgroundColor")
fun View.backgroundColor(@AttrRes attrRes: Int) {
    setBackgroundColor(getThemeColor(attrRes))
}

@BindingAdapter("backgroundTint")
fun View.backgroundTint(@AttrRes attrRes: Int) {
    backgroundTintList = ColorStateList.valueOf(getThemeColor(attrRes))
}

@BindingAdapter("dotColor")
fun WormDotsIndicator.dotColor(@AttrRes attrRes: Int) {
    setDotIndicatorColor(getThemeColor(attrRes))
}

@BindingAdapter("dotsStrokeColor")
fun WormDotsIndicator.dotsStrokeColor(@AttrRes attrRes: Int) {
    setStrokeDotsIndicatorColor(getThemeColor(attrRes))
}

@BindingAdapter("drawableTint")
fun TextView.drawableTint(@AttrRes attrRes: Int) {
    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(getThemeColor(attrRes)))
}

@BindingAdapter("foregroundColor")
fun View.foregroundTint(@AttrRes attrRes: Int) {
    foregroundTintList = ColorStateList.valueOf(getThemeColor(attrRes))
}

@BindingAdapter("textColor")
fun TextView.textColor(@AttrRes attrRes: Int) {
    setTextColor(getThemeColor(attrRes))
}

@BindingAdapter("textColorHint")
fun TextView.textColorHint(@AttrRes attrRes: Int) {
    setHintTextColor(getThemeColor(attrRes))
}

@BindingAdapter("progressColor")
fun ProgressBar.progressColor(@AttrRes attrRes: Int) {
    indeterminateTintList = ColorStateList.valueOf(getThemeColor(attrRes))
}

@BindingAdapter(
    "thumbChecked",
    "thumbUnchecked",
    "trackChecked",
    "trackUnchecked"
)
fun SwitchMaterial.switchMaterialColors(
    @AttrRes thumbChecked: Int,
    @AttrRes thumbUnchecked: Int,
    @AttrRes trackChecked: Int,
    @AttrRes trackUnchecked: Int,
) {
    thumbTintList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)),
        intArrayOf(getThemeColor(thumbChecked), getThemeColor(thumbUnchecked)))

    trackTintList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)),
        intArrayOf(getThemeColor(trackChecked), getThemeColor(trackUnchecked)))
}

@BindingAdapter("applyTintList")
fun Button.applyTintList(attr: Any?=null) {
    backgroundTintState(R.attr.colorSecondary,
        R.attr.colorBackground,
        android.R.attr.state_enabled)
    textColorList(R.attr.colorOnSecondaryHigh,
        R.attr.colorOnSurfaceLow,
        android.R.attr.state_enabled)
}

fun View.backgroundTintState(
    activeColor: Int,
    unActiveColor: Int,
    state: Int
) {
    backgroundTintList = ColorStateList(
        arrayOf(intArrayOf(state),
            intArrayOf(-state)),
        intArrayOf(getThemeColor(activeColor), getThemeColor(unActiveColor)))
}

fun TextView.textColorList(
    activeColor: Int,
    unActiveColor: Int,
    state: Int
) {
    setTextColor(ColorStateList(
        arrayOf(intArrayOf(state),
            intArrayOf(-state)),
        intArrayOf(getThemeColor(activeColor), getThemeColor(unActiveColor))))
}

@BindingAdapter(
    "itemsTextColorSelected",
    "itemsIconTintSelected",
    "itemsTextColorUnSelected",
    "itemsIconTintUnSelected")
fun BottomNavigationView.itemsColor(
    @AttrRes itemsTextColorSelected: Int,
    @AttrRes itemsIconTintSelected: Int,
    @AttrRes itemsTextColorUnSelected: Int,
    @AttrRes itemsIconTintUnSelected: Int,
) {
    itemIconTintList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)),
        intArrayOf(getThemeColor(itemsIconTintSelected), getThemeColor(itemsIconTintUnSelected)))

    itemTextColor = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)),
        intArrayOf(getThemeColor(itemsTextColorSelected), getThemeColor(itemsTextColorUnSelected)))

}

@BindingAdapter(
    "needDivider",
    "dividerMarginStart",
    "dividerMarginEnd", requireAll = false)
fun ConstraintLayoutCell.divider(
    needDivider: Boolean,
    dividerMarginStart: Int = 0,
    dividerMarginEnd: Int = 0,
) {
    this.needDivider = needDivider
    this.dividerMarginStart = dividerMarginStart.toFloat()
    this.dividerMarginEnd = dividerMarginEnd.toFloat()
}

fun Context.getThemeColor(attrRes: Int): Int {
    val typedValue = TypedValue()
    if (!theme.resolveAttribute(attrRes, typedValue, true)) {
        log("Color not found attr id $attrRes")
    }
    return typedValue.data
}

fun View.getThemeColor(attr: Int): Int {
    return context.getThemeColor(attr)
}

@BindingAdapter("imageTint")
fun ImageView.imageTint(@AttrRes attrRes: Int) {
    imageTintList = ColorStateList.valueOf(getThemeColor(attrRes))
}

fun TextInputLayout.clearErrorOnTextChange() {
    editText!!.doOnTextChanged { text, start, before, count ->
        setErrorMessage(null)
    }
}

fun TextInputLayout.textOrEmptyError(message: String): String? {
    val text = editText!!.text.toString()
    val error = text.isEmpty()
    setErrorMessage(if (error) message else null)
    if (error) {
        shakeView(this)
    }
    return if (error) null else text
}

fun TextInputLayout.setErrorMessage(message: String?=null) {
    isErrorEnabled = message != null
    error = message
}
fun View.shakeView() {
    shakeView(this)
}
fun View.showError(message: String?=null) {
    toast(message)
    shakeView(this)
}

@BindingAdapter("lottieUrl")
fun LottieAnimationView.lottieUrl(url:String?) {
    if (url.isNullOrEmpty()) return
    setAnimationFromUrl(url)
}

@BindingAdapter("lottieRes")
fun LottieAnimationView.lottieRes(res: Int) {
    setAnimation(res)
}

fun createsSelectorDrawable(color: Int): Drawable {
    return createSelectorDrawable(color, 1, -1)
}

fun createSelectorDrawable(color: Int, maskType: Int, radius: Int): Drawable {
    var maskDrawable: Drawable? = null
    if ((maskType == 1 || maskType == 5)) {
        maskDrawable = null
    } else if (maskType == 1 || maskType == 3 || maskType == 4 || maskType == 5 || maskType == 6 || maskType == 7) {
        maskPaint.color = -0x1
        maskDrawable = object : Drawable() {
            var rect: RectF? = null
            override fun draw(canvas: Canvas) {
                val bounds = bounds
                if (maskType == 7) {
                    if (rect == null) {
                        rect = RectF()
                    }
                    rect!!.set(bounds)
                    canvas.drawRoundRect(
                        rect!!,
                        dp(6f).toFloat(),
                        dp(6f).toFloat(),
                        maskPaint
                    )
                } else {
                    val rad: Int = if (maskType == 1 || maskType == 6) {
                        dp(20f)
                    } else if (maskType == 3) {
                        bounds.width().coerceAtLeast(bounds.height()) / 2
                    } else {
                        ceil(sqrt(((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX()) + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())).toDouble()))
                            .toInt()
                    }
                    canvas.drawCircle(bounds.centerX().toFloat(),
                        bounds.centerY().toFloat(),
                        rad.toFloat(),
                        maskPaint)
                }
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }
    } else if (maskType == 2) {
        maskDrawable = ColorDrawable(-0x1)
    }
    val colorStateList = ColorStateList(arrayOf(StateSet.WILD_CARD), intArrayOf(color))
    val rippleDrawable = RippleDrawable(colorStateList, null, maskDrawable)

    if (maskType == 1) {
        rippleDrawable.radius = if (radius <= 0) dp(20f) else radius
    } else if (maskType == 5) {
        rippleDrawable.radius = RippleDrawable.RADIUS_AUTO
    }
    return rippleDrawable
}

fun Context.presentFragment(fragment: BaseFragment<*>,removeLast: Boolean) {
    if(this is LaunchActivity) {
        presentFragment(fragment,removeLast)
    } else {
        val activity = findActivity(this)
        if (activity is LaunchActivity) {
            presentFragment(fragment, removeLast)
        }
    }
}
fun createCircleDrawable(size: Int, color: Int): Drawable {
    return ShapeDrawable(
        OvalShape().apply {
            resize(size.toFloat(), size.toFloat())
        }).apply {
        intrinsicWidth = size
        intrinsicHeight = size
        paint?.color = color
    }
}


fun changeBrightness(color: Int, amount: Float): Int {
    var r = (Color.red(color) * amount).toInt()
    var g = (Color.green(color) * amount).toInt()
    var b = (Color.blue(color) * amount).toInt()
    r = if (r < 0) 0 else r.coerceAtMost(255)
    g = if (g < 0) 0 else g.coerceAtMost(255)
    b = if (b < 0) 0 else b.coerceAtMost(255)
    return Color.argb(Color.alpha(color), r, g, b)
}

fun parseCardNumber(string: String) : String {
    val stringBuffer = StringBuffer()
    var offset = 0
    string.forEach { c ->
        if (offset==4) {
            stringBuffer.append(' ')
            offset = 0
        }
        stringBuffer.append(c)
        offset +=1
    }
    return stringBuffer.toString()
}

object PhotoUrls {
    const val userPlaceholder = "https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png"
    const val productPlaceholderBlur = "https://cdn.pixabay.com/photo/2015/06/24/02/12/the-blurred-819388_1280.jpg"
}

object PlaceholderTexts {
    const val newUserText = "New user"
}

