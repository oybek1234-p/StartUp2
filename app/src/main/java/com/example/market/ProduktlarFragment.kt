package com.example.market
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.market.binding.inflateBinding
import com.example.market.databinding.FragmentProduktlarBinding
import com.example.market.navigation.FragmentController
import com.example.market.viewUtils.presentFragmentRemoveLast

class ProduktlarFragment(var hasBottomNav: Boolean = true) : BaseFragment() {

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
    private var binding: FragmentProduktlarBinding?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = inflateBinding(container,R.layout.fragment_produktlar)
        binding?.apply {
            if (hasBottomNav) {
                actionBar.backButton.visibility = View.GONE
            }

        }
        return binding?.root
    }
}