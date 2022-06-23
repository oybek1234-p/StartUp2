package com.org.ui

import android.view.View
import com.example.market.R
import com.org.ui.actionbar.BaseFragment

class CreateProductFragment : BaseFragment<com.example.market.databinding.CreateProductFragmentBinding>(
    R.layout.create_product_fragment) {

    override fun createActionBar(): View? {
        return null
    }

    override fun onResume() {
        super.onResume()
        lightStatusBarEnabled = false
    }

    override fun onCreateView(binding: com.example.market.databinding.CreateProductFragmentBinding) {
        binding.apply {
            backButton.setOnClickListener {
                closeLastFragment()
            }
            progressBar.apply {
                setOnClickListener{
                    setProgress(progress+15,true)
                }
            }
        }
    }
}