package com.example.market.recycler

import androidx.databinding.ViewDataBinding
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.market.binding.inflateBinding

class BaseViewHolder<T : ViewDataBinding>(
    val binding: T,
    clickListener: RecyclerItemClickListener?
) : RecyclerView.ViewHolder(
    binding.root) {

    companion object {
        @JvmStatic
        fun <T : ViewDataBinding> create(
            parent: ViewGroup,
            @LayoutRes layoutId: Int,
            clickListener: RecyclerItemClickListener?

        ): BaseViewHolder<T> {
            val binding: T = inflateBinding(parent,layoutId)

            return BaseViewHolder(binding,clickListener)
        }
    }

    init {
        if (clickListener != null) {
            binding.root.setOnClickListener {
                clickListener.onClick(adapterPosition, itemViewType)
            }
        }
    }
}