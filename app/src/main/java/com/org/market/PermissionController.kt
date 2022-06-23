package com.org.market

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity

class PermissionController {

    var permissionResultListener: PermissionResult? = null

    var requestId: Int? = null

    companion object {

        private var INSTANCE: PermissionController? = null

        fun getInstance() =
            if (INSTANCE != null) INSTANCE!! else PermissionController().also { INSTANCE = it }

        fun checkPermissions(context: Context, permissions: Array<String>): Array<String>? {
            var shouldRequest: ArrayList<String>? = null

            permissions.forEach {
                if (context.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {

                    if (shouldRequest == null) {
                        shouldRequest = ArrayList()
                    }

                    shouldRequest?.add(it)
                }
            }

            return if (shouldRequest == null) {
                null
            } else {
                return shouldRequest!!.toTypedArray()
            }
        }

    }

    fun requestPermissions(
        context: Context,
        requestId: Int,
        permissions: Array<String>,
        resultListener: PermissionResult,
    ) {
        if (context !is Activity || context !is AppCompatActivity) {
            toast("Not activity")
            return
        }

        permissionResultListener = resultListener
        this.requestId = requestId

        val shouldRequest = checkPermissions(context, permissions)

        if (shouldRequest == null || shouldRequest.isEmpty()) {

            resultListener.onGranted()

        } else {

            context.requestPermissions(shouldRequest, requestId)
        }
    }

    fun onPermissionResult(requestId: Int, grantResults: IntArray) {
        if (this.requestId != requestId) {
            return
        }
        runOnUiThread({
            permissionResultListener?.apply {
                grantResults.forEach {
                    if (it == PackageManager.PERMISSION_DENIED) {
                        onDenied()
                        return@runOnUiThread
                    }
                }
                onGranted()
                permissionResultListener = null
            }
        })

        this.requestId = -1
    }

    interface PermissionResult {

        fun onGranted()

        fun onDenied()

    }
}