package com.example.market

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.market.binding.appInflater
import com.example.market.camera.CameraFragment
import com.example.market.model.Message
import com.example.market.model.Product
import com.example.market.navigation.FragmentController
import com.example.market.permission.PermissionController
import com.example.market.utils.AndroidUtilities
import com.example.market.viewUtils.presentFragmentRemoveLast
import com.example.market.viewUtils.toast
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.tasks.Task
import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.CancellableTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    lateinit var controller: NavController
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    var fragmentContainerView : FrameLayout?=null
    var selectedUriPhotos: ArrayList<String> ?= null
    private var connectivityChecker: ConnectivityChecker?=null
    var fragmentController: FragmentController?=null
    var isConnectedToInternet = false
    var result: Result?=null
    private lateinit var networkTextView: TextView
    private var lastAviable = false
    private lateinit var exoPlayerView: PlayerView

    fun onConnectionChanged(aviable: Boolean) {
        if (aviable==lastAviable) {
            return
        }
        lastAviable = aviable
        (window.decorView as ViewGroup).apply {
            val contains = children.contains(networkTextView)

            if (!contains && aviable) {
                return
            }

            if (!contains&&!aviable) {
                addView(networkTextView,ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            networkTextView.apply {
                if (!aviable) {
                    translationY = (-measuredHeight).toFloat()
                }
                animate().translationY((if (!aviable) 0f else -measuredHeight.toFloat())).setDuration(300).setUpdateListener {
                    if (aviable&&translationY == (-measuredHeight).toFloat()) {
                        removeView(this)
                        toast("Remove view")
                    }
                }.start()
            }
        }
    }

    private fun setWindowBackgroundColor(color: Int) {
        window.decorView.setBackgroundColor(color)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //bottomNavVisiblity()
        appInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        setWindowBackgroundColor(Color.rgb(241,241,241))
        exoPlayerView = findViewById(R.id.exoplayer_view)

//        val exoPlayer = ExoPlayer.Builder(this).build()
//        exoPlayerView.player = exoPlayer
//
//        val mediaItem = MediaItem.fromUri("https://ik.imagekit.io/startup/619242188_267086899_147161037653812_2968749196796257316_n_cnY19dVZ1.mp4?tr=h-200,w-150")
//
//        exoPlayer.apply {
//            addListener(object : Player.Listener{
//                override fun onIsPlayingChanged(isPlaying: Boolean) {
//                    super.onIsPlayingChanged(isPlaying)
//                    if (!isPlaying) {
//                        findViewById<FrameLayout>(R.id.card_view).animate().alpha(0f).setDuration(200).setUpdateListener {
//                            if (!it.isRunning) {
//                                exoPlayerView.visibility = View.GONE
//                            }
//                        }.start()
//                    }
//                }
//            })
//            setMediaItem(mediaItem)
//            prepare()
//            play()
//        }


        networkTextView = TextView(this).apply {
            text = "You have not Internet conncetion. Please turn on your internet and try again"
            setTextColor(Color.RED)
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            val padding = AndroidUtilities.dp(18f)
            setPadding(padding,padding,padding,padding)
            elevation = AndroidUtilities.dp(4f).toFloat()
        }

        MyApplication.internetAviableCallback.observe(this,{
            onConnectionChanged(it)
        })

        //this class handles connection status
        getSystemService<ConnectivityManager>()?.let {
            connectivityChecker = ConnectivityChecker(it)
            //we will set observer with livedata to get connection updates
            connectivityChecker?.connectedStatus?.observe(this,
                {
                        t ->
                    t?.let {
                        isConnectedToInternet = it
                    }
                })

            connectivityChecker?.startMonitoringConnectivity()
        }
        bottomNavigationView = findViewById(R.id.bottom_nav)

        bottomNavigationView.addView(ImageView(this).apply {
            setImageResource(R.drawable.msg_addbot)
            imageTintList = ColorStateList.valueOf(Color.BLACK)
            setOnClickListener {
                presentFragmentRemoveLast(this@MainActivity,CameraFragment(),false)
            }
        },FrameLayout.LayoutParams(120,120).apply {
            gravity = Gravity.CENTER
        })

        fragmentController = FragmentController(this).also {
            fragmentContainerView = it.fragmentContainer
            findViewById<FrameLayout>(R.id.base_container)?.addView(fragmentContainerView,FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT))
        }

        fragmentContainerView?.parent?.bringChildToFront(fragmentContainerView)

        updateBottomNav()

//        Glide.with(this).load("https://ik.imagekit.io/startup/JIANBUDAN-Plush-warm-Home-flat-slippers-Lightweight-soft-comfortable-winter-slippers-Women-s-cotton-shoes-Indoor_6UCifdo-7.jpg?tr=w-80,h-80,bl-10")
//            .into(object : SimpleTarget<Drawable>() {
//                override fun onResourceReady(
//                    resource: Drawable,
//                    transition: Transition<in Drawable>?
//                ) {
//                    toast("Thumbnail loaded")
//                    val bitmap = resource.toBitmap()
//                    val outPutByteArray = ByteArrayOutputStream()
//                    bitmap.compress(Bitmap.CompressFormat.WEBP,100,outPutByteArray)
//
//                    getProductsReference().document("1640447448681").update("thumbnailObject",Blob.fromBytes(outPutByteArray.toByteArray())).addOnCompleteListener {
//                        if (it.isSuccessful) {
//                            toast("Blob object uploaded successfully")
//                        }
//                    }
//                }
//            })
    }

    fun updateBottomNav() {
        fragmentController?.changeUserBottomNav(if (getUserIdentity()) USER_SELLER else USER_CLIENT)
        if (currentUser!=null) {
            startMessageSnapshot()
        }
    }

    companion object {
        const val container_id = R.id.container
    }

    var messageFragmetCallback:MessageCallback?=null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }
    fun setMessagesCountBadge(increment: Boolean,count: Int?=null) {
        bottomNavigationView.getOrCreateBadge(
            R.id.messages
        ).apply {
            if (count==-1) {
                clearNumber()
                backgroundColor = Color.TRANSPARENT
            } else {
                number = if (count!=null&&count>0) count else if (increment) number+1 else number-1
                backgroundColor = Color.RED
                badgeTextColor = Color.WHITE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionController.getInstance().onPermissionResult(requestCode,grantResults)
    }

    private fun startMessageSnapshot() {
       startMessagesSnapshot(object : MessageCallback{
           override fun onUnReadMessage(count: Int) {
               currentUser?.let { c ->
                   if (count == c.unreadMessages) {
                       return
                   }
                   val addedOrRemoved = count - c.unreadMessages

                   if (addedOrRemoved>0) {
                       setMessagesCountBadge(true)
                       val userMessages = MessagesController.getInstance().messages
                       if (userMessages.isNotEmpty()) {
                            getNewAddedMessage(addedOrRemoved,object : ResultCallback<ArrayList<Message>>{
                                override fun onSuccess(result: ArrayList<Message>?) {
                                    result?.forEach {
                                        userMessages.add(0,it)
                                        c.unreadMessages = count
                                    }
                                    MessagesController.getInstance().notifyObserver()
                                }
                            })
                       }
                   }
               }
           }

           override fun onFailed(message: String?) {

           }
       })
    }

    fun getNewAddedMessage(count: Int,resultCallback: ResultCallback<ArrayList<Message>>) {
        FirebaseFirestore
            .getInstance()
            .collection(USERS)
            .document(currentUser!!.id)
            .collection(USER_MESSAGES)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(count.toLong())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    resultCallback.onSuccess(parseDocumentSnapshot(it.result.documents,Message::class.java))
                } else {
                    resultCallback.onFailed()
                }
            }
    }

    lateinit var badge: BadgeDrawable

    override fun onBackPressed() {
        fragmentController?.closeLastFragment(
            anim = arrayOf(
                R.anim.anim_left_to_right,
                R.anim.anim_left_to_right_close
        ))
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun showKeyboard(view: View) {
        view.requestFocus()

        val im: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (!im.showSoftInput(if (currentFocus!=null) currentFocus!! else view,0)){
                if (!im.isActive(view)&&!im.isAcceptingText){
                    im.toggleSoftInput(0,0)
                }
            }
        }

    fun closeKeyboard(editText: EditText?=null){
        val im: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = editText ?: currentFocus
        if (view!=null) {
            im.hideSoftInputFromWindow(view.windowToken,0)
        }
    }
}