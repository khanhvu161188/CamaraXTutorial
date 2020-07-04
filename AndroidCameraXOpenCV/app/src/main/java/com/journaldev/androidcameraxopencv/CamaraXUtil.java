package com.journaldev.androidcameraxopencv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraX;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.SensorOrientedMeteringPointFactory;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.video.Video;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CamaraXUtil {
    public CameraX.LensFacing lensFacing = CameraX.LensFacing.FRONT ;
    TextureView textureView;
    Activity activity;
    ImageView ivBitmap;

    public CamaraXUtil(Activity a){
        activity = a;
        textureView = a.findViewById(R.id.textureView);
        ivBitmap = a.findViewById(R.id.ivBitmap);
    }

    @SuppressLint("RestrictedApi")
    public Preview setPreview() {
        int width = textureView.getWidth();
        int height = textureView.getHeight();
        Rational aspectRatio = new Rational(width,height);
        Size screen = new Size(width,height); //size of the screen

//        PreviewConfig pConfig;
        PreviewConfig.Builder prevConfig = new PreviewConfig.Builder()
                .setLensFacing(lensFacing)
                .setTargetAspectRatioCustom(aspectRatio)
                .setTargetResolution(screen);
//                .setTargetAspectRatio(aspectRatio)

        Camera2Config.Extender camera2Extender = new Camera2Config.Extender(prevConfig);
        camera2Extender
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(60, 60))
                // .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                // .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                // AWB_MODE_OFF
                // .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 1);


        //AE_MODE_OFF
        /*
                .setCaptureRequestOption(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, 100)
                .setCaptureRequestOption(CaptureRequest.SENSOR_FRAME_DURATION, (long)16666666)
                .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, (long)20400000);
        */
//        CameraX.LensFacing d = ;
        /*  Removes autofocus


        MeteringPointFactory factory = new SensorOrientedMeteringPointFactory(textureView.getWidth(), textureView.getHeight());
        MeteringPoint point = factory.createPoint(0, 0);
        FocusMeteringAction action = FocusMeteringAction.Builder.from(point,
                FocusMeteringAction.MeteringMode.AF_ONLY)
                // .addPoint(point2, FocusMeteringAction.MeteringMode.AE_ONLY) // could have many
//                /*.setAutoFocusCallback(new OnAutoFocusListener(){
//                    public void onFocusCompleted(boolean isSuccess) {
//                    }
//                })
                // auto calling cancelFocusAndMetering in 5 sec.
                .setAutoCancelDuration(0, TimeUnit.SECONDS)
                .build();
        /*
        try {
            CameraX.getCameraControl(lensFacing).startFocusAndMetering(action);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }*/

        Preview preview = new Preview(prevConfig.build());

        //        ImageAnalysis.Builder();
        //new Config.ExtendableBuilder(pConfig);
//        Camera2Config.Extender camera2Extender = new Camera2Config.Extender(pConfig);  // r(pConfig);
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
//                        updateTransform();
                    }
                });

        return preview;
    }

    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    public ImageCapture setImageCapture() {

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(lensFacing)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(activity.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        final ImageCapture imgCapture = new ImageCapture(imageCaptureConfig);
        return imgCapture;
    }
    private Mat prevMat = null;
    TargetController targetController;
    void SetupTargetController(){
        targetController = new TargetController();
        for(int i =0; i<50; i++) {
            targetController.AddTargetLocal(new Target(0.15f, 0.85f, 0.1f));
            targetController.AddTargetLocal(new Target(0.15f, 0.15f, 0.1f));
            targetController.AddTargetLocal(new Target(0.85f, 0.15f, 0.1f));
            targetController.AddTargetLocal(new Target(0.85f, 0.85f, 0.1f));
        }
        //        targetController.AddTargetLocal(new Target(0.5f,0.5f,0.1f));
    }
    public ImageAnalysis setImageAnalysis() {
        // Setup image analysis pipeline that computes average pixel luminance
        class ThreadPerTaskExecutor implements Executor {
            public void execute(Runnable r) {
                new Thread(r).start();
            }
        }
        Executor executor = new ThreadPerTaskExecutor();
        // HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        // analyzerThread.start();
        // ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        SetupTargetController();
        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setLensFacing(lensFacing)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageQueueDepth(1)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer(executor,
                //(Executor) analyzerThread,
                (image, rotationDegrees) -> {
                    //Analyzing live camera feed begins.
                    final Bitmap bitmap = textureView.getBitmap();
                    if(bitmap==null)
                        return;

                    Mat bigMat = new Mat();
                    Utils.bitmapToMat(bitmap, bigMat);
                    Mat orginalMat = bigMat.clone();
                    int w = bigMat.width();
                    int h = bigMat.height();
                    int scaleFactor = 8;
                    Mat mat = new Mat();
//                    Mat fx = new Mat();
//                    Mat fy = new Mat();
                    List<Mat> listF = new ArrayList<Mat>();
                    Mat matMagnitude = new Mat();
                    Mat matMagnitudeX4 = new Mat();
                    Mat matZero = new Mat(new org.opencv.core.Size(w/scaleFactor,h/scaleFactor), CvType.CV_32FC1, new Scalar(0));//  cv::Size(1, 49), CV_64FC1);
                    Mat matWhite = new Mat(new org.opencv.core.Size(w/scaleFactor,h/scaleFactor), CvType.CV_32FC1, new Scalar(255.0));//  cv::Size(1, 49), CV_64FC1);
                    Mat matFlow = new Mat();

                    targetController.SetImageSpace(w,h);

//                    Log.d("TAG", "w " + w );
//                    Log.d("TAG", "h " + h );
                    //                        imgCone
                    // curr = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
                    // Imgproc.COLOR_RGB2GRAY;
                    // Imgproc.cvtColor(mat, mat, currentImageType);
//                    targetController.RenderCurrentTarget(bigMat);
                    if (prevMat != null) {
//                        Imgproc.comp
                        Imgproc.resize(bigMat, mat, new org.opencv.core.Size(w/scaleFactor, h/scaleFactor));
                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
                        Video.calcOpticalFlowFarneback(prevMat, mat, matFlow, 0.5, 3, 15, 3, 5, 1.2, 0);
                        Core.split(matFlow, listF);
                        Core.magnitude(listF.get(0),listF.get(1),matMagnitude);
//                        Imgproc.conve


                        Core.scaleAdd(matMagnitude,4,matZero, matMagnitudeX4);
                        matMagnitudeX4.convertTo(matMagnitudeX4, CvType.CV_8UC1);
//                        magnitude_x4_f = cv2.scaleAdd(magnitude_f, 4, black_f)
//                        magnitude_f = cv2.magnitude(flow[:,:,0], flow[:,:,1])
//                        magnitude_x4_clipped_at_255_f = cv2.min(magnitude_x4_f, mat255_f)
                        // Core.min(matMagnitudeX4,matWhite,matMagnitudeX4);
                        Imgproc.threshold(matMagnitudeX4, matMagnitudeX4, 7, 255, CvType.CV_8UC1);
                       // Imgproc.threshold(matMagnitudeX4, matMagnitudeX4, 15, 255, CvType.CV_8UC1);
                        prevMat = mat;
                        // check for collision
                        if(targetController.isColliding(matMagnitudeX4)){
                            targetController.SetCurrentTargetCaptured();
                        }

                        Core.scaleAdd(mat,1,matMagnitudeX4, matMagnitude);
                        Imgproc.resize(matMagnitude, bigMat, new org.opencv.core.Size(w, h));

                        // for testing
                       // Imgproc.resize(targetController.isColliding(matMagnitudeX4), bigMat, new org.opencv.core.Size(w, h));
                        // Imgproc.resize(matMagnitudeX4, bigMat, new org.opencv.core.Size(w, h));
//                        Imgproc.resize(matMagnitudeX4, bigMat, new org.opencv.core.Size(w, h));

                        // Imgproc.cvtColor(bigMat, bigMat, Imgproc.COLOR_RGB2GRAY);
                        // Imgproc.rectangle(bigMat,new Point(100, 100),new Point(w/2,h/2 ),new Scalar(0,255,0),3);

                    } else {
                        Imgproc.resize(bigMat, mat, new org.opencv.core.Size(w/scaleFactor, h/scaleFactor));
                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
                        prevMat = mat;
                        Imgproc.resize(mat, bigMat, new org.opencv.core.Size(w, h));
                    }

                     targetController.RenderCurrentTarget(orginalMat);

                    // Imgproc.GaussianBlur(mat,mat, new org.opencv.core.Size(21, 21), 0.0);
//                    Log.d("TAG", "analyze: " + width + " " + height);
//                    Imgproc.rectangle(mat,new Point(0, 0),new Point(width-5,height-5),new Scalar(0,255,0),3);
//                    Imgproc.rectangle(mat,new Point(100, 100),new Point(105,105 ),new Scalar(0,255,0),3);

                    Utils.matToBitmap(orginalMat, bitmap);
//                    Utils.matToBitmap(bigMat, bitmap);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivBitmap.setImageBitmap(bitmap);
                        }
                    });
                });


        return imageAnalysis;

    }

}
