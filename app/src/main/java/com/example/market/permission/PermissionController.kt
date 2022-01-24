package com.example.market.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.market.utils.AndroidUtilities

class PermissionController {

    var permissionResultListener: PermissionResult?=null

    var requestId: Int?=null

    companion object {

        private var INSTANCE: PermissionController?=null

        fun getInstance() = if (INSTANCE!=null) INSTANCE!! else PermissionController().also { INSTANCE = it }

        fun checkPermissions(context: Context,permissions: Array<String>): Array<String>? {
            var shouldRequest: ArrayList<String>?=null

            permissions.forEach {
                if (context.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {

                    if (shouldRequest==null) {
                        shouldRequest = ArrayList()
                    }

                    shouldRequest?.add(it)
                }
            }

            return if (shouldRequest==null) {
                null
            } else {
                val array = arrayOf<String>()
                shouldRequest?.toArray(array)
                array
            }
        }

    }

    fun requestPermissions(context: Context,requestId: Int,permissions: Array<String>,resultListener: PermissionResult) {
        if (context !is Activity||context !is AppCompatActivity) {
            return
        }

        permissionResultListener = resultListener
        this.requestId = requestId

        val shouldRequest = checkPermissions(context,permissions)

        if (shouldRequest==null) {

            resultListener.onGranted()

        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(context,shouldRequest,requestId)
            } else {
                context.requestPermissions(permissions,requestId)
            }

        }
    }

    fun onPermissionResult(requestId: Int,grantResults: IntArray) {
        if (this.requestId != requestId) {
            return
        }

        AndroidUtilities.runOnUIThread {
            permissionResultListener?.apply {
                grantResults.forEach {
                    if (it == PackageManager.PERMISSION_DENIED) {
                        onDenied()
                        return@runOnUIThread
                    }
                }
                onGranted()
            }
        }
        this.requestId = null
        this.permissionResultListener = null
    }

}