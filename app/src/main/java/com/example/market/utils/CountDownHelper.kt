package com.example.market.utils

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper

class CountDownHelper {
    private val handler = Handler(Looper.getMainLooper())

    private var lastTimeMillis = 0L

    private var countDownTimer: CountDownTimer?=null

    var listener: CountDownInterface?=null

    var progress = 0

    private var millisInFuture = 0L

    var isFinished = true

    fun start(millis: Long) {
        post {
            millisInFuture = millis
            updateTimer(millis)
            countDownTimer?.start()
        }
    }

    fun post(unit: ()->Unit) {
        handler.post(unit)
    }

    fun stop() {
        post {
            isFinished = true
            lastTimeMillis = 0L
            millisInFuture = 0
            countDownTimer?.cancel()
            listener?.onFinish()
        }
    }

    fun pause() {
        post {
            countDownTimer?.cancel()
        }
    }

    fun updateTimer(millis: Long) {
        countDownTimer = object : CountDownTimer(millis,100L) {
            override fun onTick(millisUntilFinished: Long) {
                lastTimeMillis = millisUntilFinished
                isFinished = false
                progress = (100 - (100L * lastTimeMillis / millisInFuture)).toInt()
                listener?.onProgress(millisInFuture,lastTimeMillis,progress)
            }

            override fun onFinish() {
                if (progress == 100){
                    isFinished = true
                    listener?.onFinish()
                }
            }
        }
    }

    fun resume() {
        post {
            updateTimer(lastTimeMillis)
            countDownTimer?.start()
        }
    }

    interface CountDownInterface {
        fun onFinish()
        fun onProgress(millisInFuture: Long,lastMillis: Long,progress: Int)
    }
}