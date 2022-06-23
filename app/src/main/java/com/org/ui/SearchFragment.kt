package com.org.ui

import android.view.View
import com.example.market.R
import com.example.market.databinding.FragmentSearchBinding
import com.org.ui.actionbar.BaseFragment

/**
 * SearchText
 */
class SearchFragment: BaseFragment<FragmentSearchBinding>(R.layout.fragment_search) {
    override fun createActionBar(): View? {
        return null
    }
    override fun onCreateView(binding: FragmentSearchBinding) {
        binding.searchBar.isSearch = true
    }
}