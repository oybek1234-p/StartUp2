package com.example.market.utils;

import android.util.Log;

public class FileLog {
    public static void e(Exception e){

    }
    public static void e(String e){
        Log.i("StartUp",e);
    }
    public static void e(Throwable e){

    }
    public static void d(Throwable e){

    }
    public static void d(Exception e){

    }
    public static void d(String e){
        Log.i("StartUp",e);
    }
}
