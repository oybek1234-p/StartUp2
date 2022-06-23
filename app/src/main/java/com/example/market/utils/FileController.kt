package com.example.market.utils
//
//import android.os.Build
//import android.os.Environment
//import android.util.SparseArray
//import com.example.market.MyApplication
//import com.example.market.SharedConfig
//import com.example.market.viewUtils.toast
//import java.io.File
//import java.io.FileWriter
//
//class FilesController {
//
//    companion object {
//        const val MEDIA_DIR_IMAGE = 0
//        const val MEDIA_DIR_VIDEO = 1
//        const val MEDIA_DIR_MUSIC = 2
//        const val MEDIA_DIR_DOCUMENT = 3
//
//        private var INSTANCE: FilesController?=null
//        fun getInstance(): FilesController{
//            return if (INSTANCE!=null) INSTANCE!! else FilesController().also {
//                INSTANCE = it
//            }
//        }
//    }
//    var startUpPath: File?=null
//    var paths = SparseArray<File>()
//
//    fun createMediaPaths() {
//        try {
//            if (Environment.MEDIA_MOUNTED==Environment.getExternalStorageState()) {
//                toast("Not mounted")
//                val path = Environment.getExternalStorageDirectory()
//                startUpPath = if (Build.VERSION.SDK_INT >=30) {
//                    val newPath = MyApplication.appContext.getExternalFilesDir(null)
//                    File(newPath,"StartUp")
//                } else {
//                    File(path,"StartUp")
//                }
//                startUpPath?.apply {
//                    toast("StartUpPath not null")
//                    mkdirs()
//                    if (isDirectory) {
//                        toast("Create startup images dir")
//                        val imageDir = File(startUpPath,"StartUp Images")
//                        imageDir.mkdirs()
//
//                        if (imageDir.isDirectory) {
//                            paths.put(MEDIA_DIR_IMAGE,imageDir)
//                        }
//
//                        val videoDir = File(startUpPath,"StartUp Videos")
//                        videoDir.mkdirs()
//
//                        if (videoDir.isDirectory) {
//                            paths.put(MEDIA_DIR_VIDEO,videoDir)
//                        }
//
//                        val musicDir = File(startUpPath,"StartUp Music")
//                        musicDir.mkdirs()
//
//                        if (musicDir.isDirectory) {
//                            paths.put(MEDIA_DIR_MUSIC,musicDir)
//                        }
//
//                        val documentDir = File(startUpPath,"StartUp Documents")
//                        documentDir.mkdirs()
//
//                        if (documentDir.isDirectory) {
//                            paths.put(MEDIA_DIR_DOCUMENT,documentDir)
//                        }
//                    }
//                }
//                checkSaveToGallery()
//            }
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//
//    fun getFileType(dir: Int): String {
//        return when(dir) {
//            MEDIA_DIR_VIDEO -> ".mp4"
//            MEDIA_DIR_IMAGE -> ".jpeg"
//            MEDIA_DIR_MUSIC -> ".mp3"
//            MEDIA_DIR_DOCUMENT -> ".doc"
//            else -> ""
//        }
//    }
//
//    fun createNewFile(directory: Int,name: String?=null): File? {
//        try {
//            return File(paths[directory],name ?: System.currentTimeMillis().toString()+getFileType(directory))
//        }catch (e: java.lang.Exception){
//
//        }
//        return null
//    }
//
//    fun createEmptyFile(f: File) {
//        try {
//            if (f.exists()) {
//                return
//            }
//            val writer = FileWriter(f)
//            writer.flush()
//            writer.close()
//        } catch (e: Throwable) {
//            FileLog.e(e)
//        }
//    }
//
//    fun checkSaveToGallery() {
//        try {
//            val startUpPath = File(Environment.getExternalStorageDirectory(),"StartUp")
//            startUpPath.mkdir()
//            val videoDir = File(startUpPath,"StartUp Videos")
//            videoDir.mkdir()
//            val imageDir = File(startUpPath,"StartUp Images")
//            imageDir.mkdir()
//            SharedConfig.getInstance().saveToGallery.let {
//                if (videoDir.isDirectory) {
//                    File(videoDir,".nomedia").apply {
//                        if (it) {
//                            delete()
//                        } else {
//                            createEmptyFile(this)
//                        }
//                    }
//                }
//                if (imageDir.isDirectory) {
//                    File(imageDir,".nomedia").apply {
//                        if (it) {
//                            delete()
//                        } else {
//                            createEmptyFile(this)
//                        }
//                    }
//                }
//            }
//        }catch (e: Exception) {
//
//        }
//    }
//}