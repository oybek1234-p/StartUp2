package com.org.ui.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class RecyclerListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {
    private var emptyView: View? = null
    var emptyViewAnimationType = 0

    fun emptyViewIsVisible() = emptyView?.isVisible

    fun setEmptyView(view: View) {
        if (emptyView == view) {
            return
        }
        emptyView = view
        checkIsEmpty(false)
    }

    var observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkIsEmpty(true)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkIsEmpty(true)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkIsEmpty(true)
        }
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
    }

    private fun checkIsEmpty(animated: Boolean) {
        if (emptyView == null) return
        val isEmptyVisible = isAdapterEmpty()
        if (emptyViewIsVisible() == isEmptyVisible) {
            return
        }
        if (animated) {
            emptyView?.apply {
                if (isEmptyVisible) {
                    animate().setListener(null).cancel()
                    if (visibility == View.GONE) {
                        visibility = View.VISIBLE
                        alpha = 0f
                        if (emptyViewAnimationType == 1) {
                            scaleY = 0.7f
                            scaleX = 0.7f
                        }
                    }
                    animate().alpha(1f).scaleY(1f).scaleX(1f).setDuration(150).start()
                } else {
                    if (visibility == View.VISIBLE) {
                        animate().alpha(0f).scaleX(0.7f).scaleY(0.7f).setDuration(150)
                            .setListener(object :
                                AnimatorListenerAdapter() {

                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    if (emptyView != null) {
                                        visibility = View.GONE
                                    }
                                }
                            })
                    }
                }
            }
        }
    }

    fun isAdapterEmpty() = adapter?.itemCount == 0

    open class DiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return false
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            try {
                return oldItem == newItem
            } catch (e: Exception) {

            }
            return false
        }
    }

    open class Adapter<T : ViewDataBinding, M>(
        @LayoutRes val layoutId: Int,
        diffCallback: DiffUtil.ItemCallback<M>,
    ) : ListAdapter<M, BaseViewHolder<T>>(diffCallback) {

        var mRecyclerView: RecyclerView? = null
        var clickListener: RecyclerItemClickListener? = null

        open fun onViewHolderCreated(holder: BaseViewHolder<T>, type: Int) {}

        override fun getItemId(position: Int): Long {
            return getItem(position).hashCode().toLong()
        }

        fun setOnItemClickListener(clickListener: RecyclerItemClickListener?) {
            this.clickListener = clickListener
        }

        @CallSuper
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
            val vh: BaseViewHolder<T> = BaseViewHolder.create(parent, viewType, clickListener)
            onViewHolderCreated(vh, viewType)
            return vh
        }

        var autoSetData = true

        override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
            val model = currentList[position]
            bind(holder, position, model)
            holder.binding.apply {
                if (autoSetData) {
                    try {
                        setVariable(BR.data, model)
                    } catch (e: Exception) {
                        throw e
                    }
                }
                executePendingBindings()
            }
        }

        open fun bind(holder: BaseViewHolder<T>, position: Int, model: M) {}

        @LayoutRes
        open fun getItemLayoutId(position: Int, model: M): Int = layoutId

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            recyclerView.apply {
                mRecyclerView = this
                setHasFixedSize(true)
            }
            //    setHasStableIds(true)
        }

        override fun onFailedToRecycleView(holder: BaseViewHolder<T>): Boolean {
            return true
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            mRecyclerView = null
        }

        override fun getItemViewType(position: Int): Int {
            return getItemLayoutId(position, currentList[position])
        }
    }

    open class SimpleAdapter<T : ViewDataBinding, M>(
        @LayoutRes val layoutId: Int,
    ) : RecyclerView.Adapter<BaseViewHolder<T>>() {

        var mRecyclerView: RecyclerView? = null
        var clickListener: RecyclerItemClickListener? = null
        var currentList = ArrayList<M>()

        @SuppressLint("NotifyDataSetChanged")
        fun setDataList(list: ArrayList<M>) {
            currentList = list
            notifyDataSetChanged()
        }

        open fun onViewHolderCreated(holder: BaseViewHolder<T>, type: Int) {}

        override fun getItemCount(): Int {
            return currentList.size
        }

        fun setOnItemClickListener(clickListener: RecyclerItemClickListener?) {
            this.clickListener = clickListener
        }

        @CallSuper
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
            val vh: BaseViewHolder<T> = BaseViewHolder.create(parent, viewType, clickListener)
            onViewHolderCreated(vh, viewType)
            return vh
        }

        var autoSetData = true

        override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
            val model = currentList[position]
            bind(holder, position, model)
            holder.binding.apply {
                if (autoSetData) {
                    try {
                        setVariable(BR.data, model)
                    } catch (e: Exception) {
                        throw e
                    }
                }
                executePendingBindings()
            }
        }

        open fun bind(holder: BaseViewHolder<T>, position: Int, model: M) {}

        @LayoutRes
        open fun getItemLayoutId(position: Int, model: M): Int = layoutId

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            recyclerView.apply {
                mRecyclerView = this
                setHasFixedSize(true)
            }
        }

        fun getViewHolder(position: Int): BaseViewHolder<T>? {
            return mRecyclerView?.findViewHolderForLayoutPosition(position) as BaseViewHolder<T>?
        }

        fun getViewHolder(item: M): BaseViewHolder<T>? {
            try {
                val index = currentList.indexOf(item)
                return getViewHolder(index)
            } catch (e: java.lang.Exception) {

            }
            return null
        }

        override fun onFailedToRecycleView(holder: BaseViewHolder<T>): Boolean {
            return true
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            mRecyclerView = null
        }

        override fun getItemViewType(position: Int): Int {
            return getItemLayoutId(position, currentList[position])
        }
    }

    open class BaseViewHolder<T : ViewDataBinding>(
        val binding: T,
        clickListener: RecyclerItemClickListener?,
    ) : RecyclerView.ViewHolder(
        binding.root) {

        fun setFullSpan(fullSpan: Boolean) {
            itemView.layoutParams
                .apply {
                    if (this is StaggeredGridLayoutManager.LayoutParams) {
                        isFullSpan = fullSpan
                    }
                }
        }

        companion object {
            @JvmStatic
            fun <T : ViewDataBinding> create(
                parent: ViewGroup,
                @LayoutRes layoutId: Int,
                clickListener: RecyclerItemClickListener?,

                ): BaseViewHolder<T> {
                val binding: T = inflateBinding(parent, layoutId)

                return BaseViewHolder(binding, clickListener)
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

    interface RecyclerItemClickListener {
        fun onClick(position: Int, viewType: Int)
    }

}