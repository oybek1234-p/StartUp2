package com.example.market.home
//
//import android.util.Log
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.market.models.Product
//import kotlinx.coroutines.*
//
//class HomeViewModel() : ViewModel() {
//    val mutableLiveData: MutableLiveData<ArrayList<Product>> = MutableLiveData()
//
//
//    val mainCategoryLiveData: MutableLiveData<ArrayList<MainCategory>> = MutableLiveData()
//    var listOfProducts = ArrayList<Product>()
//
//    init {
//        Log.i("HomeViewModel","Created")
//        loadMore()
//        loadListData()
//    }
//
//    private fun loadListData(){
//        viewModelScope.launch {
//            loadMainCategory(mainCategoryLiveData)
//        }
//    }
//
//    fun loadMore() {
//        loadProducts(mutableLiveData,listOfProducts,viewModelScope)
//    }
//}
//
//interface ModelResponse<T> {
//    fun onSucces(result: T)
//    fun onFailed(message: String?=null)
//}