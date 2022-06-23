package com.example.market.createseller
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.lifecycle.lifecycleScope
//import com.example.market.*
//import com.example.market.auth.LoginFragment
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.CreateSellerAccount1Binding
//import com.example.market.location.LocationActivity
//import com.example.market.navigation.bottomNavVisiblity
//import com.example.market.profile.ProfileFragmentSeller
//import com.example.market.viewUtils.*
//import kotlinx.coroutines.launch
//
//class CreateSellerAccount1 : BaseFragment<CreateSellerAccount1Binding>(R.layout.create_seller_account_1) {
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: CreateSellerAccount1Binding
//    ) {
//        bottomNavVisiblity(context,false)
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                    toast("Back pressed")
//                }
//                title.text = getString(R.string.create_store)
//            }
//
//            adressContainer.setOnClickListener {
//            }
//            continueButton.apply {
//                setOnClickListener {
//                    if (checkIsEmptyForParent(binding.root as ViewGroup)) {
//                        currentUser?.apply {
//
//                            lifecycleScope.launch {
//                                createSellerAccount(object:Result{
//                                    override fun onSuccess(any: Any?) {
//                                        phone = numberEdtxt.text.toString()
//                                        seller = true
//                                        SharedConfig.getInstance().saveUserConfig()
//                                        closeLastFragment()
//                                        fragmentController?.changeUserBottomNav(USER_SELLER)
//                                        bottomNavVisiblity(context,true)
//                                    }
//                                    override fun onFailed() {
//                                        super.onFailed()
//                                        presentFragmentRemoveLast(LoginFragment(),false)
//                                    }
//                                },this@apply)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}