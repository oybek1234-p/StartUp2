package com.example.market.bottomsheets
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.os.Bundle
//
//import com.example.market.R
//import com.example.market.ResultCallback
//import com.example.market.adapters.PaymentSheetAdapter
//import com.example.market.binding.visibleOrGone
//import com.example.market.databinding.PaymentSheetLayoutBinding
//import com.example.market.fragments.PaymentFragment
//import com.example.market.getPaymentCards
//import com.example.market.models.PaymentCard
//import com.example.market.payCards
//import com.example.market.viewUtils.BottomSheetB
//import com.example.market.viewUtils.presentFragmentRemoveLast
//
//class PaymentBottomSheet(context: Context,var onContinue: (payCard: PaymentCard)-> Unit) : BottomSheetB<PaymentSheetLayoutBinding>(context,R.layout.payment_sheet_layout) {
//    private var listAdapter: PaymentSheetAdapter = PaymentSheetAdapter()
//
//    fun loadPayCards() {
//        viewBinding.progressBar.visibleOrGone(true)
//
//        getPaymentCards(object : ResultCallback<ArrayList<PaymentCard>>{
//            @SuppressLint("NotifyDataSetChanged")
//            override fun onSuccess(result: ArrayList<PaymentCard>?) {
//                viewBinding.progressBar.visibleOrGone(false)
//                if (result?.isEmpty() == true) {
//                    dismiss()
//                    presentFragmentRemoveLast(viewBinding.root.context,PaymentFragment(),false)
//                }
//                listAdapter.notifyDataSetChanged()
//            }
//        })
//    }
//
//    private fun openPaymentsFragment() {
//        presentFragmentRemoveLast(viewBinding.root.context,PaymentFragment(),false)
//        dismiss()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        title = "Pay cards"
//
//        viewBinding.apply {
//            addCardImageView.setOnClickListener { openPaymentsFragment() }
//            addCardTextView.setOnClickListener { openPaymentsFragment() }
//
//            continueButton.setOnClickListener {
//                payCards.getOrNull(listAdapter.currentSelectedItem)?.let {
//                    onContinue(it)
//                }
//            }
//
//            recyclerView.adapter = listAdapter
//            loadPayCards()
//        }
//    }
//
//}