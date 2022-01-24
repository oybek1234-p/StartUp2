package com.example.market

import kotlin.collections.ArrayList

data class Details(var description: String = "",
                   var photos: ArrayList<String>?=null, var shipping: Shipping?=null, var specification: ArrayList<Specification>?=null)

 class Shipping {
     var id: Int = DOSTAVKA_ID
     var narxi: Int = 0
     var sellerProtection : Int = 0
     var service: String = ""

     constructor(id: Int, narxi: Int,sellerProtection:Int,service: String){
         this.id = id
         this.narxi = narxi
         this.sellerProtection = sellerProtection
         this.service = service
     }

     constructor()
 }