package com.org.market

import android.location.Location
import com.example.market.databinding.DostavkaLayoutBinding
import com.google.android.gms.maps.model.LatLng
import com.imagekit.android.util.SharedPrefUtil
import com.org.net.models.*
import com.org.ui.LaunchActivity
import com.org.ui.LocationFragment
import java.lang.Exception
import kotlin.math.cos

object ShippingController {

    var shippingLocation = ShippingLocation()
    var shippingGot = false

    fun isShippingEmpty() = shippingLocation.userId.isEmpty()

    val shippingSharedPrefs = getSharedPreference("shipping")
    private var deliverMinutePerKm = 5

    fun loadShipping() {
        if(!shippingGot) {
            shippingSharedPrefs.apply {
                shippingLocation.apply {
                    id = getLong(ID, 0L)
                    latLang = LatLng(getFloat("lat",0f).toDouble(),getFloat("lang",0f).toDouble())
                    address = getString(ADDRESS,"")!!
                    cost = getLong(COST,0L)
                    timeSpendMinute = getInt("time",0)
                    try {
                        type = ShippingTypes.valueOf(getString(TYPE,"")!!)
                    }catch (e: Exception) {

                    }
                }
            }
            shippingGot = true
        }
    }

    fun saveShipping(shippingLocation: ShippingLocation) {
        this.shippingLocation = shippingLocation
        saveShipping()
    }

    fun saveShipping() {
        shippingSharedPrefs.edit().apply{
            shippingLocation.apply {
                putLong(ID,id)
                putFloat("lat",latLang.latitude.toFloat())
                putFloat("lang",latLang.longitude.toFloat())
                putString(ADDRESS,address)
                putLong(COST, cost)
                putInt("time",timeSpendMinute)
                putString(TYPE,type.name)
            }
            apply()
        }
    }

    fun getDistance(
        start: LatLng,
        end: LatLng,
    ): Long {
        val results = FloatArray(4)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            results
        )
        return (results[0] / 1000f).toLong()
    }

    fun getCostForDistance(start: LatLng,end: LatLng,shippingType: ShippingTypes): Long {
        return (shippingType.cost * getDistance(start,end))
    }

    fun getDeliveryTime(distance: Long) : Int {
        return (distance * deliverMinutePerKm).toInt()
    }

    val list = arrayListOf(
        ShippingOffer().apply {
            lottieUrl = "https://assets5.lottiefiles.com/packages/lf20_d4Vtmp.json"
            type = ShippingTypes.Start
            cost = -1
        },
        ShippingOffer().apply {
            lottieUrl = "https://assets5.lottiefiles.com/datafiles/KJUNji2fNasvwId/data.json"
            type = ShippingTypes.Fast
            cost = -1
        },
        ShippingOffer().apply {
            lottieUrl = "https://assets10.lottiefiles.com/packages/lf20_viwsedx3.json"
            type = ShippingTypes.Pro
            cost = -1
        }
    )

    fun updateShippingLayout(binding: DostavkaLayoutBinding,withAnimation: Boolean) {
        binding.apply {
            val titleText: String
            val typeText: String
            val lottieUrl: String
            val addressText: String

            val empty = isShippingEmpty()
            if (!empty) {
                titleText = formatCurrency(shippingLocation.cost)
                typeText = shippingLocation.type.name
                lottieUrl = list.find { it.type == shippingLocation.type }!!.lottieUrl
                addressText = shippingLocation.address
            } else {
                titleText = "Press to choose a location"
                typeText = "Delivery type"
                lottieUrl = "https://assets7.lottiefiles.com/private_files/lf30_p576dm4g.json"
                addressText = "Shipping is secure!"
            }

            lottieAnimationView2.setAnimationFromUrl(lottieUrl)
            val setRunnable = Runnable {
                costView.text = titleText
                typeView.text = typeText
                addressView.text = addressText

                lottieAnimationView2.apply {
                    repeatCount = 1
                    playAnimation()
                }
            }

            if (withAnimation) {
                shimmerLayout.showShimmer(true)
                runOnUiThread({
                    shimmerLayout.hideShimmer()
                    setRunnable.run()
                },500)
            } else {
                shimmerLayout.hideShimmer()
                setRunnable.run()
            }
        }
    }

}