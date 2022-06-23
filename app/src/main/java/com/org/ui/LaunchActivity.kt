package com.org.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.market.R
import com.example.market.databinding.ActivityMainBinding
import com.google.firebase.database.ValueEventListener
import com.org.market.*
import com.org.net.NotificationCenterDelegate
import com.org.net.addObserver
import com.org.net.newMessageDidLoad
import com.org.ui.actionbar.*
import com.org.ui.components.appInflater
import com.org.ui.components.visibleOrGone


class LaunchActivity : AppCompatActivity(), NotificationCenterDelegate {
    lateinit var binding: ActivityMainBinding
    var alertDialog: AlertDialog? = null

    fun actionBarLayout() = binding.actionBarLayout

    companion object {
        private var mainFragmentsStack: ArrayList<BaseFragment<*>> = ArrayList()
        fun fragmentsCount() = mainFragmentsStack.size

        fun clearFragments() {
            mainFragmentsStack.apply {
                forEach { it.onFragmentDestroy() }
                clear()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationLoader.postInitApplication()
        checkDisplaySize(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(R.drawable.transparent)
        appInflater = LayoutInflater.from(this)
        super.onCreate(savedInstanceState)

        fillStatusBarHeight(this)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this)).apply {
            if (currentTheme == 0) {
                currentTheme = currentThemeInfo.themeResId
            } else {
                themes.find { currentTheme == it.themeResId }?.themeResId?.let {
                    currentTheme = it
                }
            }
            setTheme(currentTheme)
            setLightStatusBar(true)
            setContentView(root)
            executePendingBindings()
        }

        actionBarLayout().apply {
            init(mainFragmentsStack)
            updateBadgeColor()
            setUpWithBottomNav(binding.bottomNavigationView)
        }
        initUser()
    }

    private var themeBitmap: Bitmap? = null
    private var themeCanvas: Canvas? = null

    private var themeAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
        duration = 400
        interpolator = decelerateInterpolator

        doOnCancel { onThemeAnimationEnd() }
        doOnEnd { onThemeAnimationEnd() }

        addUpdateListener {
            window.decorView.foreground?.apply {
                alpha = (255 * it.animatedValue as Float).toInt()
            }
        }
    }

    fun onThemeAnimationEnd() {
        window.decorView.foreground = null
        themeBitmap?.recycle()
        themeBitmap = null
        themeCanvas = null
    }

    fun prepareForThemeChange() {
        val decorView = window.decorView
        Bitmap.createBitmap(decorView.width, decorView.height, Bitmap.Config.ARGB_8888)?.apply {
            themeBitmap = this
            themeCanvas = Canvas(this)
            decorView.draw(themeCanvas)
            decorView.foreground = toDrawable(resources)
        }
    }

    fun applyTheme(themeId: Int) {
        if (themeAnimator.isRunning) return
        val newThemeInfo = themes.find { it.themeResId == themeId }
        if (newThemeInfo != null) {
            if (newThemeInfo != currentThemeInfo) {
                currentTheme = themeId
                currentThemeInfo = newThemeInfo
                prepareForThemeChange()
                setTheme(newThemeInfo.themeResId)
                updateBadgeColor()
                binding.invalidateAll()
                mainFragmentsStack.forEach { it.invalidateViewBindings() }
                themeAnimator.start()
            }
        }
    }

    fun setLightStatusBar(light: Boolean) {
        WindowCompat.getInsetsController(window,window.decorView)!!.isAppearanceLightStatusBars = light
        window.statusBarColor = if (light) Color.WHITE else getThemeColor(R.attr.colorPrimaryVariant)
    }

    fun initUser() {
        DataController.getUserFull(currentUserId(), true)
        startMessageSnapshot()
    }

    override fun onBackPressed() {
        if (fragmentsCount() > 1) {
            mainFragmentsStack.last().onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    var mChangeEventListener: ValueEventListener?=null

    var messagesChangedCallbacks = arrayListOf<DataController.MessageChangeCallback>()

    fun startMessageSnapshot() {
        mChangeEventListener = DataController.startMessageSnapshot(currentUserId()) { ms->
            messagesChangedCallbacks.forEach {
                it.onChanged(ms)
            }
            setBadgeCount(ms.unreadMessages)
        }
    }

    fun isBottomSheetVisible() = binding.bottomNavigationView.isVisible

    fun showBottomNav(show: Boolean) {
        if (isBottomSheetVisible() == show) return
        binding.bottomNavigationView.apply {
            visibleOrGone(true)
            binding.bottomShadow.visibleOrGone(false)
            animate()
                .alpha(if (show) 1f else 0.5f)
                .translationY(if (show) 0f else measuredHeight.toFloat())
                .setDuration(if (show) 200 else 150)
                .setInterpolator(if (show) overshootInterpolator else accelerateInterpolator)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        if (!show) {
                            visibleOrGone(false)
                            binding.bottomShadow.visibleOrGone(false)
                        }
                    }
                })
                .start()
        }
    }

    private var badgeBackgroundColor = 0
    private var badgeTextColor = 0
    private var badgeCount = 0

    fun updateBadgeColor() {
        badgeBackgroundColor = getThemeColor(R.attr.colorSecondary)
        badgeTextColor = getThemeColor(R.attr.colorOnSecondaryHigh)
        setBadgeCount(badgeCount)
    }

    fun setBadgeCount(count: Int) {
        badgeCount = count
        binding.bottomNavigationView.getOrCreateBadge(R.id.messages).apply {
            val visible = count > 0
            backgroundColor = badgeBackgroundColor
            badgeTextColor = this@LaunchActivity.badgeTextColor
            this.number = badgeCount
            isVisible = visible
        }
    }

    fun clearBadge() {
        setBadgeCount(0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionController.getInstance().onPermissionResult(requestCode, grantResults)
    }

    fun presentFragment(fragment: BaseFragment<*>,removeLast: Boolean) {
        actionBarLayout().presentFragment(fragment,removeLast,true)
    }

    override fun onDestroy() {
        Glide.get(this).clearMemory()
        super.onDestroy()
    }
}