package com.example.market

 class User {
     var id = ""
     var name  = ""
     var photo = ""
     var about = ""
     var status = true
     var phone = ""
     var seller = false
     var products = 0
     var likes = 0
     var gifts = 0
     var subscribers = 0
     var subscriptions = 0
     var messages = 0
     var unreadMessages = 0
     var email = ""
     var password = ""
     var registeredDate: RegisteredDate?=null
     var shippingLocation: ShippingLocation?=null
     var sellerShippingLocation: LatLng?=null
     var comments = 0

     constructor(
         id: String,
         name: String,
         photo: String,
         about: String,
         status: Boolean,
         phone: String,
         seller: Boolean,
         likes: Int,
         subscribers: Int,
         subscriptions: Int,
         email: String,
         password: String,
         registeredDate: RegisteredDate
     ){
         this.id = id
         this.name = name
         this.photo = photo
         this.about = about
         this.status = status
         this.phone = phone
         this.seller = seller
         this.likes = likes
         this.subscribers = subscribers
         this.subscriptions = subscriptions
         this.email = email
         this.password = password
         this.registeredDate = registeredDate
     }
     constructor()
 }
