package com.example.market.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.market.BaseFragment
import com.example.market.R

class FavouriteFragment : BaseFragment() {
    override fun onBeginSlide() {

    }

    override fun isSwapBackEnabled(): Boolean {
       return false
    }

    override fun onConnectionChanged(state: Boolean) {

    }

    override fun onBackPressed() {
    }

    override fun onViewFullyVisible() {
    }

    override fun onViewFullyHiden() {
    }

    override fun onViewAttachedToParent() {
    }

    override fun onViewDetachedFromParent() {
    }

    override fun canBeginSlide(): Boolean {
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite, container, false)
    }

}