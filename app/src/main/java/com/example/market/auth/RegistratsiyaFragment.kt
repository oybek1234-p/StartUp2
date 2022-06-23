package com.example.market.auth

//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import androidx.lifecycle.lifecycleScope
//import com.example.market.*
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.FragmentRegistratsiyaBinding
//import com.example.market.navigation.bottomNavVisiblity
//import com.example.market.viewUtils.checkIsEmptyForParent
//import com.example.market.viewUtils.presentFragmentRemoveLast
//import com.example.market.viewUtils.toast
//import com.google.android.material.snackbar.Snackbar
//import kotlinx.coroutines.launch

//class RegistratsiyaFragment : BaseFragment<FragmentRegistratsiyaBinding>(R.layout.fragment_registratsiya) {
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentRegistratsiyaBinding
//    ) {
//        binding.apply {
//            backButton.setOnClickListener { closeLastFragment() }
//            accountBormiButton.setOnClickListener {
//                presentFragmentRemoveLast(LoginFragment(),true)
//            }
//
//            createAccountButton.setOnClickListener {
//                if (checkIsEmptyForParent(root as ViewGroup)) {
//                    val name = ismKiritish.text!!.toString()
//                    val email = emailKiritish.text!!.toString()
//                    val password = passwordKiritish.text!!.toString()
//
//                    lifecycleScope.launch {
//                        registerUser(name,email,password,object : Result {
//                            override fun onSuccess(any: Any?) {
//                                getMainActivity().updateBottomNav()
//                                closeLastFragment()
//                                bottomNavVisiblity(context,true)
//                            }
//                        })
//                    }
//                }
//
//            }
//        }
//    }
//}