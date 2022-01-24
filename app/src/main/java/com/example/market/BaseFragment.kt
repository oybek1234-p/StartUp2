package com.example.market

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.market.navigation.FragmentController
import com.example.market.viewUtils.toast
import java.util.*

abstract class BaseFragment : Fragment() {
    var baseFragmentLifecyle: BaseFragmentLifecyle?=null
    var bundleData: Bundle? = null
    var bundleAny: Any?=null
    var fragmentController: FragmentController?=null
    var visibleDialog: Dialog?=null
    var id = "NOID"
    open fun onBeginSlide() {}
    open fun isSwapBackEnabled() = true
    open fun onConnectionChanged(state: Boolean) {}
    open fun onBackPressed() {}
    open fun onViewFullyVisible() {}
    open fun onViewFullyHiden() {}
    open fun onViewAttachedToParent() {}
    open fun onViewDetachedFromParent() {}
    open fun canBeginSlide()  = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseFragmentLifecyle?.onViewCreated(view)
        toast("UUID ${UUID.randomUUID().toString()}")
    }

    fun getMainActivity() : MainActivity {
        return context as MainActivity
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentController = getMainActivity().fragmentController
    }

    fun dissmissVisibleDialog(dialog: Dialog?=null) {
        visibleDialog?.dismiss()
        visibleDialog = dialog
    }

    fun closeLastFragment() {
        try {
            getMainActivity().onBackPressed()
        }catch (e : Exception){

        }
    }
    fun closePreviousFragment() {
        try {
            fragmentController?.closePreviousFragment()
        }catch (e : Exception){

        }
    }

    fun presentFragmentRemoveLast(
        fragment: BaseFragment?,
        removeLast: Boolean
    ) {
        try {
            dissmissVisibleDialog()
            fragmentController?.presentFragmentRemoveLast(
                 fragment
                ,FragmentController.openSearchFragment
                ,removeLast
            )
        }catch (e: Exception){

        }
    }
}
interface BaseFragmentLifecyle {
    fun onViewCreated(view: View)
}
