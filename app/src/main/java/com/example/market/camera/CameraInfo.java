package com.example.market.camera;

import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import java.util.ArrayList;

public class CameraInfo {

    protected int cameraId;
    protected android.hardware.Camera camera;
    protected ArrayList<Size> pictureSizes = new ArrayList<>();
    protected ArrayList<Size> previewSizes = new ArrayList<>();
    protected final int frontCamera;

    protected CameraDevice cameraDevice;
    public CameraCaptureSession cameraCaptureSession;

    public CameraInfo(int cameraId,int frontFace) {
        this.cameraId = cameraId;
        frontCamera = frontFace;

    }

    public boolean isFrontFace(){
        return frontCamera !=0;
    }

    public int getCameraId() {
        return cameraId;
    }

    public Camera getCamera() {
        return camera;
    }

    public ArrayList<Size> getPictureSizes() {
        return pictureSizes;
    }

    public ArrayList<Size> getPreviewSizes() {
        return previewSizes;
    }
}
