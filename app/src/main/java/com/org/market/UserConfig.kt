package com.org.market

import com.org.net.models.*

const val USER_CONFIG = "userConfig"
fun userSharedPreferences() = getSharedPreference(USER_CONFIG)

fun hasUser() = UserConfig.hasUser()
fun currentUser() = UserConfig.user()
fun currentUserFull() = UserConfig.userFull
fun currentUserId() = currentUser().id
fun messagesCount() = currentUserFull().messagesCount
fun saveUserConfig() = UserConfig.saveConfig()

class UserConfig {
    companion object {
        var userFull = UserFull()

        fun hasUser() = userFull.user.id.isNotEmpty()
        fun user() = userFull.user

        fun checkCurrentUser(user: User) = user().id == user.id

        fun loadConfig() {
            userFull.apply {
                userSharedPreferences().apply {
                    user.apply {
                        id = getString(ID,"")!!

                        if (id.isNotEmpty()) {
                            name = getString(NAME,"")!!
                            photo = getString(PHOTO,"")!!
                            phone = getString(PHONE,"")!!
                            likes = getInt(LIKES,0)
                            products = getInt(PRODUCTS,0)
                            subscribers = getInt(SUBSCRIBERS,0)
                            subscriptions = getInt(SUBSCRIPTIONS,0)
                            lastSeenTime = getLong(LAST_SEEN_TIME,0L)
                            email = getString(EMAIL,"")!!
                            bio = getString(BIO,"")!!
                            activeOrders = getInt(ACTIVE_ORDERS,0)
                            ordersInCart = getInt(ORDERS_IN_CART,0)
                            messagesCount.apply {
                                messages = getInt(MESSAGES,0)
                                unreadMessages = getInt(UNREAD_MESSAGES,0)
                            }
                        }
                    }
                }
            }
        }

        fun saveConfig() {
            userSharedPreferences().edit().apply {
                userFull.apply {
                    user.apply {
                        putString(ID,id)
                        putString(NAME,name)
                        putString(PHONE,phone)
                        putInt(LIKES,likes)
                        putInt(PRODUCTS,products)
                        putInt(SUBSCRIBERS,subscribers)
                        putInt(SUBSCRIPTIONS,subscriptions)
                        putLong(LAST_SEEN_TIME,lastSeenTime)
                        putString(EMAIL,email)
                        putString(BIO,bio)
                        putInt(ACTIVE_ORDERS,activeOrders)
                        putInt(ORDERS_IN_CART,ordersInCart)
                        putInt(MESSAGES,messagesCount.messages)
                        putInt(UNREAD_MESSAGES,messagesCount.unreadMessages)
                    }
                }
                apply()
            }
        }

        fun clearConfig() {
            userFull.apply {
                user.apply {
                    id = ""
                    name = ""
                    photo = ""
                    phone = ""
                    likes = 0
                    products = 0
                    subscribers = 0
                    subscriptions = 0
                    lastSeenTime = 0L
                    status = 0
                    email = ""
                    bio = ""
                    activeOrders = 0
                    ordersInCart = 0
                    messagesCount.apply {
                        messages = 0
                        unreadMessages = 0
                    }
                }
            }
            saveConfig()
        }
    }
}