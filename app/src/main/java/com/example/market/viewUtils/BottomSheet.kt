package com.example.market.viewUtils
//
//import android.content.Context
//import android.os.Bundle
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ProgressBar
//import androidx.annotation.LayoutRes
//import androidx.databinding.ViewDataBinding
//import com.example.market.BaseFragment
//import com.example.market.R
//import com.example.market.binding.inflateBinding
//import com.example.market.binding.visibleOrGone
//import com.example.market.databinding.BottomSheetContainerBinding
//import com.example.market.databinding.EmptyScreenBinding
//import com.google.android.material.bottomsheet.BottomSheetDialog
//
//open class BottomSheet(context: Context) : BottomSheetDialog(context,R.style.MyBottomSheetDialogTheme) {
//
//    private var containerBinding: BottomSheetContainerBinding?=null
//    private var progressBar: ProgressBar? = null
//    private var view: View?=null
//
//    fun containerBinding() = containerBinding!!
//    fun containerLayout() = containerBinding().root as ViewGroup
//
//    var isLoading = false
//    set(value) {
//        if (field!=value) {
//            field = value
//            progressBar?.visibleOrGone(value)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        containerBinding = inflateBinding<BottomSheetContainerBinding>(null,R.layout.bottom_sheet_container).apply {
//            super.setContentView(root)
//
//            ProgressBar(context).apply {
//                progressBar = this
//                findViewById<ViewGroup>(R.id.design_bottom_sheet)
//                    .addView(this,
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//                )
//                visibleOrGone(false)
//            }
//        }
//    }
//
//    override fun setContentView(view: View) {
//        setContentView(view,null)
//    }
//
//    override fun setContentView(layoutResId: Int) {
//        layoutInflater.inflate(layoutResId,containerLayout()).also {
//            view = it
//            setContentView(it)
//        }
//    }
//
//    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
//        containerLayout().addView(view.also { this.view = it },params)
//
//    }
//}
//
//open class BottomSheetB<T: ViewDataBinding>(context: Context,@LayoutRes val layRes: Int) : BottomSheetDialog(context,R.style.MyBottomSheetDialogTheme) {
//    var containerBinding: BottomSheetContainerBinding = inflateBinding<BottomSheetContainerBinding>(null,R.layout.bottom_sheet_container).apply {
//        exitButton.setOnClickListener {
//            dismiss()
//        }
//    }
//
//    fun show(fragment: BaseFragment<*>) {
//        fragment.showDialog(this)
//    }
//
//    var viewBinding: T = inflateBinding<T>(null,layRes).also {
//        (containerBinding.root as ViewGroup).addView(it.root,ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))
//    }
//
//    var title = "Title"
//    set(value) {
//        field = value
//        containerBinding.titleView.text = value
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(containerBinding.root)
//    }
//
//}