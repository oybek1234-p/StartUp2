package com.example.market.camera;


import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.example.market.MyApplication;

import java.util.ArrayList;
import java.util.List;

public class CameraSession {

    protected CameraInfo cameraInfo;
    private final Size pictureSize;
    private final Size previewSize;
    private String currentFlashMode;
    private int lastOrientation = -1;
    private int currentOrientation = -1;
    private int lastDisplayOrientation = -1;
    private int maxZoom;
    private float currentZoom;
    private boolean isVideo;
    private boolean initied;
    private OrientationEventListener orientationEventListener;
    private final int pictureFormat;
    private boolean meteringAreaSupported;
    private int diffOrientation;
    private int jpegOrientaion;
    private boolean sameTakePictureOrientaion;
    private boolean flipFront = true;
    private boolean optimizedForBarcode;
    private boolean useTorch;
    private final boolean isRound;
    private boolean destroyed;

    Camera.CameraInfo info = new Camera.CameraInfo();

    public static final int ORIENTATION_HYSTERESIS = 5;

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {

            } else {

            }
        }
    };
    private int displayOrientation;

    public CameraSession(CameraInfo info, Size previewSize, Size pictureSize, int format, boolean isRound) {
        cameraInfo = info;
        this.previewSize = previewSize;
        this.pictureSize = pictureSize;
        this.pictureFormat = format;
        this.isRound = isRound;
        currentFlashMode = Camera.Parameters.FLASH_MODE_OFF;

        orientationEventListener = new OrientationEventListener(MyApplication.appContext) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientationEventListener == null || !initied || orientation == ORIENTATION_UNKNOWN) {
                    return;
                }
                jpegOrientaion = roundOrientation(orientation, jpegOrientaion);
                WindowManager windowManager = (WindowManager) MyApplication.appContext.getSystemService(Context.WINDOW_SERVICE);
                int rotation = windowManager.getDefaultDisplay().getRotation();
                if (lastOrientation != jpegOrientaion || rotation != lastDisplayOrientation) {
                    if (!isVideo) {
                        configurePhotoCamera();
                    }
                    lastDisplayOrientation = orientation;
                    lastOrientation = jpegOrientaion;
                }
            }
        };
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }

    private int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

    public void setOptimizedForBarcode(boolean value) {
        optimizedForBarcode = value;
        configurePhotoCamera();
    }

    public void setCurrentFlashMode(String mode) {
        currentFlashMode = mode;
        configurePhotoCamera();
    }

    public void setTorchEnabled(boolean enabled) {
        try {
            currentFlashMode = enabled ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;
            configurePhotoCamera();
        } catch (Exception e) {

        }
    }

    public void setInitied(boolean initied) {
        this.initied = initied;
    }

    public boolean isInitied() {
        return initied;
    }

    public void setFlipFront(boolean flipFront) {
        this.flipFront = flipFront;
    }

    public int getCurrentOrientation() {
        return currentOrientation;
    }

    public int getWorldAngle() {
        return diffOrientation;
    }

    public boolean isSameTakePictureOrientaion() {
        return sameTakePictureOrientaion;
    }

    protected void configureRoundCamera(boolean initial) {
        try {
            isVideo = true;
            Camera camera = cameraInfo.camera;
            if (camera != null) {
                Camera.Parameters parameters = null;
                try {
                    parameters = camera.getParameters();
                } catch (Exception e) {
                    //
                }
                Camera.getCameraInfo(cameraInfo.cameraId, info);
                updateRotation();
                if (parameters != null) {
                    parameters.setPreviewSize(previewSize.mWidth, previewSize.mHeight);
                    parameters.setPictureSize(pictureSize.mWidth, pictureSize.mHeight);
                    parameters.setRecordingHint(true);

                    maxZoom = parameters.getMaxZoom();

                    String desiredFocusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                    if (parameters.getSupportedFocusModes().contains(desiredFocusMode)) {
                        parameters.setFocusMode(desiredFocusMode);
                    } else {
                        desiredFocusMode = Camera.Parameters.FOCUS_MODE_AUTO;
                        if (parameters.getSupportedFocusModes().contains(desiredFocusMode)) {
                            parameters.setFocusMode(desiredFocusMode);
                        }
                    }
                    int outputOrientation = 0;
                    if (jpegOrientaion != OrientationEventListener.ORIENTATION_UNKNOWN) {
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            outputOrientation = (info.orientation - jpegOrientaion + 360) % 360;
                        } else {
                            outputOrientation = (info.orientation + jpegOrientaion) % 360;
                        }
                    }
                    try {
                        parameters.setRotation(outputOrientation);
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            sameTakePictureOrientaion = (360 - displayOrientation) % 360 == outputOrientation;
                        } else {
                            sameTakePictureOrientaion = displayOrientation == outputOrientation;
                        }
                    } catch (Exception e) {
                        //
                    }
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    parameters.setZoom((int) currentZoom * maxZoom);
                    try {
                        camera.setParameters(parameters);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                        //
                    }
                    if (parameters.getMaxNumMeteringAreas() > 0) {
                        meteringAreaSupported = true;
                    }
                }
            }


        } catch (Exception e) {
            //
        }
    }

    public void updateRotation() {
        if (cameraInfo == null) {
            return;
        }

        try {
            Camera.getCameraInfo(cameraInfo.getCameraId(), info);
        } catch (Throwable throwable) {
            return;
        }
        Camera camera = (cameraInfo == null || destroyed) ? null : cameraInfo.camera;

        displayOrientation = getDisplayOrientation(info, true);
        int cameraDisplayOrientation;

        if ("samsung".equals(Build.MANUFACTURER) && "sf2wifixx".equals(Build.PRODUCT)) {
            cameraDisplayOrientation = 0;
        } else {
            int degrees = 0;
            int temp = displayOrientation;
            switch (temp) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
            if (info.orientation % 90 != 0) {
                info.orientation = 0;
            }
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                temp = (info.orientation + degrees) % 360;
                temp = (360 - temp) % 360;
            } else {
                temp = (info.orientation - degrees + 360) % 360;
            }
            cameraDisplayOrientation = temp;
        }
        currentOrientation = cameraDisplayOrientation;
        if (camera != null) {
            try {
                camera.setDisplayOrientation(currentOrientation);
            } catch (Throwable ignore) {

            }

        }
        diffOrientation = currentOrientation - displayOrientation;
        if (diffOrientation < 0) {
            diffOrientation += 360;
        }
    }

    protected void configurePhotoCamera() {
        try {
            Camera camera = cameraInfo.camera;
            if (camera!=null) {
                Camera.Parameters parameters = null;

                try {
                    parameters = camera.getParameters();
                }catch (Exception e) {
                    //
                }
                Camera.getCameraInfo(cameraInfo.cameraId,info);

                updateRotation();

                diffOrientation = currentOrientation - displayOrientation;
                if (diffOrientation < 0) {
                    diffOrientation += 360;
                }

                if (parameters!=null) {
                    parameters.setPreviewSize(previewSize.mWidth,previewSize.mHeight);
                    parameters.setPictureSize(pictureSize.mWidth,previewSize.mHeight);
                    parameters.setPictureFormat(pictureFormat);
                    parameters.setJpegQuality(100);
                    parameters.setJpegThumbnailQuality(100);
                    parameters.setZoom((int) (currentZoom * maxZoom));

                    if (optimizedForBarcode) {
                        List<String> modes = parameters.getSupportedFocusModes();
                        String continiousVideoFocus = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                        if (modes!=null&&modes.contains(continiousVideoFocus)){
                            parameters.setFocusMode(continiousVideoFocus);
                        }
                        if (parameters.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_BARCODE)){
                            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
                        }
                    }else{
                        if (parameters.getSupportedFocusModes()!=null&&parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }
                    }

                    int outputOrientation = 0;
                    if (jpegOrientaion != OrientationEventListener.ORIENTATION_UNKNOWN) {
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            outputOrientation = (info.orientation - jpegOrientaion + 360) % 360;
                        } else {
                            outputOrientation = (info.orientation + jpegOrientaion) % 360;
                        }
                    }
                    try {
                        parameters.setRotation(outputOrientation);
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            sameTakePictureOrientaion = (360 - displayOrientation) % 360 == outputOrientation;
                        } else {
                            sameTakePictureOrientaion = displayOrientation == outputOrientation;
                        }
                    } catch (Exception e) {
                        //
                    }
                    parameters.setFlashMode((useTorch ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF));

                    try {
                        camera.setParameters(parameters);
                    }catch (Exception e){
                        //
                    }
                }
            }
        }catch (Exception e){
            //
        }
    }

    protected void focusToRect(Rect focusRect, Rect meteringRect) {
        try {
            Camera camera = cameraInfo.camera;

            if (camera!=null) {
                camera.cancelAutoFocus();
                Camera.Parameters parameters = null;

                try {
                    parameters = camera.getParameters();
                }catch (Exception e) {
                    //
                }

                if (parameters!=null) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    ArrayList<Camera.Area> meteringAreas = new ArrayList<>();
                    meteringAreas.add(new Camera.Area(focusRect,1000));
                    parameters.setFocusAreas(meteringAreas);

                    if (meteringAreaSupported) {
                        meteringAreas = new ArrayList<>();
                        meteringAreas.add(new Camera.Area(meteringRect,1000));
                        parameters.setMeteringAreas(meteringAreas);
                    }
                    try {
                        camera.setParameters(parameters);
                        camera.autoFocus(autoFocusCallback);
                    }catch (Exception e) {
                        //
                    }
                }
            }
        }catch (Exception e){
            //
        }
    }

    public void onStartRecord() {
        isVideo = true;
    }

    public void setZoom(float value){
        currentZoom = value;
        if (isVideo && Camera.Parameters.FLASH_MODE_ON.equals(currentFlashMode)) {
            useTorch = true;
        }
        if (isRound) {
            configureRoundCamera(false);
        } else  {
            configurePhotoCamera();
        }
    }
    
    protected void configureRecording(int quality, MediaRecorder recorder) {
        Camera.getCameraInfo(cameraInfo.cameraId, info);

        int outputOrientation = 0;
        if (jpegOrientaion != OrientationEventListener.ORIENTATION_UNKNOWN) {
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                outputOrientation = (info.orientation - jpegOrientaion + 360) % 360;
            } else {
                outputOrientation = (info.orientation + jpegOrientaion) % 360;
            }
        }
        recorder.setOrientationHint(outputOrientation);

        int highProfile = getHigh();
        boolean canGoHigh = CamcorderProfile.hasProfile(cameraInfo.cameraId, highProfile);
        boolean canGoLow = CamcorderProfile.hasProfile(cameraInfo.cameraId, CamcorderProfile.QUALITY_LOW);
        if (canGoHigh && (quality == 1 || !canGoLow)) {
            recorder.setProfile(CamcorderProfile.get(cameraInfo.cameraId, highProfile));
        } else if (canGoLow) {
            recorder.setProfile(CamcorderProfile.get(cameraInfo.cameraId, CamcorderProfile.QUALITY_LOW));
        } else {
            throw new IllegalStateException("cannot find valid CamcorderProfile");
        }
        isVideo = true;
    }

    protected void stopVideoRecording() {
        isVideo = false;
        useTorch = false;
        configurePhotoCamera();
    }

    private int getHigh() {
        if ("LGE".equals(Build.MANUFACTURER) && "g3_tmo_us".equals(Build.PRODUCT)) {
            return CamcorderProfile.QUALITY_480P;
        }
        return CamcorderProfile.QUALITY_HIGH;
    }

    private int getDisplayOrientation(Camera.CameraInfo cameraInfo,boolean isStillCapture) {
        WindowManager windowManager = (WindowManager) MyApplication.appContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayOrientation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (info.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;

            if (!isStillCapture && displayOrientation == 90) {
                displayOrientation = 270;
            }
            if (!isStillCapture && "Huawei".equals(Build.MANUFACTURER) && "angler".equals(Build.PRODUCT) && displayOrientation == 270) {
                displayOrientation = 90;
            }
        } else {
            displayOrientation = (info.orientation - degrees + 360) % 360;
        }

        return displayOrientation;
    }

    public int getDisplayOrientation() {
        try {
            Camera.getCameraInfo(cameraInfo.cameraId,info);
            return getDisplayOrientation(info,true);
        }catch (Exception e) {
            //
        }
        return 0;
    }
    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        cameraInfo.camera.setPreviewCallback(previewCallback);
    }

    public void setOneShotPreviewCallback(Camera.PreviewCallback previewCallback) {
        try {
            cameraInfo.camera.setOneShotPreviewCallback(previewCallback);
        }catch (Exception e) {
            //
        }
    }
    public void destroy() {
        initied = false;
        destroyed = true;
        if (orientationEventListener!=null) {
            orientationEventListener.disable();
            orientationEventListener = null;
        }
    }
}
