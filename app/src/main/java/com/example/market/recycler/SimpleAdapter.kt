package com.example.market.recycler

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.example.market.R

abstract class SimpleAdapter<T : ViewDataBinding, M>(
    diffCallback: DiffUtil.ItemCallback<M>,
    @LayoutRes val layoutRes: Int,
) : BaseAdapter<T, M>(diffCallback) {
    var showProgress = false
        set(value) {
            if (value != field) {
                field = value
                notifyItemInserted(currentList.size)
            }
        }

    override fun getItemViewType(position: Int): Int {
        return if (isProgressType(position)) R.layout.simple_progress_layout else super.getItemViewType(position)
    }

    fun isProgressType(position: Int) = showProgress && position == currentList.size - 1

    override fun getItemLayoutId(position: Int): Int {
        return layoutRes
    }

    override fun getItemCount(): Int {
        val size = currentList.size
        return if (showProgress) size + 1 else size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        if (isProgressType(position)) {
            return
        }
        super.onBindViewHolder(holder, position)
    }
}
