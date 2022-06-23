package com.example.market.viewUtils
//
//import android.content.Context
//import android.content.res.ColorStateList
//import android.graphics.Color
//import android.util.AttributeSet
//import android.util.TypedValue
//import android.view.Gravity
//import android.view.View
//import android.view.ViewGroup
//import android.view.WindowManager
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import com.example.market.MyApplication
//import com.example.market.R
//import com.example.market.utils.getDrawable
//
//class PopupWindowLayout @JvmOverloads constructor(context: Context,attributeSet: AttributeSet?=null,deffStyle:Int = 0)  : LinearLayout(context,attributeSet,deffStyle){
//    private var items = HashMap<Int,Item>()
//    var dissmis: (() -> Unit?)? =null
//
//    init {
//        background = getDrawable(R.drawable.popup_fixed_alert2)
//        orientation = VERTICAL
//        layoutParams = WindowManager.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//    }
//
//    fun itemSize() = items.size
//    fun getItem(key: Int) = items[key]
//
//    fun addItem(id: Int,title: String,iconRes: Int,onClick:(v: View) -> Unit){
//        val item = Item(context).apply {
//            setData(title,iconRes)
//            setOnClickListener {
//                onClick(it)
//                dissmis?.let { it1 -> it1() }
//            }
//            items[id] = this
//        }
//        addView(item)
//    }
//
//    class Item(context: Context) : LinearLayout(context){
//        private val m = AndroidUtilities.dp(18f)
//
//        init {
//            orientation = HORIZONTAL
//            setPadding(m,AndroidUtilities.dp(12f),m,AndroidUtilities.dp(12f))
//            isClickable = true
//            isFocusable = true
//        }
//
//        fun setData(title: String,iconRes: Int) {
//            titleView.text = title
//            iconView.setImageResource(iconRes)
//        }
//
//        private var iconView = ImageView(context).apply {
//            imageTintList = ColorStateList.valueOf(Color.rgb(103,106,111))
//            addView(this, LayoutParams(AndroidUtilities.dp(24f), AndroidUtilities.dp(24f)).apply {
//                gravity = Gravity.CENTER
//            })
//        }
//
//        private var titleView = TextView(context).apply {
//            setTextColor(Color.BLACK)
//            typeface = MyApplication.robotoRegular
//            setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f)
//            addView(this, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
//                marginStart = AndroidUtilities.dp(12f)
//                gravity = Gravity.CENTER_VERTICAL
//            })
//        }
//    }
//}