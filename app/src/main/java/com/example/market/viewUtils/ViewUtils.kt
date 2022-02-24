package com.example.market.viewUtils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.market.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.DecimalFormat
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.EditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.market.binding.formatCurrency
import com.example.market.navigation.FragmentController
import com.example.market.utils.AndroidUtilities
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.reflect.Field
import java.util.concurrent.Future
import android.os.VibrationEffect

import androidx.core.content.ContextCompat.getSystemService

import android.os.Vibrator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


fun dpToPx(value: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        MyApplication.appContext.resources.displayMetrics
    ).toInt()
}
fun View.setForegroundColor(color: Int) {
    foreground = ColorDrawable(color)
}
fun View.setRadiusBackground(radius: Float,color: Int) {
    var drawable = background
    if (drawable==null||drawable !is GradientDrawable){
        drawable = GradientDrawable()
    }
    drawable.apply {
        cornerRadius = radius
        setTint(color)
        background = this
    }
}
var DEFAULT_BACKGROUND_RADIUS = AndroidUtilities.dp(4f)
var DEFAULT_BACKGROUND_COLOR = Color.LTGRAY

fun View.setRadiusForeground(radius: Float,color: Int) {
    var drawable = foreground
    if (drawable==null||drawable !is GradientDrawable){
        drawable = GradientDrawable()
    }
    drawable.apply {
        cornerRadius = radius
        setTint(color)
        foreground = this
    }
}

fun View.setFlickerView() {
    setRadiusForeground(
        DEFAULT_BACKGROUND_RADIUS.toFloat(),
        DEFAULT_BACKGROUND_COLOR
    )
}
 var anticipateInterpolator = AnticipateOvershootInterpolator(10f)

/**
 * Sets item layout full span
 */
fun setFullSpan(holder: RecyclerView.ViewHolder,fullSpan: Boolean) {
    holder.itemView.layoutParams.apply {
        if (this is StaggeredGridLayoutManager.LayoutParams) {
            isFullSpan = fullSpan
        }
    }
}

fun nextPage(viewPager: ViewPager2) {
    viewPager.apply {
        if (!isAttachedToWindow) {
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    removeOnAttachStateChangeListener(this)
                    nextPage(viewPager)
                }

                override fun onViewDetachedFromWindow(v: View?) {

                }
            })
            return
        }
        MyApplication.handler.postDelayed({
            nextPage(viewPager)
        },6000)
        if (adapter==null) return
        val isEnd = currentItem == viewPager.adapter!!.itemCount - 1
        currentItem = if (isEnd) 0 else currentItem + 1
    }
}

 fun changeNavItem(bottomNavigationView: BottomNavigationView, removeId:Int, id: Int, icon: Int, title:String) {
    bottomNavigationView.apply {
        var groupId: Int
        var order: Int
        menu.apply {
            findItem(removeId)?.let {

                groupId = it.groupId
                order = it.order

                removeItem(removeId)
                add(groupId,id,order,title).apply {
                    setIcon(icon)
                }
            }
        }
    }
}
 fun useBlackText(color1: Int, color2: Int): Boolean {
    val r1 = Color.red(color1) / 255.0f
    val r2 = Color.red(color2) / 255.0f
    val g1 = Color.green(color1) / 255.0f
    val g2 = Color.green(color2) / 255.0f
    val b1 = Color.blue(color1) / 255.0f
    val b2 = Color.blue(color2) / 255.0f
    val r = r1 * 0.5f + r2 * 0.5f
    val g = g1 * 0.5f + g2 * 0.5f
    val b = b1 * 0.5f + b2 * 0.5f
    val lightness = 0.2126f * r + 0.7152f * g + 0.0722f * b
    val lightness2 = 0.2126f * r1 + 0.7152f * g1 + 0.0722f * b1
    return lightness > 0.705f || lightness2 > 0.705f
}

fun getPixelsInCM(cm: Float): Float {
    return cm / 2.54f * MyApplication.appContext.resources.displayMetrics.xdpi
}

fun presentFragmentRemoveLast(context: Context,fragment: Fragment,removeLast: Boolean,anim: Array<Int> = FragmentController.openSearchFragment) {
    (context as MainActivity).apply {
        fragmentController?.presentFragmentRemoveLast(fragment, anim, removeLast)
    }
}

private val any = Any()
private val decimialFormat = DecimalFormat("#,###")

fun getNumberFormat(number: Long): String {
    return decimialFormat.format(number)
}

fun toast(context: Context?=null,message:String?) {
    if (message==null) return
    return Toast.makeText(MyApplication.appContext,message,Toast.LENGTH_SHORT).show()
}
fun toast(text:String?){
    toast(message = text)
}
fun getCostText(cost: String): String {
    val mCost = getTrimmedText(cost,16)
    val currency = if (MyApplication.currency == CURRENCY_UZS) SUM else DOLLAR
    return getNumberFormat(mCost.toLong()) + " $currency"
}
fun vibrate(millis: Long = 500) {
    val v = MyApplication.appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v!!.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        //deprecated in API 26
        v!!.vibrate(millis)
    }
}
fun getDiscountText(discountPercent: Int,cost: String): String {
    val mCost = 7484844.toString()
    val discountCost = discountPercent * mCost.toLong() / 100
    val currency = if (MyApplication.currency == CURRENCY_UZS) SUM else DOLLAR
    return getNumberFormat(discountCost) + " $currency"
}

