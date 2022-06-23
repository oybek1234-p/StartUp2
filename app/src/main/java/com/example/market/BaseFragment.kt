package com.example.market
//
//import android.app.Dialog
//import android.content.Context
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.databinding.ViewDataBinding
//import androidx.fragment.app.Fragment
//import com.example.market.binding.inflateBinding
//import com.example.market.navigation.FragmentController
//import com.example.market.theme.Theme
//import com.example.market.viewUtils.AlertDialogLayout
//import com.example.market.viewUtils.PopupDialog
//import com.org.ui.ActionBar.AlertDialog
//
//abstract class BaseFragment<T: ViewDataBinding>(private val layoutRes: Int) : Fragment(){
//    var baseFragmentLifecycle: BaseFragmentLifecyle?=null
//    var bundleData: Bundle? = null
//    var bundleAny: Any?=null
//    var fragmentController: FragmentController?=null
//    var isViewCreated = false
//    var isProgress = false
//    var id = "NO_ID"
//    var isEmpty = false
//
//
//    fun fragmentController() = fragmentController!!
//
//    private var mBinding: T?=null
//    val binding: T get() = mBinding!!
//
//    fun showBottomNav(show: Boolean) {
//        getMainActivity().showBottomNav(show)
//    }
//
//    fun applyTheme(themes: Theme.Themes) {
//        getMainActivity().applyTheme(themes)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        mBinding = inflateBinding(container,layoutRes,false)
//        isViewCreated = true
//        onCreateView(inflater, container, savedInstanceState, binding)
//        super.onCreateView(inflater, container, savedInstanceState)
//        return binding.root
//    }
//
//    abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,binding: T)
//
//    open fun onBeginSlide() {}
//    open fun isSwapBackEnabled() = true
//    open fun onConnectionChanged(state: Boolean) {}
//    open fun onBackPressed() {}
//
//    open fun onViewFullyVisible() {
//        getMainActivity().applyStatusBar()
//    }
//
//    open fun onViewFullyHiden() {}
//    open fun onViewAttachedToParent() {}
//    open fun onViewDetachedFromParent() {}
//
//    open fun onThemeChanged() {
//        binding.invalidateAll()
//    }
//
//    open fun canBeginSlide()  = true
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        baseFragmentLifecycle?.onViewCreated(view)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        mBinding = null
//        isViewCreated = false
//        fragmentController = null
//        bundleAny = null
//        bundleData = null
//        baseFragmentLifecycle = null
//    }
//
//    fun getMainActivity() : MainActivity {
//        return context as MainActivity
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        fragmentController = getMainActivity().fragmentController
//    }
//
//    fun closeLastFragment() {
//        try {
//            getMainActivity().onBackPressed()
//        }catch (e : Exception){
//
//        }
//    }
//
//    fun closePreviousFragment() {
//        try {
//            fragmentController?.closePreviousFragment()
//        }catch (e : Exception){
//
//        }
//    }
//
//    fun presentFragmentRemoveLast(
//        fragment: Fragment,
//        removeLast: Boolean
//    ) {
//        try {
//            dismissVisibleDialog()
//            fragmentController?.presentFragmentRemoveLast(
//                 fragment
//                ,FragmentController.openSearchFragment
//                ,removeLast
//            )
//        }catch (e: Exception){
//
//        }
//    }
//}
//interface BaseFragmentLifecyle {
//    fun onViewCreated(view: View)
//}
