package com.org.ui.components

import com.example.market.databinding.SellerInfoLayoutMaterialBinding
import com.org.market.findActivity
import com.org.net.models.Product
import com.org.ui.LocationFragment
import com.org.ui.ProfileFragment
import com.org.ui.actionbar.Theme
import com.org.ui.actionbar.presentFragment

class SellerInfoLayoutProvider(val binding: SellerInfoLayoutMaterialBinding) {
    var product: Product?=null

    init {
        binding.apply {
            dostavkaLayout.setOnClickListener {
                root.context.presentFragment(LocationFragment {
                },false)
            }
            productsImageView.setOnClickListener {
                if (product!=null) {
                    root.context.presentFragment(ProfileFragment(product!!.sellerId),false)
                }
            }
            photoView.setOnClickListener {
                if (product!=null) {
                    root.context.presentFragment(ProfileFragment(product!!.sellerId),false)
                }
            }
            
        }
    }
}