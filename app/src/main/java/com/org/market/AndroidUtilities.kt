package com.org.market

import android.animation.Animator
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Vibrator
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.ActionBar.log
import com.org.net.models.*
import com.org.ui.LaunchActivity
import com.org.ui.actionbar.ActionBarLayout
import java.io.File
import java.io.FileWriter
import java.text.ParseException
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.roundToInt

var statusBarHeight = 0
var displaySize = Point()
var density = 1f
var screenRefreshRate = 60f

var displayMetrics = DisplayMetrics()
var decelerateInterpolator = DecelerateInterpolator()
var accelerateInterpolator = AccelerateInterpolator()
var overshootInterpolator = OvershootInterpolator(1f)
var anticipateOvershootInterpolator = AnticipateOvershootInterpolator()

fun openCallView(number: Long) {
    getApplicationContext().startActivity(
        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        })
}

fun highlightText(
    str: CharSequence,
    query: String,
    color: Int,
): CharSequence? {
    if (TextUtils.isEmpty(query) || TextUtils.isEmpty(str)) {
        return null
    }
    val s = str.toString().lowercase()
    val spannableStringBuilder = SpannableStringBuilder.valueOf(str)
    var i = s.indexOf(query)
    while (i >= 0) {
        try {
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(color), i, min(i + query.length, str.length), 0)
        } catch (e: Exception) {
            log(e)
        }
        i = s.indexOf(query, i + 1)
    }
    return spannableStringBuilder
}

fun currentTimeMillis() = System.currentTimeMillis()

fun newId() = currentTimeMillis().toString()
fun findActivity(context: Context): LaunchActivity? {
    if (context is Activity) return context as LaunchActivity
    if (context is ContextWrapper) return findActivity(context.baseContext)
    return null
}

fun calculateDateWithType(date: Long): Pair<Int, Long> {
    var day = 0L
    var hh = 0L
    var mm = 0L
    try {
        val currentTime = currentTimeMillis()
        val timeDiff: Long = currentTime - date
        day = TimeUnit.MILLISECONDS.toDays(timeDiff)
        hh = (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day))
        mm =
            (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(
                timeDiff)))
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return if (mm <= 60 && hh != 0L) {
        if (hh <= 60 && day != 0L) {
            Pair(DAY, day)
        } else {
            Pair(HOUR, hh)
        }
    } else {
        Pair(MINUTE, mm)
    }
}

fun getDateDifferenceText(date: Long): String {
    val value = calculateDateWithType(date)
    val type = value.first
    val dateValue = value.second
    if (dateValue == 0L) {
        return getDateText(DATE_NOW)
    }
    return dateValue.toString() + " ${
        when (type) {
            HOUR -> "h"
            DAY -> "d"
            MONTH -> "mh"
            YEAR -> "y"
            MINUTE -> "m"
            else -> "date"
        }
    }"
}

fun calculateDateExtended(date: Long): Pair<Int,Long> {
    val cDate = calculateDateWithType(date)
    val dateV = cDate.second
    var type = DATE_NEW

    when(cDate.first) {
        DAY -> {
            type = when (dateV) {
                1L -> {
                    DATE_TODAY
                }
                2L -> {
                    DATE_YESTERDAY
                }
                in 3..29 -> {
                    DATE_THIS_MONTH
                }
                else -> {
                    DATE_THIS_YEAR
                }
            }
        }
        MINUTE -> {
            type = if (dateV<=4) {
                DATE_NEW
            } else {
                DATE_TODAY
            }
        }
        HOUR -> {
            type = DATE_TODAY
        }
    }
    return Pair(type,dateV)
}

fun fillStatusBarHeight(context: Context) {
    if (statusBarHeight > 0) {
        return
    }
    statusBarHeight = getStatusBarHeight(context)
}

fun getStatusBarHeight(context: Context): Int {
    context.resources?.apply {
        val statusBarResId = getIdentifier("status_bar_height", "dimen", "android")
        return getDimensionPixelSize(statusBarResId)
    }
    return 0
}

fun calculateBitmapColor(bitmap: Bitmap): Int {
    try {
        Bitmap.createScaledBitmap(bitmap, 1, 1, false).apply {
            val pixel = getPixel(0, 0)
            if (bitmap != this) {
                recycle()
            }
            return pixel
        }
    } catch (e: Exception) {
        log(e)
    }
    return 0
}

fun calcDrawableColor(drawable: Drawable): Int {
    try {
        val bitmap = drawable.toBitmap()
        val color = calculateBitmapColor(bitmap)
        bitmap.recycle()
        return color
    } catch (e: java.lang.Exception) {
        log(e)
    }
    return 0
}

