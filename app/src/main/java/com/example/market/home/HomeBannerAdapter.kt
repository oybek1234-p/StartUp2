package com.example.market.home

import com.example.market.R
import com.example.market.databinding.HomeBannerItemBinding
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder

class HomeBannerAdapter: DataBoundAdapter<HomeBannerItemBinding,Banner>(R.layout.home_banner_item){
    override fun onCreateViewHolder(
        viewHolder: DataBoundViewHolder<HomeBannerItemBinding>?,
        viewType: Int,
    ) {

    }

    override fun bindItem(
        holder: DataBoundViewHolder<HomeBannerItemBinding>,
        binding: HomeBannerItemBinding,
        position: Int,
        model: Banner,
    ) {

    }
}
