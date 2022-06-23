package com.example.market.fragments

//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.EditText
//import androidx.core.view.doOnDetach
//import androidx.core.widget.doAfterTextChanged
//import androidx.core.widget.doOnTextChanged
//import com.example.market.*
//import com.example.market.binding.parseCardNumber
//import com.example.market.databinding.FragmentAddPayCardBinding
//import com.example.market.models.PaymentCard
//import com.example.market.viewUtils.toast
//import com.example.market.viewUtils.vibrate
//
//class AddPayCardFragment : BaseFragment<FragmentAddPayCardBinding>(R.layout.fragment_add_pay_card) {
//
//    fun checkCorrect(cardNumber: String,expDate: String,cvv: String): Boolean {
//        return cardNumber.length==16 && expDate.length == 4 && cvv.length == 3
//    }
//
//    fun savePayCard(payCard: PaymentCard) {
//        addNewPaymentCard(payCard,object : Result {
//            override fun onSuccess(any: Any?) {
//                //fill
//            }
//        })
//        closeLastFragment()
//    }
//
//
//    override fun onResume() {
//        super.onResume()
//        getMainActivity().showKeyboard(binding.cardNumberEditText)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentAddPayCardBinding
//    ) {
//        binding.apply {
//
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                }
//
//                title.text = getString(R.string.add_card)
//                titleContainer.gravity = Gravity.START
//
//                options.apply {
//                    setImageResource(R.drawable.ic_done)
//                    setOnClickListener {
//                        val cardNumber = cardNumberEditText.editText!!.text.toString().replace(" ","")
//                        val expiryDate = cardExpiryYearEditText.editText!!.text.toString().replace("/","")
//                        val cvv = cardCvvEditText.editText!!.text.toString()
//
//                        if (checkCorrect(cardNumber,expiryDate,cvv)&& checkCurrentUser()) {
//                            val payCard = PaymentCard().apply {
//                                userId = currentUser!!.id
//                                this.cardNumber = cardNumber.toLong()
//                                this.expiryDate = expiryDate
//                                this.cvv = cvv.toInt()
//                            }
//                            savePayCard(payCard)
//                        } else {
//                            vibrate(20)
//                        }
//                    }
//                }
//            }
//
//            cardNumberEditText.apply {
//                editText?.apply {
//                    var canUpdate = true
//                    doAfterTextChanged {
//                        if (text.isNotEmpty()&&canUpdate) {
//                            val cardNumber = parseCardNumber(
//                                text.toString().replace(" ","").trim().toLong())
//                            it?.apply {
//                                canUpdate = false
//                                text.delete(0,length)
//                                append(cardNumber)
//                                canUpdate = true
//                            }
//                        }
//                    }
//
//                    setOnFocusChangeListener { _, hasFocus ->
//                        val isWrong = text.length!=19 && !hasFocus
//
//                        isErrorEnabled = isWrong
//                        if (isWrong) {
//                            error = "Card number must be 16 characters"
//                        }
//                    }
//                }
//            }
//
//            cardExpiryYearEditText.apply {
//                editText?.apply {
//                    var canUpdate = true
//                    var lastIndex = 0
//                    doAfterTextChanged {
//                        if (text.isNotEmpty()&&canUpdate) {
//                            val expiryDateText = text.toString()
//
//                            if (expiryDateText.length==2&&expiryDateText.length>lastIndex) {
//                                canUpdate = false
//                                append("/")
//                                canUpdate = true
//                            }
//                            lastIndex = expiryDateText.length
//                        }
//                    }
//
//                    setOnFocusChangeListener { _, hasFocus ->
//                        val isWrong = text.length!=5 && !hasFocus
//                        isErrorEnabled = isWrong
//                        if (isWrong) {
//                            error = "Expiry date must contain right date"
//                        }
//                    }
//                }
//            }
//
//            cardCvvEditText.apply {
//                editText?.apply {
//                    setOnFocusChangeListener { _, hasFocus ->
//                        val isWrong = text.length!=3 && !hasFocus
//                        isErrorEnabled = isWrong
//                        if (isWrong) {
//                            error = "CVV must be 3 secret numbers"
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//}