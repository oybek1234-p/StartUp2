package com.example.market.boshlash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.market.MainActivity
import com.example.market.R

class BoshlashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view = inflater.inflate(R.layout.fragment_boshlash, container, false)
        val accountingizBormi = view.findViewById<TextView>(R.id.accountingiz_bormi)?.apply {
            setOnClickListener {
                (context as MainActivity).apply {
                //    controller.navigate(R.id.action_boshlashFragment_to_loginFragment3)
                }
            }
        }

        val boshlashButton = view.findViewById<View>(R.id.boshlash)?.apply {
            setOnClickListener {
                (context as MainActivity).apply {
                 //   controller.navigate(R.id.action_boshlashFragment_to_registratsiyaFragment)
                }
            }
        }
        return view
    }
}