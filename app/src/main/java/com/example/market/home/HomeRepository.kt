package com.example.market.home

import android.os.AsyncTask
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.example.market.model.Product
import com.google.android.gms.tasks.Continuation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList

    var lastDoc: String?=null
    val BACKGROUND_EXECUTOR: Executor = AsyncTask.THREAD_POOL_EXECUTOR

    fun loadBannerList(liveData: MutableLiveData<ArrayList<Banner>>){
//        FirebaseFirestore.getInstance().collection("bannerImages").get().addOnCompleteListener { it ->
//            if (it.isSuccessful) {
//                val list = ArrayList<Banner>()
//                it.result?.documents?.forEach {
//                    list.add(it!!.toObject(Banner::class.java)!!)
//                }
//                liveData.postValue(list)
//            }
//        }
    }


     fun loadMainCategory(liveData: MutableLiveData<ArrayList<MainCategory>>) {
        FirebaseFirestore.getInstance().collection("mainCategories").get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val list = ArrayList<MainCategory>()
                    it.result?.documents?.forEach {
                        val category = it.toObject(MainCategory::class.java)!!
                        list.add(category)
                    }
                    liveData.postValue(list)
                }
            }
    }

     fun loadProducts(
         liveData: MutableLiveData<ArrayList<Product>>,
         list: ArrayList<Product>,
         viewModelScope: CoroutineScope,
         mLastdoc : String?=null,
         whereEqualTo: Pair<String,String>?=null
     ) {
         val l = mLastdoc ?: lastDoc

            var reference = FirebaseFirestore
                .getInstance()
                .collection("products")
                .orderBy("id",Query.Direction.DESCENDING)

         l?.let {
             if (it.isNotEmpty()){
                 reference = reference.startAfter(it)
             }
         }

         whereEqualTo?.let {
             reference = reference.whereEqualTo(it.first,it.second)
         }

         reference = reference.limit(12)

         reference.get().continueWith(BACKGROUND_EXECUTOR,
             {
                 it.addOnCompleteListener { a ->
                     viewModelScope.launch(Dispatchers.IO) {
                         if (a.isSuccessful && a.result != null) {
                             val documents = a.result!!.documents

                                for (i in documents) {
                                    i.toObject(Product::class.java)?.let {
                                        list.add(it)
                                    }
                                }
                                if (list.size > 0) {
                                    lastDoc = list[list.size - 1].id

                                    liveData.postValue(list)
                                }
                            }
                        }
                    }
                })
        }
@Keep
class MainCategory {
    var photo : String = ""
    var title: String = ""
}
