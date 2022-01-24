package com.example.market.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return try {
            modelClass.newInstance()
        }catch (e: Exception){
            throw Throwable("Can not create now view model instance for type ${modelClass.name}")
        }
    }

}