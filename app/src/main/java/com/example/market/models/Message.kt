package com.example.market.models
//import com.example.market.User
//import com.example.market.utils.getCurrentDateTimestamp
//import com.example.market.utils.getCurrentTime
//
//const val MESSAGE_TYPE_MESSAGE = 0
//const val MESSAGE_TYPE_NEW_ORDER_TO_SELLER = 5
//const val MESSAGE_TYPE_NEW_ORDER_ADDED_TO_CART_TO_SELLER = 6
//const val MESSAGE_TYPE_LIKE = 1
//const val MESSAGE_TYPE_SUBSCRIBE = 2
//const val MESSAGE_TYPE_PRODUCT_NEW_COMMENT = 3
//const val MESSAGE_TYPE_SUBSCRIPTION = 4
//
//class Message {
//    var id = ""
//    var type = MESSAGE_TYPE_MESSAGE
//    var message: String?=null
//    var recieverId = ""
//    var sendDate: java.util.Date?= getCurrentTime()
//    var user: User?=null
//    var product: Product?=null
//    var read: Boolean = false
//    var date = getCurrentDateTimestamp()
//
//    constructor(id: String,type: Int,message: String,user: User,product: Product,recieverId: String) {
//        this.id = id
//        this.type = type
//        this.message = message
//        this.user = user
//        this.product = product
//        this.recieverId = recieverId
//    }
//    constructor()
//}