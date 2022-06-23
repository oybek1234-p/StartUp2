package com.org.ui

import androidx.core.widget.doAfterTextChanged
import com.example.market.R
import com.example.market.databinding.FragmentAddPayCardBinding
import com.org.market.*
import com.org.net.models.PayCard
import com.org.ui.actionbar.*

class AddPaymentCardFragment : BaseFragment<FragmentAddPayCardBinding>(R.layout.fragment_add_pay_card) {
    private val paymentCard = PayCard()
    private var donePressed = false

    override fun onResume() {
        super.onResume()
        showKeyboard(requireBinding().cardNumberEditText)
    }

    override fun onCreateView(binding: FragmentAddPayCardBinding) {
        actionBar.apply {
            title = "Add payment card"
            addMenuItem(0,R.drawable.ic_done).setOnClickListener {
                addPaymentCard()
            }
        }
        binding.apply {
            cardNumberEditText.apply {
                editText!!.apply {
                    var change = false
                    doAfterTextChanged {
                        if (change) return@doAfterTextChanged
                        change = true
                        val parsed = parseCardNumber(it.toString().replace(" ",""))
                        it!!.clear()
                        it.append(parsed)
                        change = false
                    }
                }
                clearErrorOnTextChange()
            }
            cardExpiryYearEditText.apply {
                editText!!.apply {
                    var change = false
                    var last = 0
                    doAfterTextChanged {
                        if (change) return@doAfterTextChanged
                        change = true
                        val length = text.length
                        if (length == 2 && length>last) {
                            append("/")
                        }
                        last = text.length
                        change = false
                    }
                }
                clearErrorOnTextChange()
            }
            cardCvvEditText.clearErrorOnTextChange()
        }
    }

    fun addPaymentCard() {
        if (donePressed) return
        requireBinding().apply {
            val cardNumber = cardNumberEditText.editText!!.text.toString()
            if (cardNumber.length<19) {
                cardNumberEditText.setErrorMessage("Card number should have 16 chars")
                cardNumberEditText.shakeView()
                return@apply
            }
            val expiryDate = cardExpiryYearEditText.editText!!.text.toString()
            if (expiryDate.length<5) {
                cardExpiryYearEditText.setErrorMessage("Expiry date invalid")
                cardExpiryYearEditText.shakeView()
                return@apply
            }
            val cvvNumber = cardCvvEditText.editText!!.text.toString()
            if (cvvNumber.length<3) {
                cardCvvEditText.setErrorMessage("CVV must have 3 numbers")
                cardCvvEditText.shakeView()
                return@apply
            }
            donePressed = true
            paymentCard.apply {
                id = newId()
                this.number = cardNumber.replace(" ","",false).toLong()
                this.expiryDate = Pair(expiryDate.substring(0,1).toInt(),expiryDate.substring(3,4).toInt())
                this.cvv = cvvNumber.toInt()
            }
            DataController.uploadPaymentCard(paymentCard,object : Result{
                override fun onSuccess() {
                    closeLastFragment()
                }

                override fun onFailed() {
                    donePressed = false
                }
            })
        }
    }
}