package com.example.market.createseller
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.market.*
import com.example.market.auth.LoginFragment
import com.example.market.binding.inflateBinding
import com.example.market.databinding.CreateSellerAccount1Binding
import com.example.market.location.LocationActivity
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.profile.ProfileFragmentSeller
import com.example.market.viewUtils.*
import kotlinx.coroutines.launch

class CreateSellerAccount1 : BaseFragment() {

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

    private fun setAdress() {
//        (bundleAny!! as ShippingLocation).let {
//            sellerAccount.shippingLocation = it
//            if(it.adress!!.isNotEmpty()) {
//                binding?.adressTextView?.text = getString(R.string.adres) + it.adress!!
//            } else {
//                binding?.adressTextView?.text = getString(R.string.magazin_adresini_kiritish)
//            }
//        }
    }

    override fun onViewAttachedToParent() {
//        bundleAny?.let {
//            if (it is ShippingLocation) {
//                setAdress()
//            }
//        }
    }

    override fun onViewDetachedFromParent() {

    }

    override fun canBeginSlide(): Boolean {
      return true
    }

    private var binding: CreateSellerAccount1Binding?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        binding = inflateBinding(container,R.layout.create_seller_account_1)

        bottomNavVisiblity(context,false)
        binding?.apply {
            actionBar.apply {
                backButton.setOnClickListener {
                    closeLastFragment()
                    toast("Back pressed")
                }
                title.text = getString(R.string.create_store)
            }

            adressContainer.setOnClickListener {
                presentFragmentRemoveLast(LocationActivity(),false)
            }
            continueButton.apply {
                setOnClickListener {
                    if (checkIsEmptyForParent(binding?.root as ViewGroup)) {
                        currentUser?.apply {

                            lifecycleScope.launch {
                                createSellerAccount(object:Result{
                                    override fun onSuccess(any: Any?) {
                                        phone = numberEdtxt.text.toString()
                                        seller = true
                                        SharedConfig.getInstance().saveUserConfig()
                                        closeLastFragment()
                                        fragmentController?.changeUserBottomNav(USER_SELLER)
                                        bottomNavVisiblity(context,true)
                                    }
                                    override fun onFailed() {
                                        super.onFailed()
                                        presentFragmentRemoveLast(LoginFragment(),false)
                                    }
                                                                                     },this@apply)
                            }
                        }
                    }
                }
            }
        }
        return binding?.root
    }
}