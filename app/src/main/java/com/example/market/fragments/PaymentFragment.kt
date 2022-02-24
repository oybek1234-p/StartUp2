package com.example.market.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.market.*
import com.example.market.binding.parseCardNumber
import com.example.market.binding.visibleOrGone
import com.example.market.databinding.PayCardLayoutBinding
import com.example.market.databinding.PaymentFragmentBinding
import com.example.market.models.PaymentCard
import com.example.market.recycler.databoundrecycler.DataBoundAdapter
import com.example.market.recycler.databoundrecycler.DataBoundViewHolder

class PaymentFragment : BaseFragment<PaymentFragmentBinding>(R.layout.payment_fragment) {
    private var listAdapter: PaymentsAdapter?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
        binding: PaymentFragmentBinding
    ) {
        binding.apply {
            actionBar.apply {
                backButton.setOnClickListener { closeLastFragment() }
                titleContainer.gravity = Gravity.START
                title.text = getString(R.string.pay_cards)
                options.apply {
                    setImageResource(R.drawable.menu_add)
                    setOnClickListener { openAddNewCartFragment() }
                }
            }

            emptyScreen.apply {
                addItemButton.apply {
                    setOnClickListener { openAddNewCartFragment() }
                    text = context.getString(R.string.add_card)
                }
                lottieView.setAnimationFromUrl("https://assets6.lottiefiles.com/datafiles/rVb1hOBHYfqtck7/data.json")
                titleView.text = getString(R.string.card_empty)
                subtitleView.text = getString(R.string.card_empty_subtitle)
            }
            recyclerView.adapter = PaymentsAdapter().also { listAdapter = it }
        }
    }

    override fun onViewAttachedToParent() {
        super.onViewAttachedToParent()
        loadPaymentCards()
    }

    private fun loadPaymentCards() {
        binding.progressBar.visibleOrGone(true)

        getPaymentCards(object : ResultCallback<ArrayList<PaymentCard>> {

            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(result: ArrayList<PaymentCard>?) {
                binding.apply {
                    progressBar.visibleOrGone(false)
                    emptyScreen.root.visibleOrGone(payCards.isEmpty())
                }

                listAdapter?.notifyDataSetChanged()
            }
        })
    }

    fun openAddNewCartFragment() {
        presentFragmentRemoveLast(AddPayCardFragment(),false)
    }

    inner class PaymentsAdapter: DataBoundAdapter<PayCardLayoutBinding, PaymentCard>(R.layout.pay_card_layout) {
        override fun onCreateViewHolder(
            viewHolder: DataBoundViewHolder<PayCardLayoutBinding>?,
            viewType: Int,
        ) {

        }
        override fun getItemCount(): Int {
            return payCards.size
        }

        override fun onBindViewHolder(
            holder: DataBoundViewHolder<PayCardLayoutBinding>,
            position: Int
        ) {
            holder.binding.apply {
                payCards.getOrNull(position)?.let {
                    cardNumber.text = parseCardNumber(it.cardNumber)
                    yearTextView.text = it.expiryDate
                    userNameView.text = it.cardHolderName
                }
            }
        }

        override fun bindItem(
            holder: DataBoundViewHolder<PayCardLayoutBinding>,
            binding: PayCardLayoutBinding,
            position: Int,
            model: PaymentCard,
        ) {
            //fill
        }
    }
}