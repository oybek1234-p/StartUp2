package com.example.market.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.example.market.MyApplication;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AndroidUtilities {
    public static int dp(float value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,value, MyApplication.Companion.getAppContext().getResources().getDisplayMetrics());
    }
    public static String getNumberFormat(int number){
        NumberFormat numberFormat = new DecimalFormat("#,###");
        return numberFormat.format(number);
    }

    public static void runOnUIThread(Runnable runnable,long delay){
        MyApplication.Companion.getHandler().postDelayed(runnable,delay);
    }
    public static void runOnUIThread(Runnable runnable){
        MyApplication.Companion.getHandler().post(runnable);
    }
    public static void cancelRunOnUIThread(Runnable runnable){
        MyApplication.Companion.getHandler().removeCallbacks(runnable);
    }
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }
    public static int density = 0;
}
