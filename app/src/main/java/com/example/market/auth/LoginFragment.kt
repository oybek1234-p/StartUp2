package com.example.market.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.market.*
import com.example.market.binding.inflateBinding
import com.example.market.databinding.FragmentKirishBinding
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.profile.ProfileFragmentSeller
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.checkIsEmptyForParent
import com.example.market.viewUtils.presentFragmentRemoveLast
import com.example.market.viewUtils.toast
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment() {
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
        return true
    }
    private var binding: FragmentKirishBinding?=null
    private var skipVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = inflateBinding(container,R.layout.fragment_kirish)

        binding?.apply {
            bottomNavVisiblity(context,false)


            backButton.setOnClickListener {
                closeLastFragment()
            }
            continueButton.apply {
                if (skipVisible) {
                    visibility = View.VISIBLE
                }
                setOnClickListener {
                    closeLastFragment()
                }
            }
            accountYoqmiButton.setOnClickListener {
                presentFragmentRemoveLast(
                    RegistratsiyaFragment(),
                    true
                )
            }

            continueButton.setOnClickListener {
                if (checkIsEmptyForParent(binding!!.root as ViewGroup)) {
                    val email = emailKiritish.text
                    val password = passwordKiritish.text
                    if (!email.isNullOrEmpty()&&!password.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            loginUser(email.toString(),password.toString(),object : Result {
                                override fun onFailed() {

                                }

                                override fun onSuccess(any: Any?) {
                                    getMainActivity().updateBottomNav()
                                    closeLastFragment()
                                }
                            })
                        }
                    }
                }
            }
        }
        return binding?.root
    }

}