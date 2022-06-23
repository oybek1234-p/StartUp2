package com.example.market.adapters
//
//import android.annotation.SuppressLint
//import android.graphics.drawable.Drawable
//import android.graphics.drawable.GradientDrawable
//import com.example.market.MyApplication
//import com.example.market.R
//import com.example.market.binding.load
//import com.example.market.databinding.PayCardLayoutMiniBinding
//import com.example.market.models.PaymentCard
//import com.example.market.models.getPayCardName
//import com.example.market.models.getPayCardPhotoUrl
//import com.example.market.payCards
//import com.example.market.recycler.databoundrecycler.DataBoundAdapter
//import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
//
//class PaymentSheetAdapter : DataBoundAdapter<PayCardLayoutMiniBinding, PaymentCard>(R.layout.pay_card_layout_mini) {
//    var currentSelectedItem = 0
//    var lastSelectedItem = 0
//
//    var selectedItemForeground: Drawable = GradientDrawable().apply {
//        setStroke(AndroidUtilities.dp(2f),MyApplication.appContext.getColor(R.color.red_color))
//        cornerRadius = AndroidUtilities.dp(12f).toFloat()
//    }
//
//    override fun getItemCount(): Int {
//        return payCards.size
//    }
//
//    override fun onCreateViewHolder(
//        viewHolder: DataBoundViewHolder<PayCardLayoutMiniBinding>,
//        viewType: Int,
//    ) {
//        viewHolder.apply {
//            itemView.setOnClickListener {
//                lastSelectedItem = currentSelectedItem
//                currentSelectedItem = layoutPosition
//
//                if (lastSelectedItem != currentSelectedItem) {
//                    getViewHolderAtPosition(lastSelectedItem)?.itemView?.foreground = null
//                    itemView.foreground = selectedItemForeground
//                }
//            }
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    override fun onBindViewHolder(
//        holder: DataBoundViewHolder<PayCardLayoutMiniBinding>,
//        position: Int
//    ) {
//        val selected = currentSelectedItem == position
//        holder.binding.apply {
//            root.foreground = if (selected) selectedItemForeground else null
//
//            payCards.get(position).apply {
//                photoView.load(getPayCardPhotoUrl(payCardName))
//                titleView.text = getPayCardName(payCardName)
//                val cardNumber = cardNumber.toString()
//                cardNumerView.text = "****${cardNumber.substring(cardNumber.length - 4,cardNumber.length)}"
//            }
//        }
//    }
//
//    override fun bindItem(
//        holder: DataBoundViewHolder<PayCardLayoutMiniBinding>,
//        binding: PayCardLayoutMiniBinding,
//        position: Int,
//        model: PaymentCard,
//    ) {
//        //Overridden
//    }
//
//}