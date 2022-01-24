package com.example.market.home

import com.example.market.MainActivity
import com.example.market.R
import com.example.market.SellerShopFragment
import com.example.market.categories.CategoriesFragment
import com.example.market.databinding.HomeMainCategoryItemBinding
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
import com.example.market.viewUtils.presentFragmentRemoveLast
import java.util.ArrayList

class HomeMainCategoryListAdapter : DataBoundAdapter<HomeMainCategoryItemBinding, MainCategory>(R.layout.home_main_category_item){

    override fun onCreateViewHolder(
        viewHolder: DataBoundViewHolder<HomeMainCategoryItemBinding>?,
        viewType: Int,
    ) {
        viewHolder?.binding?.root?.setOnClickListener {
            if (viewHolder.adapterPosition ==dataList.size-1) {
                presentFragmentRemoveLast(
                    viewHolder.itemView.context,
                    CategoriesFragment(),
                    false
                )
            } else {

                presentFragmentRemoveLast(
                    viewHolder.itemView.context,
                    SellerShopFragment(),
                    false,
                )
            }
        }
    }

    val category = MainCategory().apply {
        title = "See all categories"
        photo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Feather-arrows-arrow-right.svg/1200px-Feather-arrows-arrow-right.svg.png"
    }

    override fun setDataList(dataList: ArrayList<MainCategory>?) {
        dataList?.let {
            it.add(category)
            super.setDataList(it)
        }
    }


    override fun bindItem(
        holder: DataBoundViewHolder<HomeMainCategoryItemBinding>?,
        position: Int,
        model: MainCategory?,
    ) {
        holder?.binding?.apply {

        }
    }

}
