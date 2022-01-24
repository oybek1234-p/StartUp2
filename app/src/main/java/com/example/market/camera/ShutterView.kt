package com.example.market.camera

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.example.market.R
import com.example.market.navigation.FragmentController
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.getDrawable
import com.example.market.utils.log
import com.example.market.viewUtils.toast

class ShutterView @JvmOverloads constructor(context: Context,attrs:AttributeSet?=null,deffStyle: Int = 0) : View(context, attrs,deffStyle) {

    private var shutterCircleDrawable: Drawable?=null
    private var shutterCircleStrokeDrawable: Drawable?=null

    var shutterCircleColor = 0
    var shutterCircleStrokeColor = 0

    var shutterListener: ShutterListener?=null

    var isAnimating = false

    private var lastAnimatedValue = 0f
    private var shutterCircleDrawableCurrentSize = Rect()
    private var shutterCircleStrokeDrawableCurrentSize = Rect()

    private var shutterCircleADrawableCurrentSize = Rect()
    private var shutterCircleAStrokeDrawableCurrentSize = Rect()

    private var shutterStrokeOffset = 0
    private var shutterCircleOffset = 0
    private var shutterStrokeWidth = 0

    var isRecording = false
        set(value) {
            if (value!=field) {
                field = value
                animator?.apply {
                    setFloatValues(0f,1f)
                    if (isRecording){
                        startAnimation()
                    } else {
                        cancelAnimator()
                    }
                }
            }
        }

    init {
        create()
    }

    fun create() {
        shutterCircleColor = context.getColor(R.color.shutterCircleColor)
        shutterCircleStrokeColor = context.getColor(R.color.red_ff4d40)

        shutterCircleDrawable = getDrawable(R.drawable.oval_grey)?.apply {
            setTint(shutterCircleColor)
        }
        shutterCircleStrokeDrawable = getDrawable(R.drawable.oval_drawable_stroke)

        shutterStrokeOffset = AndroidUtilities.dp(6f)
        shutterCircleOffset = AndroidUtilities.dp(2f)
        shutterStrokeWidth = AndroidUtilities.dp(8f)

        setWillNotDraw(false)
    }

    fun checkDrawableNotNull() = shutterCircleStrokeDrawable == null || shutterCircleDrawable == null

    private var animator = ValueAnimator.ofFloat(0f,1f).apply {

        duration = 1000
        interpolator = FragmentController.decelerateInterpolator

        addUpdateListener {
            isAnimating = true
            update(it.animatedValue as Float)
            invalidate()
        }

        doOnEnd {
            doOnAnimationFinished()
        }
        doOnStart {
            doOnAnimationStarted()
        }
    }

    fun doOnAnimationStarted() {
        isAnimating = true
        shutterListener?.onAnimationStarted()
    }

    fun doOnAnimationFinished() {
        isAnimating = false
        shutterListener?.onAnimationFinished()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureShutter()
    }

    fun measureShutter() {
        shutterCircleStrokeDrawableCurrentSize.set(
            shutterStrokeOffset,
            shutterStrokeOffset,
            measuredWidth - shutterStrokeOffset,
            measuredHeight - shutterStrokeOffset
        )

        val shutterCirclePadding = shutterStrokeOffset + shutterCircleOffset + shutterStrokeWidth

        shutterCircleDrawableCurrentSize.set(
            shutterCirclePadding,
            shutterCirclePadding,
            measuredWidth - (shutterCirclePadding),
            measuredHeight - (shutterCirclePadding)
        )

        update(0f)
    }

    fun updateBounds(circleSize: Rect , strokeSize: Rect) {
        shutterCircleDrawable?.bounds = circleSize
        shutterCircleStrokeDrawable?.bounds = strokeSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (checkDrawableNotNull()) {
            return
        }

        shutterCircleDrawable?.draw(canvas)
        shutterCircleStrokeDrawable?.draw(canvas)

    }

    fun cancelAnimator() {
        animator?.apply {
            repeatCount = 0
            setFloatValues(0f,lastAnimatedValue)
            cancel()
            reverse()
        }
    }

    fun startAnimation() {
        animator?.apply {
            lastAnimatedValue = 1f
            setFloatValues(0f,1f)
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    private fun update(animatedValue: Float) {
        if (checkDrawableNotNull()) {
            return
        }

        val shutterCWidth = shutterCircleDrawableCurrentSize.width()
        val shutterCHeight = shutterCircleDrawableCurrentSize.height()

        if (isRecording&&shutterCircleADrawableCurrentSize.width()!=0 || !isRecording&&shutterCircleADrawableCurrentSize.width()!=shutterCWidth) {

            lastAnimatedValue = animatedValue

            val circleWidth = shutterCWidth - (shutterCWidth * animatedValue)
            val circleHeight = shutterCHeight - (shutterCHeight * animatedValue)

            val circleX = (measuredWidth - circleWidth) / 2
            val circleY = (measuredHeight - circleHeight) / 2

            shutterCircleADrawableCurrentSize.set(circleX.toInt(),
                circleY.toInt(), (circleWidth.toInt() + circleX).toInt(),
                (circleHeight.toInt() + circleY).toInt())

        }

        val circleStrokeCWidth = shutterCircleStrokeDrawableCurrentSize.width()
        val circleStrokeCHeight = shutterCircleStrokeDrawableCurrentSize.height()

        val circleStrokeWidth = circleStrokeCWidth + shutterStrokeOffset * animatedValue
        val circleStrokeHeight = circleStrokeCHeight + shutterStrokeOffset * animatedValue

        val circleStrokeX = (measuredWidth - circleStrokeWidth) / 2
        val circleStrokeY = (measuredHeight - circleStrokeHeight) / 2

        shutterCircleAStrokeDrawableCurrentSize.set(circleStrokeX.toInt(),circleStrokeY.toInt(),
            (circleStrokeWidth.toInt()+circleStrokeX).toInt(),
            (circleStrokeHeight + circleStrokeY).toInt())

        updateBounds(shutterCircleADrawableCurrentSize,shutterCircleAStrokeDrawableCurrentSize)
    }

    interface ShutterListener {
        fun onAnimationFinished()
        fun onAnimationStarted()
    }

}