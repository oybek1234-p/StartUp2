package com.example.market.recycler

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil

abstract class SimpleAdapter<T: ViewDataBinding,M>(diffcallback: DiffUtil.ItemCallback<M>,@LayoutRes val layoutRes: Int) : BaseAdapter<T,M>(diffcallback) {
    override fun getItemLayoutId(position: Int): Int {
        return layoutRes
    }
}
