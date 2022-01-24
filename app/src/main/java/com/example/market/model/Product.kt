package com.example.market.model

import com.example.market.categories.Category
import com.example.market.colors.productMoreColors
import com.example.market.home.Banner
import com.example.market.home.MainCategory
import com.example.market.home.VIEW_TYPE_MORE
import com.example.market.home.VIEW_TYPE_PRODUCT
import com.google.firebase.firestore.Blob

class Product {
     var id = ""
     var sellerId = ""
     var photo = ""
     var title = ""
     var narxi = ""
     var currency = "0"
     var soni = "1"
     var kategoriya = ""
     var scaleRatio: Float = 0.0F
     var discount = ""
     var dostavka = ""
     var top: String = ""
     var likesCount: Int = 0
     var commentCount = 0
     var soldCount = 0
     var viewsCount = 0
     var date = ""
     var timestamp = 0L
     var thumbnailObject: Blob?=null
    /**
     * Banner list
     */
    var bannerList :ArrayList<Banner>?=null

    /**
     * Category list
     */
    var categoryList: ArrayList<Category>?=null

    /**
     * Main category list
     */
    var mainCategoryList: ArrayList<MainCategory>?=null

    /**
      * set product more colors
      */
     var type:Int = VIEW_TYPE_PRODUCT
     set(value) {
         field = value
         if (type == VIEW_TYPE_MORE) {

             if (moreColors==null){
                 moreColors = productMoreColors[
                         (productMoreColors.indices).random()
                 ]
             }
         }
     }

     var moreColors: Array<Int>? = null

     override fun equals(other: Any?): Boolean {
         return this === other
     }
 }