package com.example.market.fragments
//
//import android.annotation.SuppressLint
//import android.graphics.Color
//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.example.market.*
//import com.example.market.binding.parseCardNumber
//import com.example.market.binding.visibleOrGone
//import com.example.market.databinding.PayCardLayoutBinding
//import com.example.market.databinding.PaymentFragmentBinding
//import com.example.market.models.PaymentCard
//import com.example.market.recycler.databoundrecycler.DataBoundAdapter
//import com.example.market.recycler.databoundrecycler.DataBoundViewHolder
//import com.example.market.viewUtils.*
//
//class PaymentFragment : BaseFragment<PaymentFragmentBinding>(R.layout.payment_fragment) {
//    private var listAdapter: PaymentsAdapter?=null
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: PaymentFragmentBinding
//    ) {
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                }
//                titleContainer.gravity = Gravity.START
//                title.text = getString(R.string.pay_cards)
//
//                options.apply {
//                    setImageResource(R.drawable.menu_add)
//                    setOnClickListener {
//                        openAddNewCartFragment()
//                    }
//                }
//            }
//
//            emptyScreen.apply {
//                addItemButton.apply {
//                    setOnClickListener { openAddNewCartFragment() }
//                    text = context.getString(R.string.add_card)
//                }
//                lottieView.setAnimationFromUrl("https://assets6.lottiefiles.com/datafiles/rVb1hOBHYfqtck7/data.json")
//                titleView.text = getString(R.string.card_empty)
//                subtitleView.text = getString(R.string.card_empty_subtitle)
//            }
//
//            recyclerView.adapter = PaymentsAdapter {
//                showCardActions(it)
//            }.apply {
//                listAdapter = this
//                setClickListener(object : RecyclerItemClickListener {
//                    override fun onClick(position: Int, type: Int) {
//
//                    }
//                })
//            }
//        }
//    }
//
//    fun showCardActions(position: Int) {
//        val popLayout = PopupWindowLayout(requireContext()).apply {
//            addItem(0,"Delete",R.drawable.chats_delete) {
//                val alertDialog = AlertDialogLayout(requireView())
//                val alert = Alert("Delete card","Do you really want delete this card?","CANCEL","DELETE",
//                    { alertDialog.dismiss() },
//                    {
//                        deleteCard(position)
//                        alertDialog.dismiss()
//                    },iconResource = R.drawable.ic_ab_delete,iconTint = Color.DKGRAY,hasPadding = true)
//
//                showAlertDialog(alertDialog,alert)
//            }
//        }
//        var x = 0
//        var y = 0
//        binding.recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.apply {
//            val intArray = IntArray(2)
//            val padding = AndroidUtilities.dp(50f)
//            getLocationInWindow(intArray)
//            popLayout.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.AT_MOST))
//            x = intArray.first() + width - popLayout.measuredWidth - padding
//            y = intArray.last() + padding
//        }
//
//        PopupDialog(popLayout).apply {
//            showPopupDialog(this,requireView(),Gravity.NO_GRAVITY,x,y,false)
//        }
//    }
//
//    fun deleteCard(position: Int) {
//        payCards.getOrNull(position)?.let {
//            deletePaymentCard(it,null)
//
//            loadPaymentCards()
//        }
//    }
//
//    override fun onViewAttachedToParent() {
//        super.onViewAttachedToParent()
//        loadPaymentCards()
//    }
//
//    private fun loadPaymentCards() {
//        binding.progressBar.visibleOrGone(true)
//
//        getPaymentCards(object : ResultCallback<ArrayList<PaymentCard>> {
//            @SuppressLint("NotifyDataSetChanged")
//            override fun onSuccess(result: ArrayList<PaymentCard>?) {
//                binding.apply {
//                    progressBar.visibleOrGone(false)
//                    emptyScreen.root.visibleOrGone(payCards.isEmpty())
//                }
//                listAdapter?.notifyDataSetChanged()
//            }
//        })
//    }
//
//    fun openAddNewCartFragment() {
//        presentFragmentRemoveLast(AddPayCardFragment(),false)
//    }
//
//    class PaymentsAdapter(val onOptionsClicked: (positions: Int)-> Unit): DataBoundAdapter<PayCardLayoutBinding, PaymentCard>(R.layout.pay_card_layout) {
//
//        init {
//            setHasStableIds(false)
//        }
//
//        override fun onCreateViewHolder(
//            viewHolder: DataBoundViewHolder<PayCardLayoutBinding>,
//            viewType: Int,
//        ) {
//            viewHolder.binding.actionView.setOnClickListener {
//                onOptionsClicked(viewHolder.layoutPosition)
//            }
//        }
//
//        override fun getItemCount(): Int {
//            return payCards.size
//        }
//
//        override fun onBindViewHolder(
//            holder: DataBoundViewHolder<PayCardLayoutBinding>,
//            position: Int
//        ) {
//            holder.binding.apply {
//                payCards.getOrNull(position)?.let {
//                    cardNumber.text = parseCardNumber(it.cardNumber)
//                    yearTextView.text = it.expiryDate
//                    userNameView.text = it.cardHolderName
//                }
//            }
//        }
//
//        override fun bindItem(
//            holder: DataBoundViewHolder<PayCardLayoutBinding>,
//            binding: PayCardLayoutBinding,
//            position: Int,
//            model: PaymentCard,
//        ) {
//            //fill
//        }
//    }
//}