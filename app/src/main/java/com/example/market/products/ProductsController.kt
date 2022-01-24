package com.example.market.products

import com.example.market.getProductsReference
import com.example.market.model.Product
import com.example.market.parseDocumentSnapshot
import com.google.firebase.firestore.Query
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProductsController {
    companion object {
        private var INSTANCE: ProductsController?=null
        fun getInstance():ProductsController{
            return INSTANCE ?: ProductsController().also { INSTANCE = it }
        }
        const val loadForPaging = 0
        const val loadForProductMore = 1
        const val PAGING_LIMIT = 6L
    }

    var listOfProducts: HashMap<String,ArrayList<Product>> = HashMap()

    /**
     * id is NOTIFICATION ID
     */
    fun loadProducts(id: String,type: Int,category: String?=null,random: Boolean=false,direction:Query.Direction = Query.Direction.DESCENDING){
        var query = getProductsReference().limit(PAGING_LIMIT)

        if (!random) {
            query = query.orderBy("id",direction)
        }

        when(type) {
            loadForPaging -> {
                checkLastDocumentAviable(id)?.let {
                    query = query.startAfter(it.id)
                }
            }
            loadForProductMore -> {
                query = query.whereEqualTo("kategoriya",category)
            }
        }

        query.get().addOnCompleteListener { it ->
            if (it.isSuccessful) {
                checklistNotNull(id)
                val list = parseDocumentSnapshot(it.result.documents,Product::class.java)
                listOfProducts[id]?.addAll(list)?.also {
                    NotificationCenter.getInstance().didRecieveNotification(id,it)
                }
            }
        }
    }

    private fun checklistNotNull(id: String) {
        val list = listOfProducts[id]

        if (list==null) {
            listOfProducts[id] = ArrayList()
        }
    }
    private fun checkLastDocumentAviable(id: String): Product? {
        return listOfProducts[id]?.lastOrNull()
    }
}

  class NotificationCenter {
    companion object {
        private var INSTANCE: NotificationCenter?=null
        fun getInstance():NotificationCenter{
            return INSTANCE ?: NotificationCenter().also { INSTANCE = it }
        }
    }

      private var observers = ArrayList<Notification>()

      fun didRecieveNotification(id: String,any: Any?) {
          notifyNotifications(id, any)
      }

      private fun notifyNotifications(id: String, any: Any?) {
          observers.forEach {
              if (it.id==id) {
                  it.notidDelegate.didRecieveNotification(id, any)
              }
          }
      }
      fun observe(id: String,delegate:NotificationDelegate){
          Notification(id,delegate).let {
              observers.add(it)
          }
      }

     interface NotificationDelegate {
         fun didRecieveNotification(id: String,any: Any?)
     }
      data class Notification(val id: String,val notidDelegate: NotificationDelegate)
}