package com.example.market.models
//
//import com.example.market.ShippingLocation
//
///**
// * New - Yangi zakaz ochildi, hali puli tolanmagan.
// * Cancelled - Zakaz otmen buldi, sotuvchi yoki oluvchi toxtatdi, agar product puli tolangan bulsa qaytarilib beriladi.
// * Waiting for payment - puli tolanishini kutilmoqda
// * Payment failed - Pul tolanmadi, problema chiqdi.
// * Payment back - Pul oluvchiga qaytarib berildi,agar zakaz tuxtatilgan bulsa
// * Payment verified - Pul tolandi.
// * Shipping - Zakaz yetkazib berilmoqda.
// * Shipping failed - Zakaz yetkzib berishda problema chiqdi.
// * Shipping completed and waiting- Zakaz yetkazildi va oluvchini kutmoqda.
// * Product got - Zakaz oluvchiga yetkzildi.
// * Closed - Zakaz yopildi.
// */
//enum class OrderState {
//    IN_CART,
//    NEW,
//    CANCELED,
//    WAITING_FOR_PAYMENT,
//    PAYMENT_FAILED,
//    PAYMENT_VERIFIED,
//    PAYMENT_BACK,
//    SHIPPING,
//    SHIPPING_FAILED,
//    SHIPPING_COMPLETED_AND_WAITING,
//    PRODUCT_GOT,
//    CLOSED
//}
//
//class Payment {
//    var id = ""
//    var amount = ""
//    var date = ""
//}
//
//class Order {
//    var id = ""
//    var date = System.currentTimeMillis()
//    var state = OrderState.NEW
//    var product: Product ?= null
//    var shippingLocation: ShippingLocation ?= null
//    var payment: Payment ?= null
//    var count = 1
//    var customerId = ""
//    var customerName = ""
//    var customerPhoto = ""
//    var sellerId = ""
//    var storeName = ""
//    var storePhoto = ""
//    var completedDate = 0L
//    var paymentDone = false
//}