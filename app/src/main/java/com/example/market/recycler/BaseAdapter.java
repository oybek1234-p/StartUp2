package com.example.market.recycler;

import android.view.ViewGroup;

import com.example.market.databinding.HomeProductItemBinding;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

abstract public class BaseAdapter<T extends ViewDataBinding,M>
        extends ListAdapter<M, BaseViewHolder<T>> {

    @Nullable
    public RecyclerView mRecyclerView;
    @Nullable
    public RecyclerItemClickListener clickListener;

    protected abstract void onCreateViewHolderCreated(BaseViewHolder<T> holder,int type);

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    protected BaseAdapter(@NonNull @NotNull DiffUtil.ItemCallback diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    public void setOnItemClickListener( @org.jetbrains.annotations.Nullable RecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NotNull
    @Override
    @CallSuper
    public BaseViewHolder<T> onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        BaseViewHolder<T> vh = BaseViewHolder.create(parent,viewType,clickListener);
        onCreateViewHolderCreated(vh,viewType);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseViewHolder<T> holder, int position) {
        try {
            bind(holder,position,getItem(position));
            holder.getBinding().executePendingBindings();
        }catch (NullPointerException nullPointerException) {

        }
    }

    protected abstract void bind(@NonNull @NotNull BaseViewHolder<T> holder, int position, M model);

    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull @NotNull BaseViewHolder<T> holder) {
        return true;
    }

    @Override
    public void onDetachedFromRecyclerView(@NotNull RecyclerView recyclerView) {
        mRecyclerView = null;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemLayoutId(position);
    }

    @LayoutRes
    abstract public int getItemLayoutId(int position);

}
