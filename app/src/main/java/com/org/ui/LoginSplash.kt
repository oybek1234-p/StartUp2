package com.org.ui

import android.view.View
import com.example.market.R
import com.example.market.databinding.LoginSplashLayoutBinding
import com.org.ui.actionbar.BaseFragment

class LoginSplash : BaseFragment<LoginSplashLayoutBinding>(R.layout.login_splash_layout) {

    override fun createActionBar(): View? {
        return null
    }

    override fun onCreateView(binding: LoginSplashLayoutBinding) {
        binding.apply {
            backButton.setOnClickListener {
                closeLastFragment()
            }
            lottieView.setAnimationFromUrl("https://assets2.lottiefiles.com/packages/lf20_gcudkx1v.json")
            createProfileButton.setOnClickListener {
                presentFragment(RegisterFragment(false), false)
            }
            loginButton.setOnClickListener {
                presentFragment(LogInFragment(false), false)
            }
        }
    }
}