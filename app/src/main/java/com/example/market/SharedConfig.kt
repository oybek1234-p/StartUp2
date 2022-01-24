package com.example.market

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

const val USER_CONFIG = "userConfig"

class SharedConfig {
    companion object {
        private var INSTANCE: SharedConfig?=null

        fun getInstance(): SharedConfig{
            return if (INSTANCE!=null) INSTANCE!! else SharedConfig().also { INSTANCE = it }
        }
    }

    private fun userSharedPreference(): SharedPreferences = MyApplication.appContext.getSharedPreferences(USER_CONFIG,Context.MODE_PRIVATE)

    private var userConfigLoaded = false
    var userLogged = false
    var saveToGallery = true

    fun loadUserConfig() {
        if (userConfigLoaded) {
            return
        }
        try {
            userSharedPreference().apply {
                userLogged = getBoolean("userLogged",false)

                if (userLogged) {
                    if (currentUser==null) {
                        currentUser = User()
                    }

                    currentUser?.apply {
                        id = getString("id","").toString()
                        name = getString("name","").toString()
                        photo = getString("photo","").toString()
                        about = getString("about","").toString()
                        status = getBoolean("status",false)
                        phone = getString("phone","").toString()
                        seller = getBoolean("seller",false)
                        likes = getInt("likes",0)
                        subscribers = getInt("subsribers",0)
                        subscriptions = getInt("subscriptions",0)
                        email = getString("email","").toString()
                        password = getString("password","").toString()
                        messages = getInt("messages",0)
                        unreadMessages = getInt("unreadMessages",0)

                        try {
                            registeredDate = RegisteredDate().apply {
                                time = getInt("time",0)
                                month = getInt("month",0)
                                year = getInt("year",0)
                                day = getInt("day",0)
                            }

                            shippingLocation = ShippingLocation().apply {
                                this.id = getLong("shippingId",0L)
                                latLang = LatLng().apply {
                                    latitude = getString("latitude","").toString().toDouble()
                                    longtitude = getString("longtitude","").toString().toDouble()
                                }
                                adress = getString("adress","")
                                shippingType = getString("shippingType","").toString()
                                shippingCost = getString("shippingCost","").toString()
                            }
                        }catch (e: java.lang.Exception) {

                        }
                    }
                }
            }
            userConfigLoaded = true
        }catch (e: Exception) {

        }
    }

    private fun clearUserConfig() {
        try {
            userSharedPreference().edit().apply {
                    putString("id",null)
                    putString("name",null)
                    putString("photo",null)
                    putString("about",null)
                    putBoolean("status", false)
                    putString("phone",null)
                    putBoolean("seller", false)
                    putInt("likes",0)
                    putInt("subsribers",0)
                    putInt("subscriptions",0)
                    putString("email",null)
                    putString("password",null)

                    putBoolean("userLogged",false)

                commit()
            }
        }catch (e: Exception) {

        }
    }

    @SuppressLint("ApplySharedPref")
    fun saveUserConfig() {
        try {
            userSharedPreference().edit().apply {
                if (currentUser!=null) {
                    currentUser?.apply {
                        putString("id", id)
                        putString("name", name)
                        putString("photo", photo)
                        putString("about", about)
                        putBoolean("status", status)
                        putString("phone", phone)
                        putBoolean("seller", seller)
                        putInt("likes", likes)
                        putInt("subsribers", subscribers)
                        putInt("subscriptions", subscriptions)
                        putString("email", email)
                        putString("password", password)
                        putInt("messages",messages)
                        putInt("unreadMessages",unreadMessages)
                        try {

                            registeredDate?.apply {
                                putInt("time", time)
                                putInt("month", month)
                                putInt("year", year)
                                putInt("day", day)
                            }

                            shippingLocation?.apply {
                                putLong("shippingId", id)
                                latLang?.apply {
                                    putString("latitude", latitude.toString())
                                    putString("longtitude", longtitude.toString())
                                }
                                putString("adress", adress)
                                putString("shippingType", shippingType)
                                putString("shippingCost", shippingCost)
                            }
                        } catch (e: java.lang.Exception) {

                        }
                        putBoolean("userLogged", true)
                    }
                }else {
                    clearUserConfig()
                }
                commit()
            }
        }catch (e: Exception) {

        }
    }

    fun updateUserField(fieldName:String,value: Any) {
        sharedPreferences.edit().apply {
            when (value) {
                is String -> {
                    putString(fieldName,value)
                }
                is Boolean -> {
                    putBoolean(fieldName,value)
                }
                is Float -> {
                    putFloat(fieldName,value)
                }
                is Int -> {
                    putInt(fieldName,value)
                }
                is Long -> {
                    putLong(fieldName,value)
                }
            }
            apply()
        }
    }

}