fun getDiscountPercentText(discountPercent: Int): String {
    return "-$discountPercent%"
}
private var dostavkaAdresiText = MyApplication.appContext.getText(R.string.dostavka_adresi)
private const val SUM = "sum"
private const val DOLLAR = "$"

fun getShippingText(shipping: String): String {
    val t = getTrimmedText(shipping,16)
    val mShipping =  (5000..20000 ).random().toLong()
    val currency = if (MyApplication.currency == CURRENCY_UZS) SUM else DOLLAR
    return "$dostavkaAdresiText:  ${getNumberFormat(mShipping)} $currency"
}

private const val SUBSTRING_LENGTH_NONE = -1
fun getTrimmedText(t: String,substringLength: Int = SUBSTRING_LENGTH_NONE): String{
    t.trim().apply {
        if (substringLength != SUBSTRING_LENGTH_NONE){
            if (length > substringLength){
                return substring(0, substringLength)
            }
        }
        return this
    }
}

private val SYNCHRONIZED = Any()
@DelicateCoroutinesApi
fun setTextAsync(textView: AppCompatTextView, text: String) {

    val future: Future<PrecomputedTextCompat> = PrecomputedTextCompat.getTextFuture(
        text,textView.textMetricsParamsCompat,null)

    // and pass future to TextView, which awaits result before measuring
    // and pass future to TextView, which awaits result before measuring
    textView.setTextFuture(future)
//    synchronized(SYNCHRONIZED){
//        val mParams = TextViewCompat.getTextMetricsParams(textView)
//        val ref = WeakReference(textView)
//        GlobalScope.launch(Dispatchers.Default) {
//            // worker thread
//            val pText = PrecomputedTextCompat.create(text, mParams)
//            GlobalScope.launch(Dispatchers.Main) {
//                // main thread
//                ref.get()?.let { a ->
//                    TextViewCompat.setPrecomputedText(textView, pText)
//                }
//            }
//        }
//    }
}

fun applyTopVisiblity(textView: TextView,top: String){
    val isTop = top != ""
    textView.apply {
        if (isTop) { text = top }
        visibility = if (isTop) View.VISIBLE else View.GONE
    }
}

private val DRAWABLE_TRANSITION = DrawableTransitionOptions.withCrossFade(200)
private const val thumbnailSize = 0.0007f
private const val skipMemoryCache = true
private const val placeholderId = R.drawable.item_background2
private var imageSize = arrayOf(0,0)

fun loadImage(imageView: ImageView,url: String, scale: Float){
    try {
        Glide
            .with(MyApplication.appContext)
            .load(url)
            .thumbnail(thumbnailSize)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .priority(Priority.HIGH)
            .transition(DRAWABLE_TRANSITION)
            .into(imageView)
    } catch (e: Exception) {

    }
    imageSize[0] = MyApplication.homeProductWidth
    imageSize[1] = imageSize[0] * scale.toInt()

}

fun <T,M> changeDeclaredField(obj: Class<T>,p : T,fieldName: String,value: M?){
        val f: Field = obj.getDeclaredField(fieldName)
        f.isAccessible = true
        
        f.set(p,value)
}

fun doAfterLayout(
    view: View,
    runnable: Runnable,
) {
    val listener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            /* Layout pass done, unregister for further events */
            view.viewTreeObserver
                .removeOnGlobalLayoutListener(this)
            runnable.run()
        }
    }
    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
}

 fun getDistance(start: com.google.android.gms.maps.model.LatLng,end: com.google.android.gms.maps.model.LatLng): FloatArray {
    val results = FloatArray(4)

    Location.distanceBetween(
        start.latitude,
        start.longitude,
        end.latitude,
        end.longitude,
        results
    )

    return results
}

fun snackBar(view:View?,text: String){
    view?.let {
        Snackbar.make(view,text,Snackbar.LENGTH_SHORT).show()
    }
}

fun getCostForDistance(latLng: com.google.android.gms.maps.model.LatLng,costPerKm: Int): Long{
    return (costPerKm * getDistance(DetailsFragment.latLngTashkent, latLng)[0] / 1000f).toLong()
}

fun setLightStatusBar(activity: Activity,boolean: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = activity.window.decorView.systemUiVisibility
        flags = flags or if (boolean) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        activity.window.decorView.systemUiVisibility = flags
    }
}

fun clearLightStatusBar(view: View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = view.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        view.systemUiVisibility = flags
    }
}

fun getRecyclerChildSafe(recycler: RecyclerView,position: Int) : RecyclerView.ViewHolder? {
    recycler.children.forEach { view ->
        recycler.findContainingViewHolder(view)?.let {
            if (it.adapterPosition == position) {
                return it
            }
        }
    }
    return null
}

fun checkIsEmptyForParent(viewGroup: ViewGroup): Boolean {
    viewGroup.children.forEach {
        if (it is EditText||it is TextInputEditText) {
            val editText = it as EditText
            if (editText.text.isNullOrEmpty()) {
                toast(editText.text!!.isEmpty().toString())
                it.requestFocus()
                it.error = "Bush joyni tuldiring"
                return false
            }
        } else if (it is ViewGroup) {
            if (!checkIsEmptyForParent(it)){
                return false
            }

        }
    }
    return true
}