enum class RequestAdjustType {
    Pan,
    Resize,
    Nothing
}

fun requestAdjust(activity: Activity, type: RequestAdjustType) {
    activity.window.setSoftInputMode(
        when (type) {
            RequestAdjustType.Nothing -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            RequestAdjustType.Pan -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            RequestAdjustType.Resize -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
    )
}

fun createEmptyFile(file: File) {
    try {
        file.apply {
            if (exists()) {
                return
            }
            FileWriter(this).apply {
                flush()
                close()
            }
        }
    } catch (e: Exception) {
        log(e)
    }
}

val shadowHeight: Int
    get() {
        return when {
            density >= 4.0f -> {
                3
            }
            density >= 2.0f -> {
                2
            }
            else -> {
                1
            }
        }
    }

fun checkGpsOn(activity: Activity): Boolean {
    val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun showKeyboard(view: View?): Boolean {
    try {
        view?.apply {
            requestFocus()
            return (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                view,
                InputMethodManager.SHOW_IMPLICIT)
        }
    } catch (e: java.lang.Exception) {
        log(e)
    }
    return false
}

fun toast(message: String?) {
    runOnUiThread({
        message?.let {
            Toast.makeText(ApplicationLoader.applicationContext, it, Toast.LENGTH_LONG).show()
        }
    })
}

fun vibrate(delay: Long) {
    val manager =
        ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    manager.vibrate(delay)
    ActionBarLayout
}

fun shakeView(
    view: View,
    offset: Float = 20f,
    repeat: Int = 6,
    vibrate: Boolean = true,
) {
    view.animate().translationX(offset).setListener(object : Animator.AnimatorListener {
        override fun onAnimationCancel(animation: Animator?) {

        }

        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationStart(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            if (repeat == 0) {
                view.translationX = 0F
                return
            }
            shakeView(view, -offset, repeat - 1, false)
        }
    }).setInterpolator(decelerateInterpolator).setDuration(60).start()
    if (vibrate) {
        vibrate(50)
    }
}

fun getApplicationContext() = ApplicationLoader.applicationContext

fun getCurrentKeyboardLanguage() = (
        (ApplicationLoader.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .currentInputMethodSubtype as InputMethodSubtype).languageTag

fun runOnUiThread(runnable: Runnable?, delay: Long = 0L) {
    if (runnable == null) return
    ApplicationLoader.applicationHandler.apply {
        if (delay > 0L) {
            postDelayed(runnable, delay)
        } else {
            post(runnable)
        }
    }
}

fun cancelRunOnUIThread(runnable: Runnable?) =
    runnable?.let { ApplicationLoader.applicationHandler.removeCallbacks(it) }

fun hideKeyboard(view: View?) {
    try {
        view?.apply {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                if (!isActive) return
                hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    } catch (e: java.lang.Exception) {
        log(e)
    }
}

fun dp(value: Float) = (density * value).roundToInt()

fun getPixelsInCM(cm: Float, isX: Boolean): Float {
    return cm / 2.54f * if (isX) displayMetrics.xdpi else displayMetrics.ydpi
}

fun getDrawable(resId: Int) =
    AppCompatResources.getDrawable(ApplicationLoader.applicationContext, resId)

fun isViewInsideMotionEvent(motionEvent: MotionEvent, view: View): Boolean {
    if (motionEvent.x > view.left && motionEvent.x < view.right && motionEvent.y > view.top && motionEvent.y < view.bottom) {
        return true
    }
    return false
}

fun dpToPx(value: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        ApplicationLoader.applicationContext.resources.displayMetrics
    ).toInt()
}

fun checkDisplaySize(context: Context) {
    try {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay?.apply {
            getMetrics(displayMetrics)
            getSize(displaySize)
            screenRefreshRate = refreshRate
            density = displayMetrics.density
        }
    } catch (e: java.lang.Exception) {
        log(e)
    }
}

fun findView(viewGroup: ViewGroup, id: String): View? {
    val context = viewGroup.context
    val idRes: Int = context.resources.getIdentifier(id, "id", context.packageName)
    var view = viewGroup.findViewById<View>(idRes)
    if (view == null) {
        for (i in 0 until viewGroup.childCount) {
            val childView = viewGroup.getChildAt(i)
            if (childView is ViewGroup) {
                view = findView(childView, id)
                if (view != null) {
                    return view
                }
            }
        }
    }
    return view
}

fun addMediaToGallery(bitmap: Bitmap, name: String) {
    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

        }

        ApplicationLoader.applicationContext.contentResolver.apply {
            insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { it ->
                openOutputStream(it)?.use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    output.close()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
                update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues, null, null)
            }
        }
    } catch (e: java.lang.Exception) {
        log(e)
    }
}



















