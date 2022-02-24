package com.example.market.camera

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.market.utils.log
import com.example.market.viewUtils.toast
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreview(context: Context): SurfaceView(context),SurfaceHolder.Callback {

    val surfaceHolder: SurfaceHolder? = holder?.apply {
        addCallback(this@CameraPreview)
    }
    
    var camera: Camera?=null
    set(value) {
        if (value!=field) {
            field = value

            if (field!=null) {
                startPreview()
            } else {

            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        toast("OnMeasure")
    }
    /**
     * Start preview off the main thread otherwise it may block user interface.
     */
    fun startPreview() {
        Thread {
            try {
                surfaceHolder?.let {
                    camera?.apply {
                        setPreviewDisplay(it)
                        startPreview()

                    }
                }
            }catch (e: Exception) {

            }
        }.run()
    }

    fun checkSurfaceValid() = holder?.surface?.isValid ?: false

    private fun releaseSurface() {
            try {
                stopPreview()

                holder?.surface?.apply {
                    if (isValid) {
                        release()

                    }
                }
            }catch (e: Exception) {
                throw e
                //log(CameraFragment.TAG + " Releasing surface exception!")
            }
    }

    private fun stopPreview() {
        try {
            camera?.stopPreview()

        }catch (e: Exception) {
            throw e
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

        startPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //Empty


        stopPreview()


        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseSurface()

    }


}