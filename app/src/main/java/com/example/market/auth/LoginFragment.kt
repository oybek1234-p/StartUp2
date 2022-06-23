package com.example.market.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.market.*
import com.example.market.databinding.FragmentKirishBinding

import kotlinx.coroutines.launch

//class LoginFragment : BaseFragment<FragmentKirishBinding>(R.layout.fragment_kirish) {
//    private var skipVisible = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentKirishBinding,
//    ) {
//        binding.apply {
//            bottomNavVisiblity(context,false)
//
//            backButton.setOnClickListener { closeLastFragment() }
//            continueButton.apply {
//                if (skipVisible) { visibility = View.VISIBLE }
//                setOnClickListener { closeLastFragment() }
//            }
//            accountYoqmiButton.setOnClickListener { presentFragmentRemoveLast(RegistratsiyaFragment(), true) }
//
//            continueButton.setOnClickListener {
//                if (checkIsEmptyForParent(binding.root as ViewGroup)) {
//                    val email = emailKiritish.text
//                    val password = passwordKiritish.text
//                    if (!email.isNullOrEmpty()&&!password.isNullOrEmpty()) {
//                        lifecycleScope.launch {
//                            loginUser(email.toString(),password.toString(),object : Result {
//                                override fun onFailed() {
//
//                                }
//
//                                override fun onSuccess(any: Any?) {
//                                    getMainActivity().updateBottomNav()
//                                    closeLastFragment()
//                                }
//                            })
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}