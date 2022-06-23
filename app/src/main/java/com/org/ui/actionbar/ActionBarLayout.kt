package com.org.ui.actionbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.forEach
import com.example.market.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.org.market.*
import com.org.net.models.MESSAGE_TYPE_ALL
import com.org.ui.HomeFragment
import com.org.ui.MessagesFragment
import com.org.ui.OrdersFragment
import com.org.ui.ProfileFragment
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class ActionBarLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    deff: Int = 0,
) : FrameLayout(context, attributeSet, deff) {
    lateinit var containerView: LayoutContainer
    lateinit var containerViewBack: LayoutContainer
    var currentActionBar: ActionBar? = null

    var newFragment: BaseFragment<*>? = null
    var oldFragment: BaseFragment<*>? = null

    var currentAnimation: AnimatorSet? = null

    private val decelerateInterpolator = DecelerateInterpolator(1.5f)
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()

    private var maybeStartTracking = false
    private var startedTracking = false
    private var startedTrackingX = 0
    private var startedTrackingY = 0
    private var animationInProgress = false

    private var velocityTracker: VelocityTracker? = null
        get() {
            if (field == null) {
                field = VelocityTracker.obtain()
            }
            return field
        }

    init {
        setWillNotDraw(false)
    }

    lateinit var bottomNav: BottomNavigationView
    private var bottomMenuIds = HashMap<Int, Int>()
    private var currentBottomItem = 0
    private var oldBottomItem = 0

    fun closePreviousFragment() {
        if (fragmentStack.size > 1) {
            val previousFragment = fragmentStack[fragmentStack.size - 1]
            removeFragmentFromStackInternal(previousFragment)
        }
    }

    fun setUpWithBottomNav(bottomNavigationView: BottomNavigationView) {
        bottomNav = bottomNavigationView
        bottomNavigationView.apply {
            setOnItemSelectedListener { it ->
                val itemId = it.itemId
                oldBottomItem = currentBottomItem
                currentBottomItem = itemId
                if (oldBottomItem == currentBottomItem) {
                    return@setOnItemSelectedListener false
                }
                val openedMenu = bottomMenuIds[itemId]
                var fragment: BaseFragment<*>? = null
                if (openedMenu != null) {
                    loop@ for (i in 0 until fragmentStack.size) {
                        val fromStackFragment = fragmentStack.getOrNull(i)
                        if (fromStackFragment != null) {
                            if (fromStackFragment.classGuid == openedMenu) {
                                fragment = fromStackFragment
                                fragmentStack.remove(fromStackFragment)
                                break@loop
                            }
                        }
                    }
                }
                if (fragment == null) {
                    fragment = when (it.itemId) {
                        R.id.home -> HomeFragment().apply { showBottomNav = true }
                        R.id.orders -> OrdersFragment().apply { showBottomNav = true }
                        R.id.messages -> MessagesFragment(currentUserId(), MESSAGE_TYPE_ALL,false).apply { showBottomNav = true }
                        R.id.profile -> ProfileFragment(currentUserId()).apply {
                            isCurrentProfile = true
                            showBottomNav = true
                        }
                        else -> null
                    }
                }
                fragment?.apply {
                    bottomMenuIds[itemId] = classGuid
                    this@ActionBarLayout.presentFragment(
                        this,
                        false,
                        false)
                }
                return@setOnItemSelectedListener true
            }
            selectedItemId = R.id.home
        }
    }

    open class LayoutContainer(context: Context) : FrameLayout(context) {
        val headerShadowDrawable = getDrawable(R.drawable.header_shadow)

        init {
            setWillNotDraw(false)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            var actionBarHeight = 0
            forEach {
                it.apply {
                    if (tag == ActionBar.TAG) {
                        actionBarHeight = measuredHeight
                        layout(0, 0, measuredWidth, actionBarHeight)
                    }
                }
            }
            forEach {
                it.apply {
                    if (tag != ActionBar.TAG) {
                        val params = layoutParams as LayoutParams
                        layout(params.leftMargin,
                            params.topMargin + actionBarHeight,
                            params.leftMargin + measuredWidth,
                            params.topMargin + measuredHeight)
                    }
                }
            }
        }

        override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
            if (child?.tag == ActionBar.TAG) {
                return super.drawChild(canvas, child, drawingTime)
            } else {
                var actionBarHeight = 0
                var actionBarY = 0
                forEach loop@{
                    it.apply {
                        if (this == child) {
                            return@loop
                        }
                        if (tag == ActionBar.TAG && visibility == View.VISIBLE) {
                            actionBarHeight = measuredHeight
                            actionBarY = y.toInt()
                        }
                    }
                }
                try {
                    val result = super.drawChild(canvas, child, drawingTime)
                    if (actionBarHeight != 0) {
                        headerShadowDrawable?.apply {
                            setBounds(0,
                                actionBarY + actionBarHeight,
                                measuredWidth,
                                intrinsicHeight + actionBarY + actionBarHeight)
                            draw(canvas!!)
                        }
                    }
                    return result
                } catch (e: Exception) {

                }
            }
            return false
        }
    }

    private var layoutToIgnore: View? = null
    private var beginTrackingSent = false
    private var transitionAnimationInProgress = false

    private var transitionAnimationStartTime: Long = 0
    private var startedTrackingPointerId = 0
    private var onCloseAnimationEndRunnable: Runnable? = null
    private var onOpenAnimationEndRunnable: Runnable? = null
    private var animationRunnable: Runnable? = null

    private var animationProgress = 0f
    private var lastFrameTime: Long = 0

    fun parentActivity() = findActivity(context)!!

    lateinit var fragmentStack: ArrayList<BaseFragment<*>>

    fun currentFragment() = fragmentStack.lastOrNull()

    fun previousFragment(): BaseFragment<*>? {
        fragmentStack.apply {
            if (size >= 2) {
                return getOrNull(size - 2)
            }
        }
        return null
    }

    private var innerTranslationX = 0f
        set(value) {
            if (value != field) {
                field = value
                invalidate()
                previousFragment()?.apply {
                    val progress = value / containerView.width
                    onSlideProgress(false, progress)
                }
            }
        }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        val width = width - paddingLeft - paddingRight
        val translationX = innerTranslationX + paddingRight
        var clipLeft = paddingLeft
        var clipRight = width + paddingLeft

        if (child == containerViewBack) {
            clipRight = (translationX + dp(1f)).toInt()
        } else if (child == containerView) {
            clipLeft = translationX.toInt()
        }

        val restoreCount = canvas.save()
        if (!transitionAnimationInProgress) {
            canvas.clipRect(clipLeft, 0, clipRight, height)
        }

        val result = super.drawChild(canvas, child, drawingTime)
        canvas.restoreToCount(restoreCount)

        if (translationX != 0f) {
            if (child == containerView) {
                val alpha = max(0f, min((width - translationX) / dp(20f), 1f))
                layerShadowDrawable?.apply {
                    setBounds((translationX - intrinsicWidth).toInt(),
                        child.top,
                        translationX.toInt(),
                        child.bottom)
                    setAlpha((0xff * alpha).toInt())
                    draw(canvas)
                }
            } else if (child == containerViewBack) {
                var opacity = min(0.8f, (width - translationX) / width.toFloat())
                if (opacity < 0f) {
                    opacity = 0f
                }
                scrimPaint.color = ((-0x67000000 and -0x1000000 ushr 24) * opacity).toInt() shl 24
                canvas.drawRect(clipLeft.toFloat(),
                    0f,
                    clipRight.toFloat(),
                    height.toFloat(),
                    scrimPaint)

            }
        }
        return result
    }

    fun checkTransitionAnimation() = transitionAnimationInProgress

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val slideStarted = slideTouchEvent(ev)
        if (slideStarted) {
            ev?.apply {
                action = MotionEvent.ACTION_CANCEL
            }
        }
        super.dispatchTouchEvent(ev)
        return true
    }

    fun slideTouchEvent(event: MotionEvent?): Boolean {
        if (!checkTransitionAnimation() && !animationInProgress) {
            fragmentStack.apply {
                if (size > 1) {
                    event?.apply {
                        if (action == MotionEvent.ACTION_DOWN) {
                            currentFragment()!!.apply {
                                if (!isSwapBackEnabled(event)) {
                                    maybeStartTracking = false
                                    startedTracking = false
                                    return false
                                }
                            }
                            startedTrackingPointerId = getPointerId(0)
                            maybeStartTracking = true
                            startedTrackingX = x.toInt()
                            startedTrackingY = y.toInt()
                            velocityTracker?.clear()
                        } else if (action == MotionEvent.ACTION_MOVE && getPointerId(0) == startedTrackingPointerId) {
                            val dx = max(0f, x - startedTrackingX)
                            val dy = abs((y - startedTrackingY))
                            velocityTracker!!.addMovement(event)
                            if (
                                !transitionAnimationInProgress
                                && maybeStartTracking
                                && !startedTracking
                                && dx >= getPixelsInCM(0.4f, true)
                                && abs(dx) / 3 > dy
                            ) {
                                if (
                                    currentFragment()?.canBeginSlide() == true
                                    && findScrollingChild(this@ActionBarLayout, x, y) == null
                                ) {
                                    prepareForMoving(event)
                                } else {
                                    maybeStartTracking = false
                                }
                            } else if (startedTracking) {
                                if (!beginTrackingSent) {
                                    hideKeyboard(parentActivity().currentFocus)
                                    currentFragment()?.onBeginSlide()
                                    beginTrackingSent = true
                                }
                                if (dx > 0) {
                                    containerView.translationX = dx
                                    innerTranslationX = dx
                                }
                            }
                        } else if (
                            getPointerId(0) == startedTrackingPointerId
                            && action == MotionEvent.ACTION_CANCEL
                            || action == MotionEvent.ACTION_UP
                            || action == MotionEvent.ACTION_POINTER_UP
                        ) {
                            velocityTracker?.apply {
                                computeCurrentVelocity(1000)
                                currentFragment()?.apply {
                                    if (!startedTracking && isSwapBackEnabled(event)) {
                                        if (
                                            xVelocity >= 3500
                                            && xVelocity > abs(yVelocity)
                                            && canBeginSlide()
                                        ) {
                                            prepareForMoving(event)
                                            if (!beginTrackingSent) {
                                                hideKeyboard(parentActivity().currentFocus)
                                                beginTrackingSent = true
                                            }
                                        }
                                    }
                                }
                                if (startedTracking) {
                                    AnimatorSet().apply {
                                        containerView.apply {
                                            val backAnimation =
                                                x < measuredWidth / 3.0f && (xVelocity < 3500 || xVelocity < yVelocity)
                                            var distToMove = 0f
                                            if (!backAnimation) {
                                                distToMove = measuredWidth - x
                                                val duration =
                                                    max((200f / measuredWidth * distToMove).toInt(),
                                                        50).toLong()
                                                playTogether(
                                                    ObjectAnimator.ofFloat(this,
                                                        View.TRANSLATION_X,
                                                        measuredWidth.toFloat())
                                                        .setDuration(duration),
                                                    ObjectAnimator.ofFloat(this@ActionBarLayout,
                                                        "innerTranslationX",
                                                        innerTranslationX, measuredWidth.toFloat())
                                                        .setDuration(duration)
                                                )
                                            } else {
                                                distToMove = x
                                                val duration =
                                                    max((200f / measuredWidth * distToMove).toInt(),
                                                        50).toLong()

                                                playTogether(
                                                    ObjectAnimator.ofFloat(this,
                                                        View.TRANSLATION_X,
                                                        0f).setDuration(duration),
                                                    ObjectAnimator.ofFloat(this@ActionBarLayout,
                                                        "innerTranslationX",
                                                        innerTranslationX, 0f).setDuration(duration)
                                                )
                                            }
                                            doOnEnd {
                                                onSlideAnimationEnd(backAnimation)
                                            }
                                            start()
                                            animationInProgress = true
                                            layoutToIgnore = containerViewBack
                                        }
                                    }
                                } else {
                                    maybeStartTracking = false
                                    startedTracking = false
                                    layoutToIgnore = null
                                }
                                velocityTracker?.recycle()
                                velocityTracker = null
                            }
                        }
                    }
                    if (event == null) {
                        maybeStartTracking = false
                        startedTracking = false
                        layoutToIgnore = null
                        velocityTracker?.recycle()
                        velocityTracker = null
                    }
                }
            }
            return startedTracking
        }
        return false
    }

    fun presentFragment(
        baseFragment: BaseFragment<*>,
        removeLastFragment: Boolean,
        needAnimation: Boolean,
    ): Boolean {

        baseFragment.apply {
            val currentFragment = currentFragment()

            setParentLayout(this@ActionBarLayout)

            var fragmentView = fragmentView()
            if (fragmentView == null) {
                fragmentView = createView()
            } else {
                removeFragmentViewFromParent(baseFragment, false)
            }

            containerViewBack.addView(fragmentView)

            fragmentView.apply {
                layoutParams = (layoutParams as ViewGroup.LayoutParams).apply {
                    width = LayoutParams.MATCH_PARENT
                    height = LayoutParams.MATCH_PARENT
                }
            }

            removeActionBarViewFromParent(this, false)

            if (mActionBar != null) {
                val av = actionBar.view()!!
                containerViewBack.addView(
                    av,
                    LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(56f)
                    ))
            } else if (customActionBar != null) {
                (customActionBar!!.parent as ViewGroup?)?.removeView(customActionBar)
                containerViewBack.addView(
                    customActionBar,
                    LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        dp(56f)
                    ))
            }
            fragmentStack.add(this)
            onResume()

            val temp = containerView
            containerView = containerViewBack
            containerViewBack = temp
            containerView.visibility = View.VISIBLE
            innerTranslationX = 0f
            containerView.translationY = 0f

            bringChildToFront(containerView)

            if (!needAnimation) {
                presentFragmentInternalRemoveOld(removeLastFragment, currentFragment)
            }
            containerView.visibility = View.VISIBLE
            containerViewBack.visibility = View.VISIBLE

            if (needAnimation) {
                transitionAnimationStartTime = System.currentTimeMillis()
                transitionAnimationInProgress = true
                layoutToIgnore = containerView

                onOpenAnimationEndRunnable = Runnable {
                    currentFragment?.apply {
                        presentFragmentInternalRemoveOld(removeLastFragment, this)
                        onTransitionAnimationEnd(false, false)
                    }
                    containerView.translationX = 0f
                    onTransitionAnimationEnd(true, false)
                    onBecomeFullyVisible()
                }

                currentFragment?.onTransitionAnimationStart(false, false)
                onTransitionAnimationStart(true, false)

                oldFragment = currentFragment
                newFragment = this

                containerView.apply {
                    alpha = 0f
                    translationX = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                startLayoutAnimation(true, true)
            } else {
                currentFragment?.apply {
                    onTransitionAnimationStart(false, false)
                    onTransitionAnimationEnd(false, false)
                }
                onTransitionAnimationStart(true, false)
                onTransitionAnimationEnd(true, false)
                onBecomeFullyVisible()
            }
        }
        return true
    }

    private var scrimPaint = Paint()
    private var layerShadowDrawable = getDrawable(R.drawable.layer_shadow)

    fun startLayoutAnimation(open: Boolean, first: Boolean) {
        if (first) {
            animationProgress = 0f
            lastFrameTime = System.nanoTime() / 1000000
        }
        animationRunnable = Runnable {
            animationRunnable = null
            if (first) {
                transitionAnimationStartTime = System.currentTimeMillis()
            }
            val newTime = System.nanoTime() / 1000000
            var dt = newTime - lastFrameTime
            if (dt > 18) {
                dt = 18
            }
            lastFrameTime = newTime
            animationProgress += dt / 150f
            if (animationProgress > 1f) {
                animationProgress = 1f
            }
            newFragment?.onTransitionAnimationProgress(true, animationProgress)
            oldFragment?.onTransitionAnimationProgress(false, animationProgress)

            val interpolated = decelerateInterpolator.getInterpolation(animationProgress)
            if (open) {
                containerView.apply {
                    alpha = interpolated
                    translationX = dp(48f) * (1f - interpolated)
                }
            } else {
                containerViewBack.apply {
                    alpha = 1f - interpolated
                    translationX = dp(48f) * interpolated
                }
            }
            if (animationProgress < 1) {
                startLayoutAnimation(open, false)
            } else {
                onAnimationEndCheck(false)
            }
        }
        runOnUiThread(animationRunnable)
    }

    fun onAnimationEndCheck(byCheck: Boolean) {
        onCloseAnimationEnd()
        onOpenAnimationEnd()

        currentAnimation?.apply {
            if (byCheck) {
                cancel()
            }
            currentAnimation = null
        }
        if (animationRunnable != null) {
            cancelRunOnUIThread(animationRunnable)
            animationRunnable = null
        }
        alpha = 1f
        containerView.apply {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }
        containerViewBack.apply {
            alpha = 1f
            scaleY = 1f
            scaleX = 1f
        }
    }

    fun closeLastFragmentInternalRemoveOld(fragment: BaseFragment<*>) {
        fragment.apply {
            onPause()
            onFragmentDestroy()
            setParentLayout(null)
            fragmentStack.remove(this)
            containerViewBack.apply {
                visibility = View.INVISIBLE
                translationX = 0f
            }
            bringChildToFront(containerView)
        }
    }

    fun onCloseAnimationEnd() {
        if (transitionAnimationInProgress && onCloseAnimationEndRunnable != null) {
            transitionAnimationInProgress = false
            layoutToIgnore = null
            transitionAnimationStartTime = 0
            newFragment = null
            oldFragment = null
            val endRunnable = onCloseAnimationEndRunnable
            onCloseAnimationEndRunnable = null
            endRunnable?.run()
        }
    }

    fun onOpenAnimationEnd() {
        if (transitionAnimationInProgress && onOpenAnimationEndRunnable != null) {
            transitionAnimationInProgress = false
            layoutToIgnore = null
            transitionAnimationStartTime = 0
            newFragment = null
            oldFragment = null
            val endRunnable = onOpenAnimationEndRunnable
            onOpenAnimationEndRunnable = null
            endRunnable?.run()
        }
    }

    fun presentFragmentInternalRemoveOld(removeLast: Boolean, fragment: BaseFragment<*>?) {
        if (fragment == null) return
        fragment.apply {
            onBecomeFullyHidden()
            onPause()

            if (removeLast) {
                onFragmentDestroy()
                setParentLayout(null)
                fragmentStack.remove(this)
            } else {
                removeFragmentViewFromParent(this,false)
                removeActionBarViewFromParent(this,false)
            }
            containerViewBack.visibility = View.INVISIBLE
        }
    }

    fun onSlideAnimationEnd(backAnimation: Boolean) {
        fragmentStack.apply {
            val currentFragment = currentFragment()
            val previousFragment = previousFragment()

            if (!backAnimation) {
                if (size < 2) {
                    return
                }
                currentFragment?.apply {
                    prepareFragmentToSlide(true, false)
                    onPause()
                    onFragmentDestroy()
                    setParentLayout(null)
                    removeAt(lastIndex)
                }

                val temp = containerView
                containerView = containerViewBack
                containerViewBack = temp
                bringChildToFront(containerView)

                previousFragment?.apply {
                    currentActionBar = mActionBar
                    onBecomeFullyVisible()
                    prepareFragmentToSlide(false, false)
                    layoutToIgnore = containerView
                }
            } else {
                if (size >= 2) {
                    currentFragment?.prepareFragmentToSlide(true, false)

                    previousFragment?.apply {
                        prepareFragmentToSlide(false, false)
                        onPause()
                        removeFragmentViewFromParent(this, true)
                    }
                    layoutToIgnore = null
                }
            }
            containerViewBack.apply {
                visibility = View.INVISIBLE
                translationX = 0f
            }
            containerView.translationX = 0f
            innerTranslationX = 0f
            startedTracking = false
            animationInProgress = false

        }
    }

    companion object {
        fun removeFragmentViewFromParent(fragment: BaseFragment<*>?, onLayout: Boolean) {
            fragment?.apply {

                val fragmentView = fragmentView()
                if (fragmentView != null) {

                    val parent = fragmentView.parent as ViewGroup?
                    if (parent != null) {
                        onRemoveFromParent()
                        if (onLayout) {
                            parent.removeViewInLayout(fragmentView)
                        } else {

                            parent.removeView(fragmentView)
                        }
                    }
                }

            }
        }

        fun removeActionBarViewFromParent(fragment: BaseFragment<*>?, onLayout: Boolean) {
            fragment?.apply {
                val view = mActionBar?.mActionBarBinding?.root ?: customActionBar

                val parent = view?.parent as ViewGroup?
                if (parent!=null) {
                    onRemoveFromParent()
                    if (onLayout) {
                        parent.removeViewInLayout(view)
                    } else {
                        parent.removeView(view)
                    }
                }
            }
        }
    }

    fun prepareForMoving(event: MotionEvent) {
        maybeStartTracking = false
        startedTracking = true
        layoutToIgnore = containerViewBack
        startedTrackingX = event.x.toInt()
        containerViewBack.visibility = View.VISIBLE
        beginTrackingSent = false

        previousFragment()?.apply {
            if (mBinding == null) {
                createView()
            }
            fragmentView()?.let { fv ->
                removeFragmentViewFromParent(this, false)

                containerViewBack.addView(fv)

                fv.apply {
                    layoutParams = (layoutParams as LayoutParams).apply {
                        width = LayoutParams.MATCH_PARENT
                        height = LayoutParams.MATCH_PARENT
                    }
                }
                if (mActionBar != null) {
                    removeActionBarViewFromParent(this, false)
                    containerViewBack.addView(actionBar.view())
                } else if (customActionBar != null) {
                    (customActionBar!!.parent as ViewGroup?)?.removeView(customActionBar)
                    containerViewBack.addView(customActionBar)
                }
                onResume()

                currentFragment()?.prepareFragmentToSlide(true, true)
                prepareFragmentToSlide(false, true)
            }
        }
    }

    private val rect = Rect()

    private fun findScrollingChild(parent: ViewGroup, x: Float, y: Float): View? {
        parent.forEach { child ->
            child.apply {
                if (visibility != VISIBLE) {
                    return@forEach
                }
                getHitRect(rect)
                if (rect.contains(x.toInt(), y.toInt())) {
                    if (canScrollHorizontally(-1)) {
                        return this
                    } else if (this is ViewGroup) {
                        findScrollingChild(this, x - rect.left, y - rect.top)?.let {
                            return it
                        }
                    }
                }
            }
        }
        return null
    }

    fun init(stack: ArrayList<BaseFragment<*>>) {
        fragmentStack = stack
        containerViewBack = LayoutContainer(context).apply {
            this@ActionBarLayout.addView(this)
            layoutParams = layoutParams.apply {
                (this as LayoutParams)
                width = LayoutParams.MATCH_PARENT
                height = LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP and Gravity.START
            }
        }

        containerView = object : LayoutContainer(context) {

        }.apply {
            this@ActionBarLayout.addView(this)
            layoutParams = layoutParams.apply {
                (this as LayoutParams)
                width = LayoutParams.MATCH_PARENT
                height = LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP and Gravity.START
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        fragmentStack.forEach {
            it.onConfigurationChanged(newConfig)
        }
    }

    fun addFragmentToStack(fragment: BaseFragment<*>, position: Int) {
        if (position == -1) {
            if (!fragmentStack.isEmpty()) {
                lastFragment()?.apply {
                    onPause()
                    if (mBinding != null) {
                        onRemoveFromParent()
                        val fragmentView = fragmentView()
                        val parent = fragmentView?.parent
                        if (parent != null) {
                            if (parent is ViewGroup) {
                                parent.removeView(fragmentView)
                            }
                        }
                    }
                }
            }
            fragmentStack.add(fragment)
        } else {
            fragmentStack.add(position, fragment)
        }
    }

    override fun hasOverlappingRendering(): Boolean {
        return false
    }

    fun closeLastFragment(animated: Boolean = true) {
        if (fragmentStack.isEmpty()) {
            return
        }

        hideKeyboard(parentActivity().currentFocus)
        innerTranslationX = 0f

        val previousFragment = previousFragment()
        val currentFragment = currentFragment()!!

        if (previousFragment != null) {
            previousFragment.apply {
                val temp = containerView
                containerView = containerViewBack
                containerViewBack = temp

                setParentLayout(this@ActionBarLayout)

                if (mBinding == null) {
                    createView()
                }
                val fragmentView = fragmentView()!!
                containerView.visibility = View.VISIBLE

                (fragmentView.parent)?.apply {
                    (this as ViewGroup)
                    onRemoveFromParent()
                    removeView(fragmentView)
                }
                containerView.addView(fragmentView)
                fragmentView.layoutParams =
                    (fragmentView.layoutParams as ViewGroup.LayoutParams).apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }

                newFragment = previousFragment
                oldFragment = currentFragment

                onTransitionAnimationStart(true, true)
                currentFragment.onTransitionAnimationStart(false, true)
                onResume()

                if (!hasOwnBackground) {
                    fragmentView.setBackgroundColor(Color.WHITE)
                }

                if (!animated) {
                    closeLastFragmentInternalRemoveOld(currentFragment)
                }

                if (animated) {
                    transitionAnimationStartTime = System.currentTimeMillis()
                    transitionAnimationInProgress = true
                    layoutToIgnore = containerView
                    val previousFragmentFinal = this
                    onCloseAnimationEndRunnable = Runnable {
                        containerViewBack.translationX = 0f

                        currentFragment.apply {
                            closeLastFragmentInternalRemoveOld(this)
                            onTransitionAnimationEnd(false, true)
                        }

                        previousFragmentFinal.apply {
                            onTransitionAnimationEnd(true, true)
                            onBecomeFullyVisible()
                        }
                    }
                    startLayoutAnimation(false, true)
                } else {
                    currentFragment.onTransitionAnimationEnd(false, true)
                    onTransitionAnimationEnd(true, true)
                    onBecomeFullyVisible()
                }
            }
        } else {
            removeFragmentFromStackInternal(currentFragment)
        }

    }

    fun removeFragmentFromStackInternal(fragment: BaseFragment<*>) {
        fragment.apply {
            onPause()
            onFragmentDestroy()
            setParentLayout(null)
            fragmentStack.remove(fragment)
        }
    }

    fun dismissDialogs() {
        lastFragment()?.dismissAllDialogs()
    }

    fun lastFragment(): BaseFragment<*>? =
        if (fragmentStack.isNotEmpty())
            fragmentStack.last()
        else
            null

    fun onResume() {
        lastFragment()?.onResume()
    }

    fun onPause() {
        lastFragment()?.onPause()
    }
}