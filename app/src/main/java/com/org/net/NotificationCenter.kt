package com.org.net

import android.util.SparseArray

interface NotificationCenterDelegate {
    fun didReceiveNotification(id: Int,vararg objects: Any){ }
}

const val userInfoDidLoad = 0
const val newMessageDidLoad = 2
const val newProductsDidLoad = 3
const val productPhotosDidLoad = 4
const val locationChanged = 5

private var observers = SparseArray<ArrayList<NotificationCenterDelegate>>()

fun addObserver(observer: NotificationCenterDelegate,id: Int) {
    var listOfObservers = observers[id]
    if (listOfObservers == null) {
        listOfObservers = ArrayList<NotificationCenterDelegate>().also {
            observers.put(id,it)
        }
    }
    if (listOfObservers.contains(observer)) {
        return
    }
    listOfObservers.add(observer)
}

fun removeObserver(observer: NotificationCenterDelegate, id: Int) {
    observers.get(id)?.remove(observer)
}

fun postNotificationName(id: Int,vararg objects: Any) {
    observers.get(id)?.forEach {
        it.didReceiveNotification(id,objects)
    }
}

fun hasObservers(id: Int): Boolean {
    return observers.indexOfKey(id) >= 0
}
