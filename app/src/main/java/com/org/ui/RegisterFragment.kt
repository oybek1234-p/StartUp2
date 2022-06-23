package com.org.ui

import com.example.market.R
import com.example.market.databinding.FragmentRegistratsiyaBinding
import com.google.firebase.database.DataSnapshot
import com.org.market.*
import com.org.net.models.UserFull
import com.org.ui.actionbar.BaseFragment
import com.org.ui.components.AlertsCreator
import com.org.ui.components.visibleOrGone
import java.lang.Exception

class RegisterFragment(val fromLogin: Boolean) :
    BaseFragment<FragmentRegistratsiyaBinding>(R.layout.fragment_registratsiya) {
    var isLoading = false

    override fun onResume() {
        super.onResume()
    }

    override fun onFragmentCreate(): Boolean {
        requestAdjust(requireActivity(),RequestAdjustType.Pan)
        return super.onFragmentCreate()
    }

    override fun onCreateView(binding: FragmentRegistratsiyaBinding) {
        actionBar.apply { title = "Register" }

        binding.apply {
            logInView.setOnClickListener {
                if (checkIsNotLoading { openLoginFragment() }) {
                    openLoginFragment()
                }
            }
            createView.setOnClickListener { register() }
        }
    }

    fun openLoginFragment() {
        showProgress(false)
        if (fromLogin) {
            closeLastFragment()
        } else {
            presentFragment(LogInFragment(true), false)
        }
    }

    fun checkIsNotLoading(run: () -> Unit): Boolean {
        if (isLoading) {
            AlertsCreator.showAlert(
                requireActivity(),
                "Cancel register",
                "Do you really want to cancel loading",
                "CANCEL",
                "YES",
                null,
                {
                    run()
                })
            return false
        }
        return true
    }

    fun register() {
        requireBinding().apply {
            val name = nameView.text.toString()
            val password = passwordView.text.toString()
            val email = emailView.text.toString()

            if (checkTexts(name, password, email)) {
                showProgress(true)
                DataController.registerUser(email, password, callback)
            }
        }
    }

    fun showProgress(show: Boolean) {
        requireBinding().apply {
            isLoading = show
            createView.text = if (show) null else requireActivity().getText(R.string.ochish)
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

    override fun canBeginSlide(): Boolean {
        return checkIsNotLoading {
            closeLastFragment()
        }
    }

    private val callback = object : ResultCallback<UserFull>() {
        override fun onSuccess(data: UserFull?) {
            showProgress(false)
            if (fromLogin) {
                requireActionBarLayout().closePreviousFragment()
            }
            (requireActivity()).initUser()
            closeLastFragment()
        }

        override fun onFailed(exception: Exception?) {
            showProgress(false)
            if (exception != null) {
                toast(exception.message)
            } else {
                toast("Something went wrong")
            }
        }
    }

    override fun onRemoveFromParent() {
        super.onRemoveFromParent()
        DataController.cancelCallback(callback)
    }

    override fun onBackPressed(): Boolean {
        if (!checkIsNotLoading {
                if (fromLogin) {
                    requireActionBarLayout().closePreviousFragment()
                }
                DataController.cancelCallback(callback)
                closeLastFragment()
            }) {
            return false
        }
        return super.onBackPressed()
    }

    fun checkTexts(
        name: String,
        password: String,
        email: String,
    ): Boolean {
        val nameIsEmpty = name.isEmpty()
        val passwordIsEmpty = password.isEmpty()
        val emailIsEmpty = email.isEmpty()

        if (nameIsEmpty || passwordIsEmpty || emailIsEmpty) {
            if (nameIsEmpty) {
                toast("Please provide your name")
            }
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
}