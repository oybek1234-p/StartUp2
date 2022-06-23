package com.example.market.theme
//
//import android.graphics.Color
//import com.example.market.viewUtils.toast
//
//object Theme {
//
//    const val key_actionBarBackground = "actionBarBackground"
//    const val key_actionBarIcon = "actionBarIconColor"
//    const val key_actionBarText = "actionBarText"
//
//    const val key_windowBackground = "windowBackground"
//
//    const val key_bottomNavBackground = "bottomNavBackground"
//    const val key_bottomNavIcon = "bottomNavIcon"
//    const val key_bottomNavText = "bottomNavText"
//    const val key_bottomNavIconSelected = "bottomNavIconSelected"
//    const val key_bottomNavTextSelected = "bottomNavTextSelected"
//
//    const val key_productBackground = "productBackground"
//    const val key_productTitle = "productTitle"
//    const val key_productCost = "productCost"
//    const val key_productDiscount = "productDiscount"
//    const val key_productDiscountBackground = "productDiscountBackground"
//    const val key_productDiscountStrokeThough = "productDiscountStrokeThough"
//    const val key_productDiscountPercent = "productDiscountPercent"
//    const val key_productShippingBackground = "productShippingBackground"
//    const val key_productShipping = "productShipping"
//
//    const val key_searchText = "searchText"
//    const val key_searchButtonBackground = "searchButtonBackground"
//    const val key_searchIcon = "searchIcon"
//    const val key_searchBackground = "searchIcon"
//    const val key_productAddIcon = "productAddIcon"
//
//    private var ThemeBlueLight = HashMap<String,Int>().apply {
//        put(key_actionBarBackground,Color.parseColor("#3F83B0"))
//        put(key_actionBarIcon,Color.WHITE)
//        put(key_actionBarText,Color.WHITE)
//
//        put(key_windowBackground,Color.parseColor("#F2F2F2"))
//
//        put(key_productBackground,Color.WHITE)
//        put(key_productTitle,Color.parseColor("#8A9398"))
//        put(key_productCost,Color.BLACK)
//        put(key_productDiscount,Color.parseColor("#8A9398"))
//        put(key_productDiscountStrokeThough,Color.parseColor("#4FA9E7"))
//        put(key_productDiscountPercent,Color.parseColor("#4FA9E7"))
//        put(key_productShippingBackground,Color.parseColor("#F2F2F2"))
//        put(key_productShipping,Color.parseColor("#8A9398"))
//
//        put(key_bottomNavBackground,Color.WHITE)
//        put(key_bottomNavIcon,Color.parseColor("#8A9398"))
//        put(key_bottomNavText,Color.parseColor("#8995A1"))
//        put(key_bottomNavIconSelected,Color.parseColor("#4FA9E7"))
//        put(key_bottomNavTextSelected,Color.parseColor("#4FA9E7"))
//
//        put(key_productAddIcon,Color.parseColor("#4FA9E7"))
//
//        put(key_searchBackground,Color.WHITE)
//        put(key_searchText,Color.parseColor("#7E858E"))
//        put(key_searchButtonBackground,Color.parseColor("#4FA9E7"))
//        put(key_searchIcon,Color.WHITE)
//    }
//
//    private var ThemeBlueDark = HashMap<String,Int>().apply {
//        put(key_actionBarBackground,Color.parseColor("#222E3C"))
//        put(key_actionBarIcon,Color.WHITE)
//        put(key_actionBarText,Color.WHITE)
//
//        put(key_windowBackground,Color.parseColor("#151E27"))
//
//        put(key_productBackground,Color.parseColor("#1D2733"))
//        put(key_productTitle,Color.parseColor("#D5D6DB"))
//        put(key_productCost,Color.WHITE)
//        put(key_productDiscount,Color.parseColor("#D5D6DB"))
//        put(key_productDiscountStrokeThough,Color.parseColor("#62AADD"))
//        put(key_productDiscountPercent,Color.parseColor("#62AADD"))
//        put(key_productShippingBackground,Color.parseColor("#151E27"))
//        put(key_productShipping,Color.parseColor("#D5D6DB"))
//
//        put(key_bottomNavBackground,Color.parseColor("#222E3C"))
//        put(key_bottomNavIcon,Color.parseColor("#8995A1"))
//        put(key_bottomNavText,Color.parseColor("#8995A1"))
//        put(key_bottomNavIconSelected,Color.parseColor("#62AADD"))
//        put(key_bottomNavTextSelected,Color.parseColor("#62AADD"))
//
//        put(key_productAddIcon,Color.parseColor("#62AADD"))
//
//        put(key_searchBackground,Color.parseColor("#222E3C"))
//        put(key_searchText,Color.parseColor("#8995A1"))
//        put(key_searchButtonBackground,Color.parseColor("#62AADD"))
//        put(key_searchIcon,Color.WHITE)
//    }
//
//    var currentColors = HashMap<String,Int>()
//    var currentTheme = Themes.NoTheme
//
//    fun applyTheme(theme: Themes) {
//        if (currentTheme == theme) {
//            return
//        }
//        currentTheme = theme
//        currentColors = if (theme == Themes.ThemeBlueLight) ThemeBlueLight else ThemeBlueDark
//    }
//
//    @JvmStatic
//    fun getColor(id: String): Int {
//        return currentColors[id] ?: 0
//    }
//
//    enum class Themes {
//        ThemeBlueLight,
//        ThemeBlueDark,
//        NoTheme
//    }
//
//}