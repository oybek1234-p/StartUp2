@file:Suppress("ControlFlowWithEmptyBody", "ControlFlowWithEmptyBody", "ControlFlowWithEmptyBody")

package com.example.market.home

import androidx.recyclerview.widget.*
import com.example.market.models.Product

object ProductDiff : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
}