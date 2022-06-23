package com.org.ui.actionbar

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.ActionBar.log
import com.org.market.findActivity
import com.org.market.toast
import com.org.ui.LaunchActivity
import com.org.ui.LoginSplash
import com.org.ui.components.appInflater
import com.org.ui.components.inflateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment<T : ViewDataBinding>(private val layoutRes: Int):LifecycleOwner {

    var finishing = false
    var isFinished = false

    var visibleDialog: Dialog? = null
    var popUpDialog: PopupDialog? = null

    var classGuid = 0
    var arguments: Bundle? = null

    var lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    var lightStatusBarEnabled = false

    private var parentLayout: ActionBarLayout? = null

    var showBottomNav = false

    fun requireActivity() = findActivity(appInflater.context)!! as LaunchActivity
    fun requireActionBarLayout() = parentLayout!!

    var mActionBar: ActionBar? = null
    var customActionBar: View?=null
    fun getActionBarView() = mActionBar?.view() ?: customActionBar

    val actionBar get() = mActionBar!!
    fun parentLayout() = parentLayout!!

    var hasOwnBackground = false
    var mBinding: T? = null

    fun openLogin() {
        presentFragment(LoginSplash(),false)
    }

    fun fragmentView() = mBinding?.root
    fun requireBinding() = mBinding!!

    var isPaused = true
    var parentDialog: Dialog? = null
    var isTransitionAnimation = false
    var fragmentBeginToShow = false

    fun context() = requireActivity() as Context

    open fun onConfigurationChanged(newConfig: Configuration?) {}

    open fun onFragmentCreate(): Boolean {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        return true
    }

    init {
        classGuid = System.currentTimeMillis().toInt()
        onFragmentCreate()
    }

    fun showBottomNav(show: Boolean) {
        requireActivity().showBottomNav(show)
    }

    fun applyTheme(themeId:Int) {
        (requireActivity() as LaunchActivity).applyTheme(themeId)
    }

    fun closeLastFragment() {
        requireActionBarLayout().closeLastFragment(true)
    }

    open fun invalidateViewBindings() {
        mActionBar?.mActionBarBinding?.invalidateAll()
        mBinding?.invalidateAll()
    }

    fun createView(): View {
        inflateBinding<T>(null, layoutRes).apply {
            mBinding = this
            onCreateView(this)
            return root
        }
    }

    abstract fun onCreateView(binding: T)

    fun setParentLayout(actionBarLayout: ActionBarLayout?) {
        if (parentLayout != actionBarLayout) {
            parentLayout = actionBarLayout

            ActionBarLayout.removeFragmentViewFromParent(
                this,
                false
            )

            ActionBarLayout.removeActionBarViewFromParent(this,false)
            
            if (
                parentLayout != null &&
                mActionBar == null
            ) {
                createActionBar()?.apply {
                    if (tag != ActionBar.TAG) {
                        customActionBar = this
                        tag = ActionBar.TAG
                    }
                }
            }
        }
    }

    open fun createActionBar(): View? {
        return ActionBar().apply {
            mActionBar = this
            parentFragment = this@BaseFragment
            title = "Actionbar Title"
        }.view()
    }

    fun clearViews() {
        ActionBarLayout.apply {
            removeFragmentViewFromParent(this@BaseFragment, true)
            mBinding = null
            removeActionBarViewFromParent(this@BaseFragment, true)
            mActionBar = null
        }
        parentLayout = null
    }

    open fun onResume() {
        isPaused = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        requireActivity().setLightStatusBar(lightStatusBarEnabled)
        if (!showBottomNav) {
            showBottomNav(false)
        }

    }

    open fun onPause() {
        isPaused = true
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        try {
            dismissAllDialogs()
            mActionBar?.onPause()
        } catch (e: java.lang.Exception) {
            log(e)
        }
    }

    open fun onFragmentDestroy() {
        isFinished = true
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    open fun onRemoveFromParent() {}

    fun showDialog(dialog: Dialog): Dialog {
        dismissVisibleDialog()

        dialog.apply {
            visibleDialog = dialog
            setCanceledOnTouchOutside(true)
            setOnDismissListener {
                onDialogDismiss(this)
            }
            show()
        }
        return dialog
    }

    fun dismissPopupDialog() {
        popUpDialog?.apply {
            dismiss()
            popUpDialog = null
        }
    }

    fun dismissAllDialogs() {
        dismissVisibleAlertDialog()
        dismissVisibleDialog()
        dismissPopupDialog()
    }

    fun finishFragment() {
        parentDialog?.apply {
            dismiss()
            return
        }
        finishFragment(true)
    }

    fun finishFragment(animated: Boolean) {
        if (isFinished) {
            return
        }
        parentLayout?.apply {
            finishing = true
            closeLastFragment(animated)
        }
    }

    fun removeSelfFromStack() {
        if (isFinished) return
        parentDialog?.apply {
            dismiss()
            return
        }
        parentLayout?.removeFragmentFromStackInternal(this)
    }

    fun isLastFragment() = false

    fun getLayoutContainer(): FrameLayout? {
        return fragmentView()?.parent?.let {
            if (it is FrameLayout) it else null
        }
    }

    open fun allowPresentFragment() = true

    open fun onBackPressed(): Boolean {
        requireActionBarLayout().closeLastFragment(true)
        return true
    }

    fun presentFragment(fragment: BaseFragment<*>, removeLast: Boolean) {
        presentFragment(fragment, removeLast, true)
    }

    fun presentFragment(
        fragment: BaseFragment<*>,
        removeLast: Boolean,
        animated: Boolean,
    ) {
        parentLayout?.presentFragment(fragment, removeLast, animated)
    }

    open fun prepareFragmentToSlide(topFragment: Boolean, beginSlide: Boolean) {
        //prepare
    }

    open fun canBeginSlide() = true

    open fun onBeginSlide() {
        try {
            dismissAllDialogs()
        } catch (e: Exception) {
            log(e)
        }
    }

    open fun onSlideProgress(isOpen: Boolean, progress: Float) {

    }

    open fun onTransitionAnimationProgress(isOpen: Boolean, progress: Float) {

    }

    open fun onTransitionAnimationStart(isOpen: Boolean, backward: Boolean) {
        isTransitionAnimation = true
        if (isOpen) {
            fragmentBeginToShow = true
        }
    }

    open fun onTransitionAnimationEnd(isOpen: Boolean, backward: Boolean) {
        isTransitionAnimation = false
    }

    open fun onBecomeFullyVisible() {
        if (showBottomNav) {
            showBottomNav(true)
        }
    }

    open fun onBecomeFullyHidden() {}

    open fun isSwapBackEnabled(ev: MotionEvent) = true

    open fun onConnectionChanged(state: Boolean) {}

    open fun onThemeChanged() {

    }

    open fun onDialogDismiss(dialog: Dialog?) {

    }

    fun dismissVisibleDialog() {
        visibleDialog?.apply {
            dismiss()
            visibleDialog = null
        }
    }

    fun dismissVisibleAlertDialog() {

    }
}

