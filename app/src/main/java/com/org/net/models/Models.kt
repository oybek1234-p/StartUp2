package com.org.net.models

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.NonNull
import com.example.market.R
import com.google.android.gms.maps.model.LatLng
import com.org.market.Currency
import com.org.market.currentTimeMillis
import com.org.market.newId
import com.org.ui.adapters.VIEW_TYPE_PRODUCT

enum class UserStatus {
    ONLINE,
    OFFLINE
}

class User {
    var id = ""
    var name = ""
    var photo = ""
    var phone = ""
    var likes = 0
    var products = 0
    var subscribers = 0
    var subscriptions = 0
    var lastSeenTime = 0L
    var status = 0
    var mobilePhone = 0L

    constructor(
        id: String, name: String, photo: String, phone: String,
        likes: Int,
        products: Int,
        subscribers: Int,
        subscriptions: Int,
        lastSeenTime: Long,
        status: Int,
        mobilePhone: Long,
    ) {
        this.id = id
        this.likes = likes
        this.name = name
        this.phone = phone
        this.photo = photo
        this.products = products
        this.subscribers = subscribers
        this.subscriptions = subscriptions
        this.lastSeenTime = lastSeenTime
        this.status = status
        this.mobilePhone = mobilePhone
    }

    constructor()
}

class MessagesCount {
    var messages = 0
    var unreadMessages = 0
}

class UserFull {
    var user = User()
    var email = ""
    var bio = ""
    var activeOrders = 0
    var ordersInCart = 0
    var messagesCount = MessagesCount()

    constructor(
        user: User,
        email: String,
        bio: String,
        activeOrders: Int,
        ordersInCart: Int,
        messagesCount: MessagesCount,
    ) {
        this.user = user
        this.email = email
        this.bio = bio
        this.activeOrders = activeOrders
        this.ordersInCart = ordersInCart
        this.messagesCount = messagesCount
    }

    constructor()

}

class Product {
    var id = ""
    var photo = ""
    var photoScaleRatio = 1f
    var title = ""
    var subtitle = ""
    var videoUrl = ""

    @NonNull
    var cost = 0L
    var currency = Currency.USZ
    var shippingCost = ""
    var sellerId = ""
    var sellerPhoto = ""
    var sellerName = ""
    var sellerLikes = 0
    var sellerProducts = 0
    var sellerMobilePhone = 0L
    var sellerSubscribersCount = 0
    var count = 0
    var category = Category()
    var discountPercent = 0f
    var commentsCount = 0
    var sold = 0
    var views = 0
    var shares = 0
    var likes = 0
    var hashtag: String? = null
    var uploadedDate = currentTimeMillis()
    var type = VIEW_TYPE_PRODUCT
}

class ProductLike {
    var id = newId()
    var productId = ""
    var productPhoto = ""
    var productPhotoScale = 0f
    var productTitle = ""
    var productCost = 0L
    var sellerId = ""
    var userId = ""
}

class SearchProduct {
    var id = System.currentTimeMillis().toString()
    var requestsCount = 0
    var text = ""
}

class Photo {
    var id = newId()
    var url = ""

    constructor(photo: String) {
        this.url = photo
    }

    constructor()
}

const val PHOTO_BANNER = 0

class Banner {
    var id = ""
    var photo = ""
    var name = ""
    var sellerId = ""
    var sellerName = ""
    var type = PHOTO_BANNER
    var date = currentTimeMillis()
    var viewsCount = 0
}

class Category {
    var id = ""
    var name = ""
    var photo = ""
    var parentId = ""
    var productsCount = 0
    var options = listOf(ProductOption.BRAND)
}

enum class ProductOption {
    COLOR,
    BRAND,
    SIZE,
    DATE
}

class Specification {
    var id = currentTimeMillis().toString()
    var name: String = ""
    var value: String = ""

    constructor()

    constructor(id: String, name: String, value: String) {
        this.id = id
        this.name = name
        this.value = value
    }
}

class Header {
    var id = ""
    var photo = ""
    var title = ""
    var subtitle = ""
    var actionButtonRes: Drawable? = null
    var actionText: String? = null
    var onActionClick: ((view: View) -> Unit)? = null
    var onClick: ((view: View) -> Unit)? = null
}

data class Empty(
    val title: String = "",
    val subtitle: String = "",
    val buttonText: String = "",
    var buttonClickAction: View.OnClickListener? = null,
    val lottieUrl: String = "",
    val lottieRes: Int = -1,
    val id: Long = System.currentTimeMillis(),
)

class PayCard {
    var id = ""
    var holderName = ""
    var number = 0L
    var expiryDate: Pair<Int, Int> = Pair(0, 0)
    var cvv = 0
}

enum class ShippingTypes(val cost: Int) {
    Start(1700),
    Fast(2500),
    Pro(3200)
}

class ShippingLocation {
    var id = System.currentTimeMillis()
    var userId = ""
    var latLang: LatLng = LatLng(0.0, 0.0)
    var address = ""
    var cost = 0L
    var timeSpendMinute = 0
    var type = ShippingTypes.Start
}

class ShippingOffer {
    var id = newId()
    var lottieUrl = ""
    var type = ShippingTypes.Start
    var cost = 0L
    var timeSpendMinute = 0
}

const val MESSAGE_TYPE_ALL = 0
const val MESSAGE_TYPE_MESSAGE = 1
const val MESSAGE_TYPE_SUBSCRIBED = 2
const val MESSAGE_TYPE_PRODUCT_LIKED = 3
const val MESSAGE_TYPE_COMMENTED = 4

class Message {
    var id = newId()
    var message = ""
    var photo = ""
    var productId = ""
    var senderPhoto = ""
    var senderId = ""
    var senderName = ""
    var sendDate = currentTimeMillis()
    var type = MESSAGE_TYPE_MESSAGE
    var receiverId = ""

    constructor(
        id: String = "",
        message: String = "",
        photo: String = "",
        productId: String = "",
        senderPhoto: String = "",
        senderId: String = "",
        senderName: String = "",
        sendDate: Long = currentTimeMillis(),
        type: Int = 0,
        receiverID: String = "",
    ) {
        this.id = id
        this.message = message
        this.photo = photo
        this.productId = productId
        this.senderPhoto = senderPhoto
        this.senderId = senderId
        this.senderName = senderName
        this.sendDate = sendDate
        this.type = type
        this.receiverId = receiverID
    }

    constructor()
}

class Subscriber {
    var id = ""

    constructor(id: String) {
        this.id = id
    }

    constructor()
}


const val DATE_NEW = 0
const val DATE_TODAY = 1
const val DATE_YESTERDAY = 2
const val DATE_THIS_WEEK = 3
const val DATE_THIS_MONTH = 4
const val DATE_THIS_YEAR = 5
const val DATE_NOW = 6

const val MILLISECOND = 0
const val SECOND = 1
const val MINUTE = 2
const val HOUR = 3
const val DAY = 4
const val WEEK = 5
const val MONTH = 6
const val YEAR = 7
