package com.example.market.profile
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.example.market.*
//import com.example.market.binding.inflateBinding
//import com.example.market.databinding.FragmentEditUserInfoBinding
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.firestore.FirebaseFirestore
//
//class EditUserInfoFragment : BaseFragment<FragmentEditUserInfoBinding>(R.layout.fragment_edit_user_info) {
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//        binding: FragmentEditUserInfoBinding
//    ) {
//        binding.apply {
//            actionBar.apply {
//                backButton.setOnClickListener {
//                    closeLastFragment()
//                }
//                currentUser?.apply {
//                    nameEdtxt.text.append(name)
//                    aboutEdtxt.text.append(about)
//                }
//                options.apply {
//                    setImageResource(R.drawable.account_check)
//                    setOnClickListener {
//                        val name = nameEdtxt.text.toString()
//                        val about = aboutEdtxt.text.toString()
//                        if (name.isNotEmpty()) {
//                            getUserReference(currentUser!!.id).apply {
//                                child("name").setValue(name)
//                                child("about").setValue(about)
//                                currentUser?.apply {
//                                    this.name = name
//                                    this.about = about
//                                    closeLastFragment()
//                                }
//                            }
//                        } else {
//                            nameEdtxt.apply {
//                                requestFocus()
//                                error = "Required"
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
