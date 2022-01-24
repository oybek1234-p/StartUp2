package com.example.market

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.example.market.utils.FilesController

import com.example.market.viewUtils.dpToPx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val CURRENCY_UZS = 0
const val CURRENCY_USD = 1

class MyApplication : Application() {

    companion object {
        lateinit var appContext: Context
        lateinit var displaySize: Pair<Int,Int>
        var homeProductWidth: Int = 0
        var handler: android.os.Handler = android.os.Handler()

        lateinit var openSansSemiBold: Typeface
        lateinit var openSansBold: Typeface
        lateinit var openSansLight: Typeface
        lateinit var openSansExtraBold: Typeface
        lateinit var openSansRegular: Typeface

        lateinit var robotoRegular: Typeface
        lateinit var robotoMedium: Typeface
        lateinit var robotoBold: Typeface

        lateinit var titlePaint: TextPaint
        lateinit var topPaint: TextPaint
        lateinit var productCostPaint: TextPaint
        lateinit var productDiscountPaint: TextPaint
        lateinit var productDiscountPercentPaint : TextPaint
        lateinit var productShippingPaint: TextPaint

        var productTitleColor = 0
        var productTopColor = 0
        var productCostColor = 0
        var productDiscountColor = 0
        var productDiscountPercentColor = 0
        var productShippingColor = 0
        var productShippingBackgroundColor = 0

        var productTitleSize = 0
        var productTopSize = 0
        var productCostSize = 0
        var productDiscountSize = 0
        var productDiscountPercentSize = 0
        var productShippingSize = 0
        lateinit var sharedPreferences: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
        var currency:Int = CURRENCY_UZS;

        private var isInternetAviable = true
            set(value) {
                field = value
                internetAviableCallback.postValue(value)
            }

        var networkInfo: NetworkInfo?=null

        var internetAviableCallback = MutableLiveData(isInternetAviable)

        fun setInternetAviable() {
            try {
                isInternetAviable = networkInfo?.isConnectedOrConnecting!=null
            }catch (e: Exception) {

            }
        }

        private fun ensureNetworkAndGet(force: Boolean) {

            if (force || networkInfo==null) {
                try {
                    networkInfo = (appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
                }catch (e: java.lang.Exception) {

                }
            }
        }

        fun isConnectionSlow(): Boolean {
            try {
                ensureNetworkAndGet(false)
                if (networkInfo!=null&&networkInfo?.type == ConnectivityManager.TYPE_MOBILE) {
                    when (networkInfo?.subtype) {
                        TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_IDEN -> return true
                    }
                }
            } catch (ignore: Throwable) {
            }
            return false
        }

    }


    @SuppressLint("CommitPrefEdits")
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        sharedPreferences = appContext.getSharedPreferences("MyApplication",Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        val networkManager = getSystemService(Context.CONNECTIVITY_SERVICE)  as ConnectivityManager
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

        val broadCastReciever = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                try {
                    networkInfo = networkManager.activeNetworkInfo
                    setInternetAviable()
                }catch (e: Exception) {

                }
            }
        }
        registerReceiver(broadCastReciever,intentFilter)

        SharedConfig.getInstance().loadUserConfig()
        FilesController.getInstance().createMediaPaths()

        GlobalScope.launch {
            setUp(object :Result{
                override fun onSuccess(any: Any?) {

                }
            })
        }

        initImageKit()

        appContext.let {
            openSansBold = ResourcesCompat.getFont(it,R.font.open_sans_bold)!!
            openSansExtraBold = ResourcesCompat.getFont(it,R.font.open_sans_extra_bold)!!
            openSansLight = ResourcesCompat.getFont(it,R.font.open_sans_light)!!
            openSansSemiBold = ResourcesCompat.getFont(it,R.font.open_sans_semi_bold)!!
            openSansRegular = ResourcesCompat.getFont(it,R.font.open_sans_regular)!!

            robotoRegular = ResourcesCompat.getFont(it,R.font.roboto_regular)!!
            robotoMedium = ResourcesCompat.getFont(it,R.font.roboto_medium)!!
            robotoBold = ResourcesCompat.getFont(it,R.font.roboto_bold)!!

            productTitleColor = Color.rgb(72,72,72)
            productCostColor = Color.rgb(33,33,33)
            productDiscountColor = Color.rgb(170,170,170)
            productTopColor = Color.rgb(255,71,71)
            productDiscountPercentColor = Color.rgb(255,71,71)
            productShippingBackgroundColor = Color.rgb(244,244,244)
            productShippingColor = Color.rgb(130,130,130)
        }

            resources.apply {
                productTitleSize = getDimensionPixelSize(R.dimen.productTitleSize)
                productCostSize = getDimensionPixelSize(R.dimen.productCostSize)
                productDiscountSize = getDimensionPixelSize(R.dimen.productDiscountSize)
                productDiscountPercentSize = getDimensionPixelSize(R.dimen.productDiscountPercentSize)
                productShippingSize = getDimensionPixelSize(R.dimen.productShippingSize)
                productTopSize = getDimensionPixelSize(R.dimen.productTopSize)

                displayMetrics.apply {
                    displaySize = Pair(widthPixels,heightPixels)
                    homeProductWidth = (widthPixels - dpToPx(24f))/2
                }
            }

        titlePaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = productTitleSize.toFloat()
            color = productTitleColor
            typeface = openSansRegular
        }
        topPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = productTopSize.toFloat()
            color = productTopColor
            typeface = openSansRegular
        }
        productCostPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = productCostSize.toFloat()
            color = productCostColor
            typeface = robotoBold
        }
        productDiscountPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = productDiscountSize.toFloat()
            color = productDiscountColor
            typeface = openSansRegular
            flags = TextPaint.STRIKE_THRU_TEXT_FLAG
        }
        productDiscountPercentPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = productDiscountPercentSize.toFloat()
            color = productDiscountPercentColor
            typeface = openSansRegular
        }
        productShippingPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = productShippingSize.toFloat()
            color = productShippingColor
            typeface = openSansRegular
        }
    }
}