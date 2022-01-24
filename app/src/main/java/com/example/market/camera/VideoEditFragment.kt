package com.example.market.camera

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.market.BaseFragment
import com.example.market.R
import com.example.market.binding.inflateBinding
import com.example.market.databinding.VideoEditLayoutBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import java.io.File

class VideoEditFragment(val videoFilePath: File) : BaseFragment() {
    var binding: VideoEditLayoutBinding?=null
    var myPlayer: ExoPlayer?=null

    override fun onPause() {
        super.onPause()
        myPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        myPlayer?.stop()
        myPlayer = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflateBinding(container, R.layout.video_edit_layout)

        binding?.apply {
            videoPlayer.apply {

                myPlayer = ExoPlayer.Builder(requireContext()).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(videoFilePath))
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                    player = this
                }

            }
        }

        return binding?.root
    }

    override fun onBeginSlide() {

    }
    override fun onConnectionChanged(state: Boolean) {

    }

    override fun onBackPressed() {

    }

    override fun onViewFullyVisible() {
    }

    override fun isSwapBackEnabled(): Boolean {
        myPlayer?.stop()
        myPlayer?.release()
        return true
    }

    override fun onViewFullyHiden() {

    }

    override fun onViewAttachedToParent() {
        myPlayer?.play()
    }

    override fun onViewDetachedFromParent() {
        binding?.videoPlayer?.visibility = View.INVISIBLE
        myPlayer?.stop()
        myPlayer?.release()
    }

}