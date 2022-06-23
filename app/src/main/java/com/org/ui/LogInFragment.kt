package com.org.ui

import com.example.market.R
import com.example.market.databinding.LoginFragmentBinding
import com.org.market.*
import com.org.net.models.UserFull
import com.org.ui.actionbar.BaseFragment
import com.org.ui.components.AlertsCreator
import com.org.ui.components.visibleOrGone
import java.lang.Exception

class LogInFragment(val fromRegister: Boolean) :
    BaseFragment<LoginFragmentBinding>(R.layout.login_fragment) {
    private var isLoading = false

    override fun onFragmentCreate(): Boolean {
        requestAdjust(requireActivity(),RequestAdjustType.Pan)
        return super.onFragmentCreate()
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(binding: LoginFragmentBinding) {
        actionBar.apply { title = "Login" }

        binding.apply {
            signUpView.setOnClickListener {
                if (checkIsNotLoading { openSignUpFragment() }){
                    openSignUpFragment()
                }
            }
            logInView.setOnClickListener { login() }
        }
    }

    override fun onRemoveFromParent() {
        super.onRemoveFromParent()
        DataController.cancelCallback(callback)
    }

    private var callback = object : ResultCallback<UserFull>() {
        override fun onFailed(exception: Exception?) {
            showProgress(false)
            if (exception != null) {
                toast(exception.message)
            } else {
                toast("Something went wrong")
            }
        }

        override fun onSuccess(data: UserFull?) {
            showProgress(false)
            if (fromRegister) {
                requireActionBarLayout().closePreviousFragment()
            }
            (requireActivity()).initUser()
            closeLastFragment()
        }
    }

    fun checkIsNotLoading(run: () -> Unit): Boolean {
        if (isLoading) {
            AlertsCreator.showAlert(
                requireActivity(),
                "Cancel login",
                "Do you really want to cancel loading",
                "CANCEL",
                "YES",
                null,
                {
                    run.invoke()
                })
            return false
        }
        return true
    }

    override fun onBackPressed(): Boolean {
        val isLoading = checkIsNotLoading {
            if (fromRegister) {
                requireActionBarLayout().closePreviousFragment()
            }
            closeLastFragment()
        }
        if (!isLoading) {
            return false
        }
        return super.onBackPressed()
    }

    fun openSignUpFragment() {
        showProgress(false)
        if (fromRegister) {
            closeLastFragment()
        } else {
            presentFragment(RegisterFragment(true), false)
        }
    }

    override fun canBeginSlide(): Boolean {
        return checkIsNotLoading {
            closeLastFragment()
        }
    }

    fun login() {
        requireBinding().apply {
            val email = emailView.text.toString()
            val password = passwordView.text.toString()
            if (checkText(email, password)) {
                showProgress(true)
                DataController.loginUser(email, password, callback)
            }
        }
    }

    fun checkText(
        email: String,
        password: String,
    ): Boolean {
        val passwordIsEmpty = password.isEmpty()
        val emailIsEmpty = email.isEmpty()

        if (passwordIsEmpty || emailIsEmpty) {
            if (passwordIsEmpty) {
                toast("You must provide password")
            }
            if (emailIsEmpty) {
                toast("Type your email")
            }
            return false
        }
        return true
    }

    fun showProgress(show: Boolean) {
        isLoading = show
        requireBinding().apply {
            logInView.text = if (show) null else requireActivity().getText(R.string.login)
            progressBar.apply {
                if (show) {
                    scaleX = 0.8f
                    scaleY = 0.8f
                    alpha = 0f
                }
                animate()
                    .alpha(if (show) 1f else 0f)
                    .scaleX(if (show) 1f else 0.8f)
                    .scaleY(if (show) 1f else 0.8f)
                    .setDuration(200)
                    .start()
            }
        }
    }

}