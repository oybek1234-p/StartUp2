/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.example.market.camera;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.market.MyApplication;
import com.example.market.utils.AndroidUtilities;
import com.example.market.utils.BuildVars;
import com.example.market.utils.FileLog;
import com.example.market.video.MP4Builder;
import com.example.market.video.Mp4Movie;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

import androidx.camera.core.impl.utils.CompareSizesByArea;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;

@SuppressLint("NewApi")
public class CameraView extends FrameLayout implements TextureView.SurfaceTextureListener {

    public CameraView(Context context) {
        super(context);
        init(false);
    }

    public CameraView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        init(false);
    }

    public CameraView(Context context, AttributeSet attributeSet,int defstyle){
        super(context,attributeSet,0);
        init(false);
    }
    private Size previewSize;
    private Size pictureSize;
    CameraInfo info;
    private boolean mirror;
    private TextureView textureView;
    private ImageView blurredStubView;
    private CameraSession cameraSession;
    private boolean initied;
    private CameraViewDelegate delegate;
    private int clipTop;
    private int clipBottom;
    private boolean isFrontface;
    private Matrix txform = new Matrix();
    private Matrix matrix = new Matrix();
    private int focusAreaSize;

    private boolean useMaxPreview;

    private long lastDrawTime;
    private float focusProgress = 1.0f;
    private float innerAlpha;
    private float outerAlpha;
    private boolean initialFrontface;
    private int cx;
    private int cy;
    private Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean optimizeForBarcode;
    File recordFile;

    private DecelerateInterpolator interpolator = new DecelerateInterpolator();
    private volatile int surfaceWidth;
    private volatile int surfaceHeight;

    private File cameraFile;

    boolean firstFrameRendered;
    private final Object layoutLock = new Object();

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    private float[] moldSTMatrix = new float[16];
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "   gl_Position = uMVPMatrix * aPosition;\n" +
                    "   vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SCREEN_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision lowp float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "   gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    public void setRecordFile(File generateVideoPath) {
        recordFile = generateVideoPath;
    }

    Runnable onRecordingFinishRunnable;

    public boolean startRecording(File path, Runnable onFinished) {
        if (cameraThread==null) {
            init(false);
        }
        cameraThread.startRecording(path);
        onRecordingFinishRunnable = onFinished;
        return true;
    }

    public void stopRecording() {
        cameraThread.stopRecording();
    }

    ValueAnimator flipAnimator;
    boolean flipHalfReached;

    private int fpsLimit = -1;
    long nextFrameTimeNs;

    public void startSwitchingAnimation() {
        if (flipAnimator != null) {
            flipAnimator.cancel();
        }
        blurredStubView.animate().setListener(null).cancel();
        if (firstFrameRendered) {
            Bitmap bitmap = textureView.getBitmap(100, 100);
            if (bitmap != null) {
                //   Utilities.blurBitmap(bitmap, 3, 1, bitmap.getWidth(), bitmap.getHeight(), bitmap.getRowBytes());
                Drawable drawable = new BitmapDrawable(bitmap);
                blurredStubView.setBackground(drawable);
            }
            blurredStubView.setAlpha(0f);
        } else {
            blurredStubView.setAlpha(1f);
        }
        blurredStubView.setVisibility(View.VISIBLE);

        synchronized (layoutLock) {
            firstFrameRendered = false;
        }

        flipHalfReached = false;
        flipAnimator = ValueAnimator.ofFloat(0, 1f);
        flipAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();

                float rotation;
                boolean halfReached = false;
                if (v < 0.5f) {
                    rotation = v;
                } else {
                    halfReached = true;
                    rotation = v - 1f;
                }
                rotation *= 180;
                textureView.setRotationY(rotation);
                blurredStubView.setRotationY(rotation);
                if (halfReached && !flipHalfReached) {
                    blurredStubView.setAlpha(1f);
                    flipHalfReached = true;
                }
            }
        });
        flipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                flipAnimator = null;
                textureView.setTranslationY(0);
                textureView.setRotationX(0);
                textureView.setRotationY(0);
                textureView.setScaleX(1f);
                textureView.setScaleY(1f);

                blurredStubView.setRotationY(0);

                if (!flipHalfReached) {
                    blurredStubView.setAlpha(1f);
                    flipHalfReached = true;
                }
                invalidate();
            }
        });
        flipAnimator.setDuration(400);
        //   flipAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        flipAnimator.start();
        invalidate();
    }

    public interface CameraViewDelegate {
        void onCameraCreated(Camera camera);

        void onCameraInit();
    }

    public void init(boolean frontface){
        initialFrontface = isFrontface = frontface;
        textureView = new TextureView(getContext());
        textureView.setSurfaceTextureListener(this);
        addView(textureView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

        blurredStubView = new ImageView(getContext());
        addView(blurredStubView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        blurredStubView.setVisibility(View.GONE);
        focusAreaSize = AndroidUtilities.dp(96);
        outerPaint.setColor(0xffffffff);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeWidth(AndroidUtilities.dp(2));
        innerPaint.setColor(0x7fffffff);
    }


    public void setOptimizeForBarcode(boolean value) {
        optimizeForBarcode = value;
        if (cameraSession != null) {
            cameraSession.setOptimizedForBarcode(true);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (previewSize != null && cameraSession != null) {
            int frameWidth, frameHeight;
            cameraSession.updateRotation();
            if (cameraSession.getWorldAngle() == 90 || cameraSession.getWorldAngle() == 270) {
                frameWidth = previewSize.mWidth;
                frameHeight = previewSize.mHeight;
            } else {
                frameWidth = previewSize.mWidth;
                frameHeight = previewSize.mHeight;
            }
            float s = Math.max(MeasureSpec.getSize(widthMeasureSpec) / (float) frameWidth, MeasureSpec.getSize(heightMeasureSpec) / (float) frameHeight);
            blurredStubView.getLayoutParams().width = textureView.getLayoutParams().width = (int) (s * frameWidth);
            blurredStubView.getLayoutParams().height = textureView.getLayoutParams().height = (int) (s * frameHeight);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkPreviewMatrix();
    }

    public float getTextureHeight(float width, float height) {
        if (previewSize == null || cameraSession == null) {
            return height;
        }

        int frameWidth, frameHeight;
        if (cameraSession.getWorldAngle() == 90 || cameraSession.getWorldAngle() == 270) {
            frameWidth = previewSize.mWidth;
            frameHeight = previewSize.mHeight;
        } else {
            frameWidth = previewSize.mHeight;
            frameHeight = previewSize.mWidth;
        }
        float s = Math.max(width / (float) frameWidth, height / (float) frameHeight);
        return (int) (s * frameHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        checkPreviewMatrix();
    }

    public void setMirror(boolean value) {
        mirror = value;
    }

    public boolean isFrontface() {
        return isFrontface;
    }

    public TextureView getTextureView() {
        return textureView;
    }

    public void setUseMaxPreview(boolean value) {
        useMaxPreview = value;
    }

    public boolean hasFrontFaceCamera() {
        return true;
    }

    public void switchCamera() {
        if (cameraSession != null) {
            close(cameraSession);
            cameraSession = null;
        }
        initied = false;
        isFrontface = !isFrontface;
        updateCameraInfoSize();
        cameraThread.reinitForNewCamera();
    }

    public Size getPreviewSize() {
        return previewSize;
    }

    CameraGLThread cameraThread;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        updateCameraInfoSize();

        surfaceHeight = height;
        surfaceWidth = width;

        if (cameraThread == null && surface != null) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("CameraView " + "start create thread");
            }
            cameraThread = new CameraGLThread(surface);
            checkPreviewMatrix();
        }
        AndroidUtilities.runOnUIThread(()-> {
            Toast.makeText(MyApplication.appContext,"OnSurface aviable",Toast.LENGTH_SHORT).show();
        });
    }

    public void initCamera(){
        if (initied) {
            return;
        }
        if (cameras==null) {
            cameras = new ArrayList<>();

            int count = Camera.getNumberOfCameras();
            Camera.CameraInfo info = new Camera.CameraInfo();

            int bufferSize = 4;
            for (int cameraId = 0; cameraId < count; cameraId++) {
                Camera.getCameraInfo(cameraId, info);
                CameraInfo cameraInfo = new CameraInfo(cameraId, info.facing);

                Camera camera = Camera.open(cameraInfo.getCameraId());
                Camera.Parameters params = camera.getParameters();

                List<Camera.Size> list = params.getSupportedPreviewSizes();
                for (int a = 0; a < list.size(); a++) {
                    Camera.Size size = list.get(a);
                    if (size.width == 1280 && size.height != 720) {
                        continue;
                    }
                    if (size.height < 2160 && size.width < 2160) {
                        cameraInfo.previewSizes.add(new Size(size.width, size.height));
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("preview size = " + size.width + " " + size.height);
                        }
                    }
                }

                list = params.getSupportedPictureSizes();
                for (int a = 0; a < list.size(); a++) {
                    Camera.Size size = list.get(a);
                    if (size.width == 1280 && size.height != 720) {
                        continue;
                    }
                    if (!"samsung".equals(Build.MANUFACTURER) || !"jflteuc".equals(Build.PRODUCT) || size.width < 2048) {
                        cameraInfo.pictureSizes.add(new Size(size.width, size.height));
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("picture size = " + size.width + " " + size.height);
                        }
                    }
                }

                camera.release();
                cameras.add(cameraInfo);

                bufferSize += 4 + 4 + 8 * (cameraInfo.previewSizes.size() + cameraInfo.pictureSizes.size());
            }
           initied = true;
        }
    }

    public ArrayList<CameraInfo> cameras;

    public ArrayList<CameraInfo> getCameras() {
        return cameras;
    }
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.mWidth * lhs.mHeight - (long) rhs.mWidth * rhs.mHeight);
        }
    }
    public static Size chooseOptimalSize(List<Size> choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnoughWithAspectRatio = new ArrayList<>();
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.mWidth;
        int h = aspectRatio.mHeight;
        for (int a = 0; a < choices.size(); a++) {
            Size option = choices.get(a);
            if (option.mHeight == option.mWidth * h / w && option.mWidth >= width && option.mHeight >= height) {
                bigEnoughWithAspectRatio.add(option);
            } else if (option.mHeight * option.mWidth <= width * height * 4) {
                bigEnough.add(option);
            }
        }
        if (bigEnoughWithAspectRatio.size() > 0) {
            return Collections.min(bigEnoughWithAspectRatio, new CompareSizesByArea());
        } else if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return Collections.max(choices, new CompareSizesByArea());
        }
    }

    private void updateCameraInfoSize() {
        ArrayList<CameraInfo> cameraInfos = getCameras();
        if (cameraInfos == null) {
            return;
        }
        for (int a = 0; a < cameraInfos.size(); a++) {
            CameraInfo cameraInfo = cameraInfos.get(a);
            if (isFrontface && cameraInfo.frontCamera != 0 || !isFrontface && cameraInfo.frontCamera == 0) {
                info = cameraInfo;
                break;
            }
        }
        if (info == null) {
            return;
        }
        float size4to3 = 4.0f / 3.0f;
        float size16to9 = 16.0f / 9.0f;
        int  w = (int) MyApplication.appContext.getResources().getDisplayMetrics().widthPixels;
        int  y = (int) MyApplication.appContext.getResources().getDisplayMetrics().heightPixels;
        float screenSize = (float) Math.max(w, y) / Math.min(w, y);
        Size aspectRatio;
        int wantedWidth;
        int wantedHeight;

        int photoMaxWidth;
        int photoMaxHeight;
        if (initialFrontface) {
            aspectRatio = new Size(16, 9);
            photoMaxWidth = wantedWidth = 480;
            photoMaxHeight = wantedHeight = 270;
        } else {
            if (Math.abs(screenSize - size4to3) < 0.1f) {
                aspectRatio = new Size(4, 3);
                wantedWidth = getMeasuredWidth();
                wantedHeight = getMeasuredHeight();

                photoMaxWidth = getMeasuredWidth();
                photoMaxHeight = getMeasuredHeight();

            } else {
                aspectRatio = new Size(16, 9);
                wantedWidth = getMeasuredWidth();
                wantedHeight = getMeasuredHeight();

                photoMaxWidth = getMeasuredWidth();
                photoMaxHeight = getMeasuredHeight();
            }
        }

        previewSize = chooseOptimalSize(info.getPreviewSizes(), wantedWidth, wantedHeight, aspectRatio);
        pictureSize = chooseOptimalSize(info.getPictureSizes(), photoMaxWidth, photoMaxHeight, aspectRatio);

        requestLayout();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int surfaceW, int surfaceH) {
        surfaceHeight = surfaceH;
        surfaceWidth = surfaceW;
        checkPreviewMatrix();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (cameraThread != null) {
            cameraThread.shutdown(0);
            cameraThread = null;
        }
        if (cameraSession != null) {
            close(cameraSession);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (!initied && cameraSession != null && cameraSession.isInitied()) {
            if (delegate != null) {
                delegate.onCameraInit();
            }
            initied = true;
        }
    }

    public void setClipTop(int value) {
        clipTop = value;
    }

    public void setClipBottom(int value) {
        clipBottom = value;
    }

    private void checkPreviewMatrix() {
        if (previewSize == null) {
            return;
        }

        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        Matrix matrix = new Matrix();
        if (cameraSession != null) {
            matrix.postRotate(cameraSession.getDisplayOrientation());
        }
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
        matrix.invert(this.matrix);

        if (cameraThread != null) {
            AndroidUtilities.runOnUIThread(() -> {
                final CameraGLThread cameraThread = this.cameraThread;
                if (cameraThread != null && cameraThread.currentSession != null) {
                    int rotationAngle = cameraThread.currentSession.getWorldAngle();
                    android.opengl.Matrix.setIdentityM(mMVPMatrix, 0);
                    if (rotationAngle != 0) {
                        android.opengl.Matrix.rotateM(mMVPMatrix, 0, rotationAngle, 0, 0, 1);
                    }
                }
            });
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public void focusToPoint(int x, int y) {
        Rect focusRect = calculateTapArea(x, y, 1f);
        Rect meteringRect = calculateTapArea(x, y, 1.5f);

        if (cameraSession != null) {
            cameraSession.focusToRect(focusRect, meteringRect);
        }

        focusProgress = 0.0f;
        innerAlpha = 1.0f;
        outerAlpha = 1.0f;
        cx = x;
        cy = y;
        lastDrawTime = System.currentTimeMillis();
        invalidate();
    }

    public void setZoom(float value) {
        if (cameraSession != null) {
            cameraSession.setZoom(value);
        }
    }

    public void setDelegate(CameraViewDelegate cameraViewDelegate) {
        delegate = cameraViewDelegate;
    }

    public boolean isInitied() {
        return initied;
    }

    public CameraSession getCameraSession() {
        return cameraSession;
    }

    public void close(CameraSession session) {
        session.destroy();
        if (session.cameraInfo != null && session.cameraInfo.camera != null) {
            session.cameraInfo.camera.stopPreview();
            session.cameraInfo.camera.setPreviewCallbackWithBuffer(null);
            session.cameraInfo.camera.release();
            session.cameraInfo.camera = null;
        }
    }

    public void destroy(boolean async, final Runnable beforeDestroyRunnable) {
        if (cameraSession != null) {
            cameraSession.destroy();
            cameraSession.destroy();
            close(cameraSession);
        }
    }

    public Matrix getMatrix() {
        return txform;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (focusProgress != 1.0f || innerAlpha != 0.0f || outerAlpha != 0.0f) {
            int baseRad = AndroidUtilities.dp(30);
            long newTime = System.currentTimeMillis();
            long dt = newTime - lastDrawTime;
            if (dt < 0 || dt > 17) {
                dt = 17;
            }
            lastDrawTime = newTime;
            outerPaint.setAlpha((int) (interpolator.getInterpolation(outerAlpha) * 255));
            innerPaint.setAlpha((int) (interpolator.getInterpolation(innerAlpha) * 127));
            float interpolated = interpolator.getInterpolation(focusProgress);
            canvas.drawCircle(cx, cy, baseRad + baseRad * (1.0f - interpolated), outerPaint);
            canvas.drawCircle(cx, cy, baseRad * interpolated, innerPaint);

            if (focusProgress < 1) {
                focusProgress += dt / 200.0f;
                if (focusProgress > 1) {
                    focusProgress = 1;
                }
                invalidate();
            } else if (innerAlpha != 0) {
                innerAlpha -= dt / 150.0f;
                if (innerAlpha < 0) {
                    innerAlpha = 0;
                }
                invalidate();
            } else if (outerAlpha != 0) {
                outerAlpha -= dt / 150.0f;
                if (outerAlpha < 0) {
                    outerAlpha = 0;
                }
                invalidate();
            }
        }
        return result;
    }

    private float takePictureProgress = 1f;

    public void startTakePictureAnimation() {
        takePictureProgress = 0;
        invalidate();
        runHaptic();
    }

    public void runHaptic() {
        long[] vibrationWaveFormDurationPattern = {0, 1};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(vibrationWaveFormDurationPattern, -1);

            vibrator.cancel();
            vibrator.vibrate(vibrationEffect);
        } else {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (flipAnimator != null) {
            canvas.drawColor(Color.BLACK);
        }
        super.dispatchDraw(canvas);
        if (takePictureProgress != 1f) {
            takePictureProgress += 16 / 150f;
            if (takePictureProgress > 1f) {
                takePictureProgress = 1f;
            } else {
                invalidate();
            }
            canvas.drawColor(ColorUtils.setAlphaComponent(Color.BLACK, (int) ((1f - takePictureProgress) * 150)));
        }
    }


    private int[] position = new int[2];
    private int[] cameraTexture = new int[1];
    private int[] oldCameraTexture = new int[1];
    private VideoRecorder videoEncoder;

    public class CameraGLThread extends com.example.market.camera.DispatchQueue {

        private final static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private final static int EGL_OPENGL_ES2_BIT = 4;
        private SurfaceTexture surfaceTexture;
        private EGL10 egl10;
        private EGLDisplay eglDisplay;
        private EGLContext eglContext;
        private EGLSurface eglSurface;
        private EGLConfig eglConfig;
        private boolean initied;

        private CameraSession currentSession;

        private SurfaceTexture cameraSurface;

        private final int DO_RENDER_MESSAGE = 0;
        private final int DO_SHUTDOWN_MESSAGE = 1;
        private final int DO_REINIT_MESSAGE = 2;
        private final int DO_SETSESSION_MESSAGE = 3;
        private final int DO_START_RECORDING = 4;
        private final int DO_STOP_RECORDING = 5;

        private int drawProgram;
        private int vertexMatrixHandle;
        private int textureMatrixHandle;
        private int positionHandle;
        private int textureHandle;

        private boolean recording;
        private boolean needRecord;

        private Integer cameraId = 0;

        //private InstantCameraView.VideoRecorder videoEncoder;

        public CameraGLThread(SurfaceTexture surface) {
            super("CameraGLThread");
            surfaceTexture = surface;
        }

        private boolean initGL() {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("CameraView " + "start init gl");
            }
            egl10 = (EGL10) EGLContext.getEGL();

            eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglGetDisplay failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                }
                eglDisplay = null;
                finish();
                return false;
            }

            int[] version = new int[2];
            if (!egl10.eglInitialize(eglDisplay, version)) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglInitialize failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                }
                finish();
                return false;
            }

            int[] configsCount = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            int[] configSpec = new int[]{
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 0,
                    EGL10.EGL_DEPTH_SIZE, 0,
                    EGL10.EGL_STENCIL_SIZE, 0,
                    EGL10.EGL_NONE
            };
            if (!egl10.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglChooseConfig failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                }
                finish();
                return false;
            } else if (configsCount[0] > 0) {
                eglConfig = configs[0];
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglConfig not initialized");
                }
                finish();
                return false;
            }
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            eglContext = egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            if (eglContext == null || eglContext == EGL10.EGL_NO_CONTEXT) {
                eglContext = null;
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglCreateContext failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                }
                finish();
                return false;
            }

            if (surfaceTexture != null) {
                eglSurface = egl10.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null);
            } else {
                finish();
                return false;
            }

            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("createWindowSurface failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                }
                finish();
                return false;
            }
            if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                }
                finish();
                return false;
            }
            GL gl = eglContext.getGL();

            android.opengl.Matrix.setIdentityM(mSTMatrix, 0);

            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SCREEN_SHADER);
            if (vertexShader != 0 && fragmentShader != 0) {
                drawProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(drawProgram, vertexShader);
                GLES20.glAttachShader(drawProgram, fragmentShader);
                GLES20.glLinkProgram(drawProgram);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(drawProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] == 0) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.e("failed link shader");
                    }
                    GLES20.glDeleteProgram(drawProgram);
                    drawProgram = 0;
                } else {
                    positionHandle = GLES20.glGetAttribLocation(drawProgram, "aPosition");
                    textureHandle = GLES20.glGetAttribLocation(drawProgram, "aTextureCoord");
                    vertexMatrixHandle = GLES20.glGetUniformLocation(drawProgram, "uMVPMatrix");
                    textureMatrixHandle = GLES20.glGetUniformLocation(drawProgram, "uSTMatrix");
                }
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("failed creating shader");
                }
                finish();
                return false;
            }

            GLES20.glGenTextures(1, cameraTexture, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture[0]);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            android.opengl.Matrix.setIdentityM(mMVPMatrix, 0);

            if (BuildVars.LOGS_ENABLED) {
                FileLog.e("gl initied");
            }


            float tX = 1.0f / 2.0f;
            float tY = 1.0f / 2.0f;
            float[] verticesData = {
                    -1.0f, -1.0f, 0,
                    1.0f, -1.0f, 0,
                    -1.0f, 1.0f, 0,
                    1.0f, 1.0f, 0
            };
            float[] texData = {
                    0.5f - tX, 0.5f - tY,
                    0.5f + tX, 0.5f - tY,
                    0.5f - tX, 0.5f + tY,
                    0.5f + tX, 0.5f + tY
            };

            vertexBuffer = ByteBuffer.allocateDirect(verticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuffer.put(verticesData).position(0);

            textureBuffer = ByteBuffer.allocateDirect(texData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureBuffer.put(texData).position(0);

            cameraSurface = new SurfaceTexture(cameraTexture[0]);
            cameraSurface.setOnFrameAvailableListener(surfaceTexture -> requestRender());
            createCamera(cameraSurface);

            return true;
        }

        public void reinitForNewCamera() {
            Handler handler = getHandler();
            if (handler != null) {
                sendMessage(handler.obtainMessage(DO_REINIT_MESSAGE, info.cameraId), 0);
            }
        }

        public void finish() {
            if (eglSurface != null) {
                egl10.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                egl10.eglDestroySurface(eglDisplay, eglSurface);
                eglSurface = null;
            }
            if (eglContext != null) {
                egl10.eglDestroyContext(eglDisplay, eglContext);
                eglContext = null;
            }
            if (eglDisplay != null) {
                egl10.eglTerminate(eglDisplay);
                eglDisplay = null;
            }
        }

        public void setCurrentSession(CameraSession session) {
            Handler handler = getHandler();
            if (handler != null) {
                sendMessage(handler.obtainMessage(DO_SETSESSION_MESSAGE, session), 0);
            }
        }

        final int array[] = new int[1];

        private void onDraw(Integer cameraId, boolean updateTexImage) {
            if (!initied) {
                return;
            }

            if (!eglContext.equals(egl10.eglGetCurrentContext()) || !eglSurface.equals(egl10.eglGetCurrentSurface(EGL10.EGL_DRAW))) {
                if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                    }
                    return;
                }
            }
            if (updateTexImage) {
                try {
                    cameraSurface.updateTexImage();
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }

            final boolean shouldRenderFrame;
            synchronized (layoutLock) {
                if (fpsLimit <= 0) {
                    shouldRenderFrame = true;
                } else {
                    final long currentTimeNs = System.nanoTime();
                    if (currentTimeNs < nextFrameTimeNs) {
                        shouldRenderFrame = false;
                    } else {
                        nextFrameTimeNs += (long) (TimeUnit.SECONDS.toNanos(1) / fpsLimit);
                        ;
                        // The time for the next frame should always be in the future.
                        nextFrameTimeNs = Math.max(nextFrameTimeNs, currentTimeNs);
                        shouldRenderFrame = true;
                    }
                }
            }

            if (currentSession == null || currentSession.cameraInfo.cameraId != cameraId) {
                return;
            }

            if (recording && videoEncoder != null) {
                videoEncoder.frameAvailable(cameraSurface, cameraId, System.nanoTime());
            }

            if (!shouldRenderFrame) {
                return;
            }

            cameraSurface.getTransformMatrix(mSTMatrix);

            egl10.eglQuerySurface(eglDisplay, eglSurface, EGL10.EGL_WIDTH, array);
            int drawnWidth = array[0];
            egl10.eglQuerySurface(eglDisplay, eglSurface, EGL10.EGL_HEIGHT, array);
            int drawnHeight = array[0];

            GLES20.glViewport(0, 0, drawnWidth, drawnHeight);

            GLES20.glUseProgram(drawProgram);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture[0]);

            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
            GLES20.glEnableVertexAttribArray(positionHandle);

            GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);
            GLES20.glEnableVertexAttribArray(textureHandle);

            GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, mSTMatrix, 0);
            GLES20.glUniformMatrix4fv(vertexMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glDisableVertexAttribArray(positionHandle);
            GLES20.glDisableVertexAttribArray(textureHandle);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
            GLES20.glUseProgram(0);

            egl10.eglSwapBuffers(eglDisplay, eglSurface);

            synchronized (layoutLock) {
                if (!firstFrameRendered) {
                    firstFrameRendered = true;
                    AndroidUtilities.runOnUIThread(() -> {
                        onFirstFrameRendered();
                    });
                }
            }
        }

        @Override
        public void run() {
            initied = initGL();
            super.run();
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;

            switch (what) {
                case DO_RENDER_MESSAGE:
                    onDraw((Integer) inputMessage.obj, true);
                    break;
                case DO_SHUTDOWN_MESSAGE:
                    finish();
                    if (recording) {
                        videoEncoder.stopRecording(inputMessage.arg1);
                    }
                    Looper looper = Looper.myLooper();
                    if (looper != null) {
                        looper.quit();
                    }
                    break;
                case DO_REINIT_MESSAGE: {
                    if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("CameraView " + "eglMakeCurrent failed " + GLUtils.getEGLErrorString(egl10.eglGetError()));
                        }
                        return;
                    }

                    if (cameraSurface != null) {
                        cameraSurface.getTransformMatrix(moldSTMatrix);
                        cameraSurface.setOnFrameAvailableListener(null);
                        cameraSurface.release();
                    }

                    cameraId = (Integer) inputMessage.obj;

                    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture[0]);
                    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

                    cameraSurface = new SurfaceTexture(cameraTexture[0]);
                    cameraSurface.setOnFrameAvailableListener(surfaceTexture -> requestRender());
                    createCamera(cameraSurface);
                    break;
                }
                case DO_SETSESSION_MESSAGE: {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("CameraView " + "set gl rednderer session");
                    }
                    CameraSession newSession = (CameraSession) inputMessage.obj;
                    if (currentSession != newSession) {
                        currentSession = newSession;
                        cameraId = newSession.cameraInfo.cameraId;
                    }
                    currentSession.updateRotation();
                    int rotationAngle = currentSession.getWorldAngle();
                    android.opengl.Matrix.setIdentityM(mMVPMatrix, 0);
                    if (rotationAngle != 0) {
                        android.opengl.Matrix.rotateM(mMVPMatrix, 0, rotationAngle, 0, 0, 1);
                    }
                    break;
                }
                case DO_START_RECORDING: {
                    if (!initied) {
                        return;
                    }
                    recordFile = (File) inputMessage.obj;
                    videoEncoder = new VideoRecorder();
                    recording = true;
                    videoEncoder.startRecording(recordFile, EGL14.eglGetCurrentContext());
                    break;
                }
                case DO_STOP_RECORDING: {
                    if (videoEncoder != null) {
                        videoEncoder.stopRecording(0);
                        videoEncoder = null;
                    }
                    recording = false;
                    break;
                }
            }
        }

        public void shutdown(int send) {
            Handler handler = getHandler();
            if (handler != null) {
                sendMessage(handler.obtainMessage(DO_SHUTDOWN_MESSAGE, send, 0), 0);
            }
        }

        public void requestRender() {
            Handler handler = getHandler();
            if (handler != null) {
                sendMessage(handler.obtainMessage(DO_RENDER_MESSAGE, cameraId), 0);
            }
        }

        public boolean startRecording(File path) {
            Handler handler = getHandler();
            if (handler != null) {
                sendMessage(handler.obtainMessage(DO_START_RECORDING, path), 0);
                if (cameraSession!=null) {
                    cameraSession.onStartRecord();
                    Toast.makeText(MyApplication.appContext,"Session not null",Toast.LENGTH_SHORT).show();

                }
                return false;
            }
            return true;
        }

        public void stopRecording() {
            Handler handler = getHandler();
            if (handler != null) {
                sendMessage(handler.obtainMessage(DO_STOP_RECORDING), 0);
            }
        }
    }

    private void onFirstFrameRendered() {
        if (blurredStubView.getVisibility() == View.VISIBLE) {
            blurredStubView.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    blurredStubView.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.e(GLES20.glGetShaderInfoLog(shader));
            }
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private void createCamera(final SurfaceTexture surfaceTexture) {
        AndroidUtilities.runOnUIThread(() -> {
            if (cameraThread == null) {
                return;
            }
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("CameraView " + "create camera session");
            }
            if (previewSize == null) {
                updateCameraInfoSize();
            }
            if (previewSize == null) {
                return;
            }
            surfaceTexture.setDefaultBufferSize(previewSize.mWidth, previewSize.mHeight);

            cameraSession = new CameraSession(info, previewSize, pictureSize, ImageFormat.JPEG, false);
            cameraThread.setCurrentSession(cameraSession);
            requestLayout();


            open(cameraSession, surfaceTexture, () -> {
                if (cameraSession != null) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("CameraView " + "camera initied");
                    }
                    cameraSession.setInitied(true);
                    requestLayout();
                }
            }, () -> cameraThread.setCurrentSession(cameraSession));
        });
    }

    public void open(final CameraSession session, final SurfaceTexture texture, final Runnable callback, final Runnable prestartCallback) {
        if (session == null || texture == null) {
            return;
        }
        try {
            Camera camera = session.cameraInfo.camera;
            try {
                if (camera == null) {
                    camera = session.cameraInfo.camera = Camera.open(session.cameraInfo.cameraId);
                }
                Camera.Parameters params = camera.getParameters();
                List<String> rawFlashModes = params.getSupportedFlashModes();

                if (prestartCallback != null) {
                    prestartCallback.run();
                }

                session.configurePhotoCamera();
                camera.setPreviewTexture(texture);
                camera.startPreview();
                if (callback != null) {
                    AndroidUtilities.runOnUIThread(callback);
                }
            } catch (Exception e) {
                session.cameraInfo.camera = null;
                if (camera != null) {
                    camera.release();
                }
                FileLog.e(e);
            }
        } catch (Exception e) {

        }
    }

    private class VideoRecorder implements Runnable {

        private static final String VIDEO_MIME_TYPE = "video/avc";
        private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
        private static final int FRAME_RATE = 30;
        private static final int IFRAME_INTERVAL = 1;

        private File videoFile;
        private int videoWidth;
        private int videoHeight;
        private int videoBitrate;
        private boolean videoConvertFirstWrite = true;
        private boolean blendEnabled;

        private Surface surface;
        private android.opengl.EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
        private android.opengl.EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
        private android.opengl.EGLContext sharedEglContext;
        private android.opengl.EGLConfig eglConfig;
        private android.opengl.EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;

        private MediaCodec videoEncoder;
        private MediaCodec audioEncoder;

        private int prependHeaderSize;
        private boolean firstEncode;

        private MediaCodec.BufferInfo videoBufferInfo;
        private MediaCodec.BufferInfo audioBufferInfo;
        private MP4Builder mediaMuxer;
        private int videoTrackIndex = -5;
        private int audioTrackIndex = -5;

        private long lastCommitedFrameTime;
        private long audioStartTime = -1;

        private long currentTimestamp = 0;
        private long lastTimestamp = -1;

        private volatile EncoderHandler handler;

        private final Object sync = new Object();
        private boolean ready;
        private volatile boolean running;
        private volatile int sendWhenDone;
        private long skippedTime;
        private boolean skippedFirst;

        private long desyncTime;
        private long videoFirst = -1;
        private long videoLast;
        private long audioFirst = -1;
        private boolean audioStopedByTime;

        private int drawProgram;
        private int vertexMatrixHandle;
        private int textureMatrixHandle;
        private int positionHandle;
        private int textureHandle;
        private int zeroTimeStamps;
        private Integer lastCameraId = 0;

        private AudioRecord audioRecorder;
        private FloatBuffer textureBuffer;

        private ArrayList<Bitmap> keyframeThumbs = new ArrayList<>();
        private com.example.market.camera.DispatchQueue generateKeyframeThumbsQueue;
        private int frameCount;

        private Runnable recorderRunnable = new Runnable() {

            @Override
            public void run() {
                long audioPresentationTimeUs = -1;
                int readResult;
                boolean done = false;
                while (!done) {
                    if (!running && audioRecorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                        try {
                            audioRecorder.stop();
                        } catch (Exception e) {
                            done = true;
                        }
                        if (sendWhenDone == 0) {
                            break;
                        }
                    }
                }
                try {
                    audioRecorder.release();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                handler.sendMessage(handler.obtainMessage(MSG_STOP_RECORDING, sendWhenDone, 0));
            }
        };

        public void startRecording(File outputFile, android.opengl.EGLContext sharedContext) {
            String model = Build.DEVICE;
            if (model == null) {
                model = "";
            }

            Size pictureSize;
            int bitrate;
            pictureSize = previewSize;
            if (Math.min(pictureSize.mHeight, pictureSize.mWidth) >= 720) {
                bitrate = 3500000;
            } else {
                bitrate = 1800000;
            }

            videoFile = outputFile;

            if (cameraSession.getWorldAngle() == 90 || cameraSession.getWorldAngle() == 270) {
                videoWidth = pictureSize.mWidth;
                videoHeight = pictureSize.mHeight;
            } else {
                videoWidth = pictureSize.mHeight;
                videoHeight = pictureSize.mWidth;
            }
            videoBitrate = bitrate;
            sharedEglContext = sharedContext;

            synchronized (sync) {
                if (running) {
                    return;
                }
                running = true;
                Thread thread = new Thread(this, "TextureMovieEncoder");
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();
                while (!ready) {
                    try {
                        sync.wait();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
            }
            keyframeThumbs.clear();
            frameCount = 0;
            if (generateKeyframeThumbsQueue != null) {
                generateKeyframeThumbsQueue.cleanupQueue();
                generateKeyframeThumbsQueue.recycle();
            }
            generateKeyframeThumbsQueue = new DispatchQueue("keyframes_thumb_queque");
            handler.sendMessage(handler.obtainMessage(MSG_START_RECORDING));
        }

        public void stopRecording(int send) {
            handler.sendMessage(handler.obtainMessage(MSG_STOP_RECORDING, send, 0));
        }

        public void frameAvailable(SurfaceTexture st, Integer cameraId, long timestampInternal) {
            synchronized (sync) {
                if (!ready) {
                    return;
                }
            }

            long timestamp = st.getTimestamp();
            if (timestamp == 0) {
                zeroTimeStamps++;
                if (zeroTimeStamps > 1) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("CameraView " + "fix timestamp enabled");
                    }
                    timestamp = timestampInternal;
                } else {
                    return;
                }
            } else {
                zeroTimeStamps = 0;
            }

            handler.sendMessage(handler.obtainMessage(MSG_VIDEOFRAME_AVAILABLE, (int) (timestamp >> 32), (int) timestamp, cameraId));
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (sync) {
                handler = new EncoderHandler(this);
                ready = true;
                sync.notify();
            }
            Looper.loop();

            synchronized (sync) {
                ready = false;
            }
        }


        private void handleVideoFrameAvailable(long timestampNanos, Integer cameraId) {
            try {
                drainEncoder(false);
            } catch (Exception e) {
                FileLog.e(e);
            }
            long dt;
            if (!lastCameraId.equals(cameraId)) {
                lastTimestamp = -1;
                lastCameraId = cameraId;
            }
            if (lastTimestamp == -1) {
                lastTimestamp = timestampNanos;
                if (currentTimestamp != 0) {
                    dt = (System.currentTimeMillis() - lastCommitedFrameTime) * 1000000;
                } else {
                    dt = 0;
                }
            } else {
                dt = (timestampNanos - lastTimestamp);
                lastTimestamp = timestampNanos;
            }
            lastCommitedFrameTime = System.currentTimeMillis();
            if (!skippedFirst) {
                skippedTime += dt;
                if (skippedTime < 200000000) {
                    return;
                }
                skippedFirst = true;
            }
            currentTimestamp += dt;
            if (videoFirst == -1) {
                videoFirst = timestampNanos / 1000;
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("CameraView " + "first video frame was at " + videoFirst);
                }
            }
            videoLast = timestampNanos;


            GLES20.glUseProgram(drawProgram);
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);
            GLES20.glEnableVertexAttribArray(textureHandle);
            GLES20.glUniformMatrix4fv(vertexMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            if (oldCameraTexture[0] != 0) {
                if (!blendEnabled) {
                    GLES20.glEnable(GLES20.GL_BLEND);
                    blendEnabled = true;
                }
                GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, moldSTMatrix, 0);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oldCameraTexture[0]);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            }
            GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, mSTMatrix, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture[0]);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glDisableVertexAttribArray(positionHandle);
            GLES20.glDisableVertexAttribArray(textureHandle);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
            GLES20.glUseProgram(0);

            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, currentTimestamp);
            EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        }

        private void handleStopRecording(final int send) {
//            if (running) {
//                sendWhenDone = send;
//                running = false;
//                return;
//            }
            running = false;
            try {
                drainEncoder(true);
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (videoEncoder != null) {
                try {
                    videoEncoder.stop();
                    videoEncoder.release();
                    videoEncoder = null;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            if (audioEncoder != null) {
                try {
                    audioEncoder.stop();
                    audioEncoder.release();
                    audioEncoder = null;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            if (mediaMuxer != null) {
                try {
                    mediaMuxer.finishMovie();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }

            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
            if (surface != null) {
                surface.release();
                surface = null;
            }
            if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroyContext(eglDisplay, eglContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(eglDisplay);
            }
            eglDisplay = EGL14.EGL_NO_DISPLAY;
            eglContext = EGL14.EGL_NO_CONTEXT;
            eglConfig = null;
            handler.exit();

            AndroidUtilities.runOnUIThread(() -> {
                cameraSession.stopVideoRecording();
                onRecordingFinishRunnable.run();
            });
        }

        private void prepareEncoder() {
            try {
                int recordBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (recordBufferSize <= 0) {
                    recordBufferSize = 3584;
                }
                int bufferSize = 2048 * 24;
                if (bufferSize < recordBufferSize) {
                    bufferSize = ((recordBufferSize / 2048) + 1) * 2048 * 2;
                }
//                for (int a = 0; a < 3; a++) {
//                    buffers.add(new InstantCameraView.AudioBufferInfo());
//                }
                if (ActivityCompat.checkSelfPermission(MyApplication.appContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                audioRecorder.startRecording();
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("CameraView " + "initied audio record with channels " + audioRecorder.getChannelCount() + " sample rate = " + audioRecorder.getSampleRate() + " bufferSize = " + bufferSize);
                }
                Thread thread = new Thread(recorderRunnable);
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();

                audioBufferInfo = new MediaCodec.BufferInfo();
                videoBufferInfo = new MediaCodec.BufferInfo();

                MediaFormat audioFormat = new MediaFormat();
                audioFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);
                audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
                audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 32000);
                audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 2048);

                audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
                audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                audioEncoder.start();

                videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
                firstEncode = true;

                MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, videoWidth, videoHeight);

                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

                videoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                surface = videoEncoder.createInputSurface();
                videoEncoder.start();

                Mp4Movie movie = new Mp4Movie();
                movie.setCacheFile(videoFile);
                movie.setRotation(0);
                movie.setSize(videoWidth, videoHeight);
                mediaMuxer = new MP4Builder().createMovie(movie, false);

            } catch (Exception ioe) {
                throw new RuntimeException(ioe);
            }

            if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("EGL already set up");
            }

            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                eglDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            if (eglContext == EGL14.EGL_NO_CONTEXT) {
                int renderableType = EGL14.EGL_OPENGL_ES2_BIT;

                int[] attribList = {
                        EGL14.EGL_RED_SIZE, 8,
                        EGL14.EGL_GREEN_SIZE, 8,
                        EGL14.EGL_BLUE_SIZE, 8,
                        EGL14.EGL_ALPHA_SIZE, 8,
                        EGL14.EGL_RENDERABLE_TYPE, renderableType,
                        0x3142, 1,
                        EGL14.EGL_NONE
                };
                android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
                int[] numConfigs = new int[1];
                if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0)) {
                    throw new RuntimeException("Unable to find a suitable EGLConfig");
                }

                int[] attrib2_list = {
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL14.EGL_NONE
                };
                eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], sharedEglContext, attrib2_list, 0);
                eglConfig = configs[0];
            }

            int[] values = new int[1];
            EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);

            if (eglSurface != EGL14.EGL_NO_SURFACE) {
                throw new IllegalStateException("surface already created");
            }

            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0);
            if (eglSurface == null) {
                throw new RuntimeException("surface was null");
            }

            if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                }
                throw new RuntimeException("eglMakeCurrent failed");
            }
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            float tX = 1.0f / 2.0f;
            float tY = 1.0f / 2.0f;

            float[] texData = {
                    0.5f - tX, 0.5f - tY,
                    0.5f + tX, 0.5f - tY,
                    0.5f - tX, 0.5f + tY,
                    0.5f + tX, 0.5f + tY
            };
            textureBuffer = ByteBuffer.allocateDirect(texData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureBuffer.put(texData).position(0);


            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SCREEN_SHADER);
            if (vertexShader != 0 && fragmentShader != 0) {
                drawProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(drawProgram, vertexShader);
                GLES20.glAttachShader(drawProgram, fragmentShader);
                GLES20.glLinkProgram(drawProgram);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(drawProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] == 0) {
                    GLES20.glDeleteProgram(drawProgram);
                    drawProgram = 0;
                } else {
                    positionHandle = GLES20.glGetAttribLocation(drawProgram, "aPosition");
                    textureHandle = GLES20.glGetAttribLocation(drawProgram, "aTextureCoord");
                    vertexMatrixHandle = GLES20.glGetUniformLocation(drawProgram, "uMVPMatrix");
                    textureMatrixHandle = GLES20.glGetUniformLocation(drawProgram, "uSTMatrix");
                }
            }
        }

        public Surface getInputSurface() {
            return surface;
        }

        public void drainEncoder(boolean endOfStream) throws Exception {
            if (endOfStream) {
                videoEncoder.signalEndOfInputStream();
            }

            ByteBuffer[] encoderOutputBuffers = null;
            if (Build.VERSION.SDK_INT < 21) {
                encoderOutputBuffers = videoEncoder.getOutputBuffers();
            }
            while (true) {
                int encoderStatus = videoEncoder.dequeueOutputBuffer(videoBufferInfo, 10000);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (!endOfStream) {
                        break;
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (Build.VERSION.SDK_INT < 21) {
                        encoderOutputBuffers = videoEncoder.getOutputBuffers();
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = videoEncoder.getOutputFormat();
                    if (videoTrackIndex == -5) {
                        videoTrackIndex = mediaMuxer.addTrack(newFormat, false);
                        if (newFormat.containsKey(MediaFormat.KEY_PREPEND_HEADER_TO_SYNC_FRAMES) && newFormat.getInteger(MediaFormat.KEY_PREPEND_HEADER_TO_SYNC_FRAMES) == 1) {
                            ByteBuffer spsBuff = newFormat.getByteBuffer("csd-0");
                            ByteBuffer ppsBuff = newFormat.getByteBuffer("csd-1");
                            prependHeaderSize = spsBuff.limit() + ppsBuff.limit();
                        }
                    }
                } else if (encoderStatus >= 0) {
                    ByteBuffer encodedData;
                    if (Build.VERSION.SDK_INT < 21) {
                        encodedData = encoderOutputBuffers[encoderStatus];
                    } else {
                        encodedData = videoEncoder.getOutputBuffer(encoderStatus);
                    }
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                    }
                    if (videoBufferInfo.size > 1) {
                        if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                            if (prependHeaderSize != 0 && (videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                                videoBufferInfo.offset += prependHeaderSize;
                                videoBufferInfo.size -= prependHeaderSize;
                            }
                            if (firstEncode && (videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                                if (videoBufferInfo.size > 100) {
                                    encodedData.position(videoBufferInfo.offset);
                                    byte[] temp = new byte[100];
                                    encodedData.get(temp);
                                    int nalCount = 0;
                                    for (int a = 0; a < temp.length - 4; a++) {
                                        if (temp[a] == 0 && temp[a + 1] == 0 && temp[a + 2] == 0 && temp[a + 3] == 1) {
                                            nalCount++;
                                            if (nalCount > 1) {
                                                videoBufferInfo.offset += a;
                                                videoBufferInfo.size -= a;
                                                break;
                                            }
                                        }
                                    }
                                }
                                firstEncode = false;
                            }
                            long availableSize = mediaMuxer.writeSampleData(videoTrackIndex, encodedData, videoBufferInfo, true);
                        } else if (videoTrackIndex == -5) {
                            byte[] csd = new byte[videoBufferInfo.size];
                            encodedData.limit(videoBufferInfo.offset + videoBufferInfo.size);
                            encodedData.position(videoBufferInfo.offset);
                            encodedData.get(csd);
                            ByteBuffer sps = null;
                            ByteBuffer pps = null;
                            for (int a = videoBufferInfo.size - 1; a >= 0; a--) {
                                if (a > 3) {
                                    if (csd[a] == 1 && csd[a - 1] == 0 && csd[a - 2] == 0 && csd[a - 3] == 0) {
                                        sps = ByteBuffer.allocate(a - 3);
                                        pps = ByteBuffer.allocate(videoBufferInfo.size - (a - 3));
                                        sps.put(csd, 0, a - 3).position(0);
                                        pps.put(csd, a - 3, videoBufferInfo.size - (a - 3)).position(0);
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }

                            MediaFormat newFormat = MediaFormat.createVideoFormat("video/avc", videoWidth, videoHeight);
                            if (sps != null && pps != null) {
                                newFormat.setByteBuffer("csd-0", sps);
                                newFormat.setByteBuffer("csd-1", pps);
                            }
                            videoTrackIndex = mediaMuxer.addTrack(newFormat, false);
                        }
                    }
                    videoEncoder.releaseOutputBuffer(encoderStatus, false);
                    if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                }
            }

            if (Build.VERSION.SDK_INT < 21) {
                encoderOutputBuffers = audioEncoder.getOutputBuffers();
            }
            boolean encoderOutputAvailable = true;
            while (true) {
                int encoderStatus = audioEncoder.dequeueOutputBuffer(audioBufferInfo, 0);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (!endOfStream || !running && sendWhenDone == 0) {
                        break;
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (Build.VERSION.SDK_INT < 21) {
                        encoderOutputBuffers = audioEncoder.getOutputBuffers();
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = audioEncoder.getOutputFormat();
                    if (audioTrackIndex == -5) {
                        audioTrackIndex = mediaMuxer.addTrack(newFormat, true);
                    }
                } else if (encoderStatus >= 0) {
                    ByteBuffer encodedData;
                    if (Build.VERSION.SDK_INT < 21) {
                        encodedData = encoderOutputBuffers[encoderStatus];
                    } else {
                        encodedData = audioEncoder.getOutputBuffer(encoderStatus);
                    }
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                    }
                    if ((audioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        audioBufferInfo.size = 0;
                    }
                    if (audioBufferInfo.size != 0) {
                        mediaMuxer.writeSampleData(audioTrackIndex, encodedData, audioBufferInfo, false);
                    }
                    audioEncoder.releaseOutputBuffer(encoderStatus, false);
                    if ((audioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                    EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                    EGL14.eglDestroyContext(eglDisplay, eglContext);
                    EGL14.eglReleaseThread();
                    EGL14.eglTerminate(eglDisplay);
                    eglDisplay = EGL14.EGL_NO_DISPLAY;
                    eglContext = EGL14.EGL_NO_CONTEXT;
                    eglConfig = null;
                }
            } finally {
                super.finalize();
            }
        }
    }

    private static final int MSG_START_RECORDING = 0;
    private static final int MSG_STOP_RECORDING = 1;
    private static final int MSG_VIDEOFRAME_AVAILABLE = 2;
    private static final int MSG_AUDIOFRAME_AVAILABLE = 3;

    private static class EncoderHandler extends Handler {
        private WeakReference<VideoRecorder> mWeakEncoder;

        public EncoderHandler(VideoRecorder encoder) {
            mWeakEncoder = new WeakReference<>(encoder);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            VideoRecorder encoder = mWeakEncoder.get();
            if (encoder == null) {
                return;
            }

            switch (what) {
                case MSG_START_RECORDING: {
                    try {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.e("start encoder");
                        }
                        encoder.prepareEncoder();
                    } catch (Exception e) {
                        throw e;
//                        FileLog.e(e);
//                        encoder.handleStopRecording(0);
//                        Looper.myLooper().quit();
                    }
                    break;
                }

                case MSG_STOP_RECORDING: {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.e("stop encoder");
                    }
                    encoder.handleStopRecording(inputMessage.arg1);
                    break;
                }
                case MSG_VIDEOFRAME_AVAILABLE: {
                    long timestamp = (((long) inputMessage.arg1) << 32) | (((long) inputMessage.arg2) & 0xffffffffL);
                    Integer cameraId = (Integer) inputMessage.obj;
                    encoder.handleVideoFrameAvailable(timestamp, cameraId);
                    break;
                }
                case MSG_AUDIOFRAME_AVAILABLE: {
                 //   encoder.handleAudioFrameAvailable((InstantCameraView.AudioBufferInfo) inputMessage.obj);
                    break;
                }
            }
        }

        public void exit() {
            Looper.myLooper().quit();
        }
    }

    public void setFpsLimit(int fpsLimit) {
        this.fpsLimit = fpsLimit;
    }
}
