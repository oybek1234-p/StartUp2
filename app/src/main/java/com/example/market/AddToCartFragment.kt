package com.example.market
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.market.auth.LoginFragment
import com.example.market.binding.increaseOrderCount
import com.example.market.binding.inflateBinding
import com.example.market.databinding.FragmentAddToCartBinding
import com.example.market.location.LocationActivity
import com.example.market.models.Order
import com.example.market.models.OrderState
import com.example.market.models.Product
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.presentFragmentRemoveLast
import com.example.market.viewUtils.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddToCartFragment(val product: Product, var isLocationGet: Boolean, var cart: Boolean): BottomSheetDialogFragment() {
    private var binding: FragmentAddToCartBinding?=null
    private var order = Order()

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
            (this as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
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
            executePendingBindings()
            closeView.setOnClickListener {
                dismiss()
            }
            buyView.text = if (cart) "Add to cart" else "Buy now"
            decreaseCountView.setOnClickListener {
                increaseOrderCount(order,false,countView)
            }
            increaseCountView.setOnClickListener {
                increaseOrderCount(order,true,countView)
            }
            dostavkaLayoutMaterial.root.setOnClickListener {
               openLocationFragment()
            }

            buyView.setOnClickListener {
                if (currentUser?.shippingLocation == null) {
                    openLocationFragment()
                    return@setOnClickListener
                }
                if (checkCurrentUser()) {
                    order.apply {
                        id = System.currentTimeMillis().toString()
                        state = if (cart) OrderState.IN_CART else OrderState.NEW
                        payment = null
                        currentUser?.let {
                            customerId = it.id
                            customerName = it.name
                            customerPhoto = it.photo
                        }
                        product = this@AddToCartFragment.product
                    }
                    addOrder(order,object : Result {
                        override fun onSuccess(any: Any?) {
                            toast(if (cart) "Successfully added to cart" else "Next step!")
                            dismiss()
                        }

                    })
                } else {
                    dialog?.window?.decorView?.visibility = View.GONE
                    if (parentFragment is DetailsFragment) {
                        toast("Details fragment")
                    }
                    presentFragmentRemoveLast(requireContext(),LoginFragment(),false)
                }
            }
        }
        return binding?.root
    }

    private fun openLocationFragment() {
        dialog?.window?.decorView?.visibility = View.GONE
        presentFragmentRemoveLast(requireContext(),LocationActivity
        {
            isLocationGet = true
            dialog?.window?.decorView?.visibility = View.VISIBLE
            binding?.dostavkaLayoutMaterial?.invalidateAll()
        },false)
    }
}