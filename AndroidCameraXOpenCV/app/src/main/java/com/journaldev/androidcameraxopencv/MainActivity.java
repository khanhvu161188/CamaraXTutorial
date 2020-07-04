package com.journaldev.androidcameraxopencv;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.Config;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.MutableConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.SensorOrientedMeteringPointFactory;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.CaptureRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static androidx.camera.core.CameraX.getDefaultLensFacing;

public class MainActivity extends AppCompatActivity // implements View.OnClickListener
 {


    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    ImageView ivBitmap;
    LinearLayout llBottom;
    int currentImageType = Imgproc.COLOR_RGB2GRAY;

    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Preview preview;
    Mat imgCone;
    FloatingActionButton btnCapture, btnOk, btnCancel;

    int width,height;
    CamaraXUtil camaraXUtil;
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*
        btnCapture = findViewById(R.id.btnCapture);
        btnOk = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnReject);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        llBottom = findViewById(R.id.llBottom);
*/

        camaraXUtil = new CamaraXUtil(this);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        // load cone image
        try {
            imgCone = Utils.loadResource(   this, R.drawable.traffic_cone_31883_640, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  Imgproc.cvtColor(img, gryimg, Imgproc.COLOR_RGB2BGRA);
    }

    private void startCamera() {
        CameraX.unbindAll();
        preview = camaraXUtil.setPreview();
        imageCapture = camaraXUtil.setImageCapture();
        imageAnalysis = camaraXUtil.setImageAnalysis();

        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
        // bindCameraUseCases();
    }

/*
    private void bindCameraUseCases() {
        // Make sure that there are no other use cases bound to CameraX
        CameraX.unbindAll();
        PreviewConfig prevConfig = new PreviewConfig.Builder()
            .setLensFacing(lensFacing)
            .build();
        Preview preview = new Preview(prevConfig);

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
            .setLensFacing(lensFacing)
            .build();
        imageCapture = new ImageCapture(imageCaptureConfig);

        // Apply declared configs to CameraX using the same lifecycle owner
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }
*/

/*
    private void showAcceptedRejectedButton(boolean acceptedRejected) {
        if (acceptedRejected) {
            CameraX.unbind(preview, imageAnalysis);
            llBottom.setVisibility(View.VISIBLE);
            btnCapture.hide();
            textureView.setVisibility(View.GONE);
        } else {
            btnCapture.show();
            llBottom.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.black_white:
                currentImageType = Imgproc.COLOR_RGB2GRAY;
                startCamera();
                return true;

            case R.id.hsv:
                currentImageType = Imgproc.COLOR_RGB2HSV;
                startCamera();
                return true;

            case R.id.lab:
                currentImageType = Imgproc.COLOR_RGB2Lab;
                startCamera();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
/*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReject:
                // showAcceptedRejectedButton(false);
                break;

            case R.id.btnAccept:

                break;
        }
    }
 */

}
