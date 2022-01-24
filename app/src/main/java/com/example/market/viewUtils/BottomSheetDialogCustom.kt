package com.example.market.viewUtils
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.example.market.binding.inflateBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

//IGNORE CLASS
abstract class BottomSheetDialogCustom <T: ViewDataBinding> (context: Context, @LayoutRes val layId: Int) : BottomSheetDialog(context) {
    var binding: T?=null
    var viewCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateView()
    }

    fun onCreateView(){
        window?.decorView?.let { it ->
            binding = inflateBinding(it as ViewGroup,layId)

            binding?.let {
                viewCreated = true
                setContentView(it.root)
                onCreateView(it)
            }
        }
    }

    abstract fun onCreateView(binding: T)

    fun onDestroyView(){
        binding = null
        viewCreated = false
    }
}