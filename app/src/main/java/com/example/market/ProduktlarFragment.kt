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

class ProduktlarFragment(var hasBottomNav: Boolean = true) : BaseFragment<FragmentProduktlarBinding>(R.layout.fragment_produktlar) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: FragmentProduktlarBinding,
    ) {
        if (hasBottomNav) {
            binding.actionBar.backButton.visibility = View.GONE
        }
    }
}