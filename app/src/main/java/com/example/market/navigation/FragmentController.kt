package com.example.market.navigation

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Trace
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.example.market.*
import com.example.market.R.id.*
import com.example.market.cart.KorzinaFragment
import com.example.market.cart.ZakazlarFragment
import com.example.market.home.HomeFragment
import com.example.market.favourite.FavouriteFragment
import com.example.market.messages.MessagesFragment
import com.example.market.profile.ProfileFragmentClient
import com.example.market.profile.ProfileFragmentSeller
import com.example.market.search.SearchFragment
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.getDrawable
import com.example.market.utils.log
import com.example.market.viewUtils.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
 class FragmentController(val context: MainActivity) {
    var fragmentManager = context.supportFragmentManager

    var fragmentContainer: FrameLayout = object : FrameLayout(context){
        private var layerShadowDrawable = getDrawable(R.drawable.layer_shadow)

        override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
            return (context as MainActivity).fragmentController!!.onTouchEvent(ev)
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            val touch = (context as MainActivity).fragmentController!!.onTouchEvent(event)
            return !touch
        }

        init {
            setWillNotDraw(false)
            id = 6546
        }

        override fun onDraw(canvas: Canvas?) {
            if (childTranslationX!=0){
                layerShadowDrawable?.apply {
                    val alpha = Math.max(0f, Math.min((width - childTranslationX) / AndroidUtilities.dp(20f)
                        .toFloat(), 1.0f))

                    setBounds((childTranslationX - intrinsicWidth).toInt(),
                        top,
                        childTranslationX.toInt(),
                        bottom)
                    setAlpha((0xff * alpha).toInt())
                    this.draw(canvas!!)
                }
            }
        }
    }

    private var container = fragmentContainer
    private var maybeStartTracking = false
    private var startedTracking = false
    private var startedTrackingX = 0
    private var startedTrackingY = 0
    private var animationInProgress = false
    private var velocityTracker: VelocityTracker? = null
    private var beginTrackingSent = false
    private var startedTrackingPointerId = -1
    private var childTranslationX = 0

    private var currentFragmentDestination: Int = -1
    private var previousFragmentDestination:Int = -1
    var graphIds = ArrayList<String>()

    companion object {
        val defaultAnim = arrayOf(
            R.anim.nav_default_enter_anim,
            R.anim.nav_default_exit_anim,
        )
        val openSearchFragment = arrayOf(R.anim.anim_right_to_left, R.anim.anim_right_to_left_close)
        val decelerateInterpolator = DecelerateInterpolator(1.5f)
        val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()
    }

    init {

        setUpBottomNavigation()
        setSelectedItemId(home)
    }

    private fun setSelectedItemId(id: Int) {
        context.bottomNavigationView.selectedItemId = id
    }

    fun closePreviousFragment() {

        fragmentManager.apply {
            val fragment = fragments[fragments.size-2]
            beginTransaction().remove(fragment).commit()
            fragments.remove(fragment)
            toast("Removing ${fragment.javaClass.name} fragment")
        }
    }

    fun removeIfContainsAlready(fragment: Fragment) {
        fragmentManager.beginTransaction().remove(fragment).commit()
    }

    private fun setUpBottomNavigation() {

        container.isClickable = true
        container.isFocusable = true
        (context).bottomNavigationView.menu.getItem(2).isEnabled = false
        (context).bottomNavigationView.setOnItemSelectedListener {
            previousFragmentDestination = currentFragmentDestination
            currentFragmentDestination = it.itemId

            if (previousFragmentDestination==currentFragmentDestination){
                return@setOnItemSelectedListener true
            }

            var currentFragment: BaseFragment?= findFrgamentByTag(currentFragmentDestination.toString())
            val previousFragment: BaseFragment?= findFrgamentByTag(previousFragmentDestination.toString())

            fragmentManager.apply {
                beginTransaction().apply {
                    previousFragment?.apply {
                        onViewDetachedFromParent()
                        onViewFullyHiden()
                        hide(this)
                    }
                    if (currentFragment==null) {
                        currentFragment = createFragment(currentFragmentDestination)
                        currentFragment?.apply {
                            add(container.id,this,currentFragmentDestination.toString())
                            baseFragmentLifecyle = object : BaseFragmentLifecyle {
                                override fun onViewCreated(view: View) {
                                    onViewAttachedToParent()
                                    onViewFullyVisible()
                                }
                            }
                        }
                    } else {
                        currentFragment?.apply {
                            show(this)
                            onViewAttachedToParent()
                            onViewFullyVisible()
                        }
                    }
                    resetGraphState()
                    commit()
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun createFragment(id: Int): BaseFragment? {
        var fragment: BaseFragment?=null
        when(id) {
            home -> {
                fragment = HomeFragment()
            }
            sevimli -> {
                fragment = FavouriteFragment()
            }
            korzina -> {
                fragment = KorzinaFragment()
            }
            produktlar -> {
                fragment = ProduktlarFragment()
            }
            messages -> {
                fragment = MessagesFragment()
            }
            zakazlar -> {
                fragment = ZakazlarFragment()
            }
            profile -> {
                fragment = ProfileFragmentSeller()
            }
        }
        return fragment
    }
    fun findFrgamentByTag(id: String): BaseFragment? = fragmentManager.findFragmentByTag(id) as BaseFragment?

    fun changeUserBottomNav(who: String) {
        return
        context.bottomNavigationView.apply {
            when(who) {
                USER_SELLER -> {
                    changeNavItem(
                        this,
                        sevimli,
                        produktlar,
                        R.drawable.product_icon,
                        context.getString(R.string.produktlar)
                    )
                    changeNavItem(
                        this,
                        korzina,
                        zakazlar,
                        R.drawable.zakaz_icon,
                        context.getString(R.string.zakazlar)
                    )
                    changeNavItem(
                        this,
                        messages,
                        messages,
                        R.drawable.profile_newmsg,
                        context.getString(R.string.messages)
                    )
                    changeNavItem(
                        this,
                        profile,
                        profile,
                        R.drawable.search_users,
                        context.getString(R.string.profile)
                    )
                }
                USER_CLIENT -> {
                    changeNavItem(
                        this,
                        produktlar,
                        sevimli,
                        R.drawable.heart_icon,
                        context.getString(R.string.sevimli)
                    )
                    changeNavItem(
                        this,
                        zakazlar,
                        korzina,
                        R.drawable.cart_icon,
                        context.getString(R.string.korzina)
                    )
                    changeNavItem(
                        this,
                        messages,
                        messages,
                        R.drawable.profile_newmsg,
                        context.getString(R.string.messages)
                    )
                    changeNavItem(
                        this,
                        profile,
                        profile,
                        R.drawable.search_users,
                        context.getString(R.string.profile)
                    )
                }
            }
        }
    }

    private fun resetGraphState(){
        graphIds.clear()
        graphIds.add(currentFragmentDestination.toString())
    }
    fun findFragmentById(id: String) {

    }
    fun presentFragmentRemoveLast(fragment: BaseFragment?, anim: Array<Int>, removeLast: Boolean) {
        if (fragment==null) return
        if (fragment.tag == currentFragmentDestination.toString()){
            toast(context,"Cant show fragment on main graph")
            return
        }
        val id = System.currentTimeMillis().toString()

        fragmentManager.apply {
           val currentFragment = if (graphIds.size>0) findFrgamentByTag(graphIds[graphIds.size-1]) else null
            var bundle: Any?=null
            currentFragment?.let {
                bundle = it.bundleAny
            }
            fragment.apply {
                bundle?.let {
                log("Bundle not null")
                    bundleAny = bundle
                }
                beginTransaction().apply {
                    val trace = System.currentTimeMillis()
                    val anim0 = AnimationUtils.loadAnimation(container.context,anim[0]).apply {
                        interpolator = decelerateInterpolator
                    }

                    val anim1 = AnimationUtils.loadAnimation(container.context,anim[1]).apply {
                        interpolator = accelerateDecelerateInterpolator
                    }

                    fragment.id = id
                    graphIds.add(id)
                    add(container.id,fragment,id)

                    baseFragmentLifecyle = object : BaseFragmentLifecyle {
                        override fun onViewCreated(view: View) {
                            log("fragment background is not null")
                            view.doOnNextLayout {
                                currentFragment?.let {
                                    onViewAttachedToParent()

                                    if (view.background == null) { view.setBackgroundColor(Color.WHITE) }
                                    container.bringChildToFront(view)
                                    anim0.setAnimationListener(object : Animation.AnimationListener{
                                        override fun onAnimationEnd(animation: Animation?) {
                                            onViewFullyVisible()
                                        }

                                        override fun onAnimationRepeat(animation: Animation?) {

                                        }

                                        override fun onAnimationStart(animation: Animation?) {

                                        }
                                    })
                                    if (it.view!=null) {
                                        anim1.setAnimationListener(object : Animation.AnimationListener{
                                            override fun onAnimationEnd(animation: Animation?) {
                                                it.onViewFullyHiden()
                                                beginTransaction().apply {
                                                    if (removeLast){
                                                        remove(it)
                                                        fragments.remove(it)
                                                        graphIds.removeAt(graphIds.size-2)
                                                    } else {
                                                        hide(it)
                                                    }
                                                    commit()
                                                }
                                            }
                                            override fun onAnimationRepeat(animation: Animation?) {

                                            }
                                            override fun onAnimationStart(animation: Animation?) {

                                            }
                                        })
                                        it.onViewDetachedFromParent()
                                    }
                                    view.startAnimation(anim0)
                                    currentFragment.requireView().startAnimation(anim1)
                                    baseFragmentLifecyle = null
                            } }

                        }

                    }
                    commit()
                }
            }
        }
    }


    fun closeLastFragment(anim: Array<Int> = defaultAnim) {
        if (graphIds.size<1) return

        fragmentManager.apply {
            val pTag = graphIds[graphIds.size-2]
            val previousFragment = findFrgamentByTag(pTag)
            if (currentFragmentDestination.toString()==pTag) {
                bottomNavVisiblity(context,true)
            }
            previousFragment?.apply {
                val currentFragment = findFrgamentByTag(graphIds[graphIds.size-1]) as BaseFragment
                val bundleResult: Any?=currentFragment.bundleAny

                beginTransaction().apply {
                        val animSet0 = AnimationUtils.loadAnimation(context,anim[0]).apply {
                            interpolator = decelerateInterpolator
                            setAnimationListener(object : Animation.AnimationListener{
                                override fun onAnimationEnd(animation: Animation?) {
                                    onViewFullyVisible()
                                }

                                override fun onAnimationRepeat(animation: Animation?) {

                                }

                                override fun onAnimationStart(animation: Animation?) {

                                }
                            })
                        }
                        baseFragmentLifecyle = null

                        show(previousFragment)

                        currentFragment.apply {
                            previousFragment.bundleAny = bundleResult
                            previousFragment.onViewAttachedToParent()
                            onViewDetachedFromParent()
                        }
                        val animSet1 = AnimationUtils.loadAnimation(context,anim[1])
                                .apply {
                                    interpolator = accelerateDecelerateInterpolator
                                    this.setAnimationListener(object : Animation.AnimationListener{
                                        override fun onAnimationStart(animation: Animation?) {
                                            currentFragment
                                                .requireView()
                                                .apply {
                                                    if (background == null) {
                                                        setBackgroundColor(Color.WHITE)
                                                    }
                                                    container.bringChildToFront(this)
                                                }
                                        }
                                        override fun onAnimationEnd(animation: Animation?) {
                                            currentFragment.let {
                                                currentFragment.onViewFullyHiden()
                                                beginTransaction()
                                                    .remove(it)
                                                    .commit()
                                                fragments.remove(it)
                                            }
                                        }
                                        override fun onAnimationRepeat(animation: Animation?) {

                                        }
                                    })
                                }
                        view?.startAnimation(animSet0)
                        currentFragment.view?.startAnimation(animSet1)
                        graphIds.removeAt(graphIds.size - 1)
                        commit()
                    }
            }
        }
    }

    private var scrimDrawable: ColorDrawable = ColorDrawable()

    fun onTouchEvent(ev: MotionEvent?): Boolean {

            if (graphIds.size > 1) {
                val currentFragment: BaseFragment = findFrgamentByTag(graphIds[graphIds.size-1]) as BaseFragment
                val previousFragment = findFrgamentByTag(graphIds[graphIds.size-2]) as BaseFragment
                if (ev != null && ev.action == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking) {

                    if (!currentFragment.isSwapBackEnabled()) {
                        maybeStartTracking = false
                        startedTracking = false
                        toast("Return false")
                        return false
                    }

                    startedTrackingPointerId = ev.getPointerId(0)
                    maybeStartTracking = true
                    startedTrackingX = ev.x.toInt()
                    startedTrackingY = ev.y.toInt()
                    if (velocityTracker != null) {
                        velocityTracker?.clear()
                    }
                } else if (ev != null && ev.action == MotionEvent.ACTION_MOVE && ev.getPointerId(0) == startedTrackingPointerId) {
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain()
                    }
                    val dx = max(0, (ev.x - startedTrackingX).toInt())
                    val dy = abs(ev.y.toInt() - startedTrackingY)
                    val minusSpace = getPixelsInCM(0.4f)
                    velocityTracker!!.addMovement(ev)
                    if (maybeStartTracking && !startedTracking && dx >= minusSpace && abs(dx) / 3 > dy
                    ) {
                        if (currentFragment.canBeginSlide()) {
                            toast("Started tracking")
                            startedTracking = true
                            prepareForMoving(ev)
                        } else {
                            maybeStartTracking = false
                        }
                    } else if (startedTracking) {

                        if (!beginTrackingSent) {
                            if (context.currentFocus != null) {
                                context.closeKeyboard(context.currentFocus as EditText)
                            }
                            currentFragment.onBeginSlide()
                            beginTrackingSent = true
                        }
                        currentFragment.view?.apply {

                            currentFragment.view?.let {
                                if (it is ViewGroup){
                                    container.requestDisallowInterceptTouchEvent(true)
                                }
                            }
                            val x = dx.toFloat()-minusSpace
                                this.translationX = x
                                childTranslationX = x.toInt()
                                previousFragment.view?.apply {
                                    var opacity = min(0.8f, (currentFragment.requireView().width - x) / width.toFloat())
                                    if (opacity < 0) {
                                        opacity = 0f
                                    }

                                    scrimDrawable.color = ((-0x67000000 and -0x1000000 ushr 24) * opacity).toInt() shl 24
                                    foreground = scrimDrawable

                                }
                        }
                    }
                } else if (ev != null && ev.getPointerId(0) == startedTrackingPointerId && (ev.action == MotionEvent.ACTION_CANCEL || ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_POINTER_UP)) {
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain()
                    }
                    velocityTracker?.computeCurrentVelocity(1000)
                    if (!startedTracking && currentFragment.isSwapBackEnabled()
                    ) {
                        val velX = velocityTracker!!.xVelocity
                        val velY = velocityTracker!!.yVelocity
                        if (velX >= 3500 && velX > abs(velY) && currentFragment.canBeginSlide()) {
                            prepareForMoving(ev)
                            if (!beginTrackingSent) {
                                if (context.currentFocus != null) {
                                    context.closeKeyboard(context.currentFocus as EditText)
                                }
                                beginTrackingSent = true
                            }
                        }
                    }
                    if (startedTracking&&currentFragment.view!=null&&previousFragment.view!=null) {
                        val currentFragmentView = currentFragment.requireView()
                        val x: Float = currentFragmentView.x
                        val animatorSet = AnimatorSet()
                        val velX = velocityTracker!!.xVelocity
                        val velY = velocityTracker!!.yVelocity
                        val backAnimation =
                            x < currentFragmentView.measuredWidth / 3.0f && (velX < 3500 || velX < velY)
                        var distToMove: Float = 0f
                        val objectAnimator = ObjectAnimator()

                        if (!backAnimation) {
                            objectAnimator.apply {
                                target = currentFragmentView
                                setProperty(View.TRANSLATION_X)
                                setFloatValues(currentFragmentView.measuredWidth.toFloat())
                            }
                            distToMove = currentFragmentView.measuredWidth - x
                        }else {
                            distToMove = x
                            objectAnimator.apply {
                                target = currentFragmentView
                                setProperty(View.TRANSLATION_X)
                                setFloatValues(0f)
                            }
                        }
                        animatorSet.playTogether(
                            objectAnimator
                        )
                        objectAnimator.addUpdateListener {

                            previousFragment.view?.apply {
                                val w = currentFragmentView.width
                                var opacity = min(0.8f, (w - currentFragment.requireView().translationX) / w.toFloat())
                                if (opacity < 0) {
                                    opacity = 0f
                                }

                                scrimDrawable.color = ((-0x67000000 and -0x1000000 ushr 24) * opacity).toInt() shl 24
                                foreground = scrimDrawable
                            }
                        }
                        animatorSet.duration = max((200.0f / currentFragmentView.measuredWidth * distToMove).toInt(),
                                50).toLong()
                        animatorSet.doOnEnd {
                            previousFragment.view?.foreground = null
                        }
                        animatorSet.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animator: Animator) {
                                onSlideAnimationEnd(backAnimation)
                            }
                        })
                        animatorSet.start()
                        animationInProgress = true
                    } else {
                        maybeStartTracking = false
                        startedTracking = false
                    }
                    if (velocityTracker != null) {
                        velocityTracker?.recycle()
                        velocityTracker = null
                    }
                } else if (ev == null) {
                    maybeStartTracking = false
                    startedTracking = false
                    if (velocityTracker != null) {
                        velocityTracker?.recycle()
                        velocityTracker = null
                    }
                }
            }
            return startedTracking
        }

    fun onSlideAnimationEnd(backAnim: Boolean) {
        val lastFragment =  findFrgamentByTag(graphIds[graphIds.size-1]) as BaseFragment
        val previousFragment = findFrgamentByTag(graphIds[graphIds.size-2]) as BaseFragment
        lastFragment.view?.parent?.requestDisallowInterceptTouchEvent(false)
        childTranslationX = 0

        fragmentManager
            .beginTransaction()
            .apply {
                if (!backAnim) {
                    lastFragment.onViewFullyHiden()
                    lastFragment.onViewDetachedFromParent()
                    previousFragment.onViewFullyVisible()
                    remove(lastFragment)
                    fragmentManager.fragments.remove(lastFragment)
                    graphIds.removeAt(graphIds.size-1)
                    if (previousFragment.tag==currentFragmentDestination.toString()) {
                        bottomNavVisiblity(context,true)
                    }
                }else{
                    hide(previousFragment)
                    previousFragment.onViewFullyHiden()
                    previousFragment.onViewDetachedFromParent()
                }
                commit()
            }
        startedTracking = false
        animationInProgress = false

    }
    private fun prepareForMoving(ev:MotionEvent){
        maybeStartTracking = false
        startedTracking = true
        beginTrackingSent = false

        if (graphIds.size>1){
            val currentFragment: BaseFragment = findFrgamentByTag(graphIds[graphIds.size-1]) as BaseFragment
            val previousFragment = findFrgamentByTag(graphIds[graphIds.size-2]) as BaseFragment
            if (currentFragment.view==null&&previousFragment.view==null) return
            val currentTimeMilis = System.currentTimeMillis()
            fragmentManager.apply {

                beginTransaction().apply {
                    previousFragment.onViewAttachedToParent()
                    show(previousFragment)
                    commit()
                }

                log((System.currentTimeMillis() - currentTimeMilis).toString())
                currentFragment.view?.apply {
                    if (background==null) {
                        setBackgroundColor(Color.WHITE)
                    }
                    container.bringChildToFront(this)
                }
            }
        }
    }

}
