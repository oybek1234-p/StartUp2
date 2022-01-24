package com.example.market.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.market.BaseFragment
import com.example.market.R
import com.example.market.auth.LoginFragment
import com.example.market.binding.inflateBinding
import com.example.market.createseller.CreateSellerAccount1
import com.example.market.currentUser
import com.example.market.databinding.FragmentProfileKlientBinding
import com.example.market.viewUtils.presentFragmentRemoveLast
import com.example.market.viewUtils.toast

class ProfileFragmentClient : BaseFragment() {
    private var binding: FragmentProfileKlientBinding?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = inflateBinding(container,R.layout.fragment_profile_klient)
        binding?.apply {
            currentUserBoolean = currentUser !=null
            kirishButton.setOnClickListener {
                presentFragmentRemoveLast(LoginFragment(),false)
            }
            sotuvchiBulish.setOnClickListener {
                presentFragmentRemoveLast(requireContext(),CreateSellerAccount1(),false)
            }
            executePendingBindings()
        }
        return binding?.root
    }
    override fun onBeginSlide() {

    }

    override fun isSwapBackEnabled(): Boolean {
       return true
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
}