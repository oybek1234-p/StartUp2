package com.example.market
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.market.binding.inflateBinding
import com.example.market.databinding.FragmentAddToCartBinding
import com.example.market.location.LocationActivity
import com.example.market.model.Product
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.presentFragmentRemoveLast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddToCartFragment(val product: Product): BottomSheetDialogFragment() {
    private var binding: FragmentAddToCartBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MyBottomSheetDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            requireView().apply {
                val lParams = layoutParams as ViewGroup.LayoutParams
                lParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                lParams.height = AndroidUtilities.dp(550f)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflateBinding(container,R.layout.fragment_add_to_cart)

        binding?.apply {
            data = product

            dostavkaLayout.root.setOnClickListener {
                presentFragmentRemoveLast(requireContext(),LocationActivity { dostavkaLayout.executePendingBindings() },false)
            }

            closeView.setOnClickListener {
                dismiss()
            }

        }
        return binding?.root
    }
}