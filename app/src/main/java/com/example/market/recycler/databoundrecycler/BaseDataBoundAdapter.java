/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.market.recycler.databoundrecycler;

import android.view.ViewGroup;

import com.example.market.BR;
import com.example.market.recycler.RecyclerItemClickListener;
import com.example.market.utils.LogUtilsKt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A reference implementation for an adapter that wants to use data binding "the right way". It
 * works with {@link DataBoundViewHolder}.
 * <p>
 * It listens for layout invalidations and notifies RecyclerView about them before views refresh
 * themselves. It also avoids invalidating the full item when data in the bound item dispatches
 * proper notify events.
 * <p>
 * This class uses layout id as the item type.
 * <p>
 * It can be used for both single type lists and multiple type lists.
 *
 * @param <T> The type of the ViewDataBinding class. Can be ommitted in multiple-binding-type use
 *           case.
 */
abstract public class BaseDataBoundAdapter<T extends ViewDataBinding,M>
        extends RecyclerView.Adapter<DataBoundViewHolder<T>> {

    public boolean automaticallySetData = true;
    public BaseDataBoundAdapter() {
        setHasStableIds(true);
        LogUtilsKt.log("Set has stable ids");
    }

    public void setClickListener(RecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private RecyclerItemClickListener clickListener;
    @Nullable
    public RecyclerView mRecyclerView;

    public List<M> dataList;

    public void setDataList(@Nullable ArrayList<M> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }
    public void setDataList(@Nullable List<M> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        if (dataList!=null){
            return dataList.size();
        } else {
            return 0;
        }
    }

    @Override
    @CallSuper
    public DataBoundViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        DataBoundViewHolder<T> vh = DataBoundViewHolder.create(parent, viewType,clickListener);
        onCreateViewHolder(vh,viewType);
        return vh;
    }

    public abstract void onCreateViewHolder(DataBoundViewHolder<T> viewHolder,int viewType);

    protected abstract void bindItem(
            DataBoundViewHolder<T> holder,
            int position,
            M model
    );

    @Override
    public final void onBindViewHolder(DataBoundViewHolder<T> holder, int position) {
        M model =  dataList.get(position);
        if (model!=null) {
            bindItem(holder, position,model);

            //May throw exception
            //All variable ids should be named 'data'
            if (automaticallySetData) {
                try {
                    holder.binding.setVariable(BR.data,model);
                }catch (Exception e){
                    LogUtilsKt.log(e.getMessage());
                }

                holder.binding.executePendingBindings();
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull @NotNull DataBoundViewHolder<T> holder) {
        return true;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = null;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemLayoutId(position);
    }

    @LayoutRes
    abstract public int getItemLayoutId(int position);
}
