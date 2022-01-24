package com.example.market.camera

import android.R.attr
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Bundle
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.example.market.BaseFragment
import com.example.market.MyApplication
import com.example.market.R
import com.example.market.binding.inflateBinding
import com.example.market.binding.load
import com.example.market.databinding.UploadVideoFragmentBinding
import com.example.market.navigation.bottomNavVisiblity
import com.example.market.utils.AndroidUtilities
import com.example.market.utils.CountDownHelper
import com.example.market.utils.FilesController
import com.example.market.utils.log
import com.example.market.viewUtils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.app.Activity
import android.hardware.SensorManager
import android.view.*
import android.R.attr.orientation
import android.graphics.Matrix
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.view.OrientationEventListener.ORIENTATION_UNKNOWN
import com.bumptech.glide.Glide
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import kotlinx.coroutines.GlobalScope

class CameraFragment: BaseFragment() {

    companion object {
        const val TAG = "CameraView"
    }

    var binding: UploadVideoFragmentBinding?=null

    private var facing: Facing = Facing.BACK

    private var outPutFile: File?=null

    private var cameraMode = Mode.VIDEO
    set(value) {
            field = value
            cameraView?.mode = value
    }

    private var cameraView: CameraView?=null

    private var updateProgressListener = object : CountDownHelper.CountDownInterface {
        override fun onProgress(millisInFuture: Long, lastMillis: Long, progress: Int) {
            setProgress(progress)
        }

        override fun onFinish() {
            stopRecording()
        }
    }

    private var countDownTimer = CountDownHelper().apply {
        listener = updateProgressListener
    }

    fun getFile(): File? {
        return FilesController
            .getInstance()
            .createNewFile(
                if (cameraMode == Mode.VIDEO)
                    FilesController.MEDIA_DIR_VIDEO
                else
                    FilesController.MEDIA_DIR_IMAGE
            )?.also {
            if (!it.isDirectory) {
                outPutFile = it
                log("$TAG Get file")
            }
        }
    }

    fun startShutterAnimation(recording: Boolean){
        binding?.startRecordingButton?.isRecording = recording
    }

    private val cameraListener = object : CameraListener() {
        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()

            setProgress(0)
            countDownTimer.start(15000)

            startShutterAnimation(true)

            startAnimation(true)

        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()

            startShutterAnimation(false)
            startAnimation(false)
            countDownTimer.stop()
        }


        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)

            openVideoEditFragment()
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)

            getFile()?.let {
                result.toFile(it
                ) { Glide.with(requireContext()).load(it).into(binding!!.openGalleryView) }
            }

        }
    }

    fun capturePhoto() {
            try {
                cameraMode = Mode.PICTURE
                myToast("take picture")
                cameraView?.takePicture()

            }catch (e: Exception){
                throw e
            }
    }

    fun startRecording(file: File) {
        try {
            cameraMode = Mode.VIDEO
            cameraView?.takeVideo(file)

        } catch (e: Exception) {
            throw e
        }
    }

    fun stopRecording() {
            try {
                cameraView?.stopVideo()
            } catch (e: Exception) {
                throw e
            }
    }

    fun setProgress(progress: Int) {
        binding?.progressBar?.progress = progress
    }

    fun openVideoEditFragment() =
        run { outPutFile?.let {
            presentFragmentRemoveLast(VideoEditFragment(it),false)
        } }


    fun isTakingVideo() = cameraView?.isTakingVideo ?: false

    fun isTakingPicture() = cameraView?.isTakingPicture ?: false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = inflateBinding(container, R.layout.upload_video_fragment)

        bottomNavVisiblity(context,false)

        binding?.apply {
            this@CameraFragment.cameraView = cameraView

            closeView.setOnClickListener {
                closeLastFragment()
            }

            cameraView.apply {

                facing = this@CameraFragment.facing
                addCameraListener(cameraListener)

                startRecordingButton.setOnClickListener {
                    if (cameraMode == Mode.VIDEO) {
                        cameraView.apply {
                            if (isTakingVideo()) {
                                stopRecording()
                            } else {
                                getFile()?.let { startRecording(it) }
                            }
                        }
                    } else {
                        capturePhoto()
                    }
                }
            }

            changeCamera.setOnClickListener {
               facing = cameraView.toggleFacing()
            }

        }
        return binding?.root
    }

    private val ANIMATION_DURATION = 300L

    fun startAnimation(recording: Boolean) {
        binding?.apply {
            changeCamera.animate().setDuration(ANIMATION_DURATION).alpha(if (recording) 0f else 1f).start()
            openGalleryView.animate().setDuration(ANIMATION_DURATION).alpha(if (recording) 0f else 1f).start()
            openGalleryTextView.animate().setDuration(ANIMATION_DURATION).alpha(if (recording) 0f else 1f).start()
            closeView.animate().setDuration(ANIMATION_DURATION).alpha(if (recording) 0f else 1f).start()
            videoLimitCountView.animate().setDuration(ANIMATION_DURATION).alpha(if (recording) 0f else 1f).start()
        }
    }
    override fun onBeginSlide() {

    }

    override fun onConnectionChanged(state: Boolean) {

    }

    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
        myToast("onResume")
        if (cameraView?.isOpened == false&&isVisible) {
            myToast("OpenCamera")
            cameraView?.open()
        }
    }

    override fun onPause() {
        super.onPause()
        myToast("OnPause")
        if (cameraView?.isOpened == true) {
            myToast("Close camera")
            cameraView?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myToast("OnDestroy")
        cameraView?.destroy()
        cameraView = null
        outPutFile = null
        binding = null
    }

    fun myToast(message: String){
        toast("Camera view: $message")
//        log("CameraView: $message")
    }

    override fun onViewFullyVisible() {
        myToast("OnViewFullyVisible")
        if (cameraView?.isOpened == false) {
            myToast("Open camera")
            cameraView?.open()
        }
    }

    override fun onViewFullyHiden() {
        AndroidUtilities.runOnUIThread({
            if (cameraView?.isOpened == true) {
                myToast("Close camera")
                cameraView?.close()
            } },ANIMATION_DURATION)
    }

    override fun onViewAttachedToParent() {
        setProgress(0)
    }

    override fun onViewDetachedFromParent() {

    }
}