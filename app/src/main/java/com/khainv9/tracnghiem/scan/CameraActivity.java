package com.khainv9.tracnghiem.scan;

import static org.opencv.core.CvType.CV_8UC3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;
import com.khainv9.tracnghiem.models.DeThi;
import com.khainv9.tracnghiem.models.DiemThi;
import com.khainv9.tracnghiem.models.HocSinh;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "CameraActivity";

    Scanner scanner;

    private CameraBridgeViewBase mOpenCvCameraView;
    Mat test;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    BaiThi baiThi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);


        mOpenCvCameraView = findViewById(R.id.activity_camera);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        Bitmap bmp = Utils.loadImage("123");
        test = Scanner.bitmapToMat(bmp);

        //lấy vị trí của bài thi trong intent gửi đến
        int i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
        if (i >= 0 && i < Utils.dsBaiThi.size()){
            baiThi = Utils.dsBaiThi.get(i);
            scanner = new Scanner(baiThi);
        }

        Toast.makeText(this, "Chạm để bắt đầu chấm bài", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (scanner != null)
            scanner.init(width, height);
    }

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (true)
            return test;
        if (scanner == null) {
            return inputFrame.rgba();
        } else {
            Scanner.ProcessResult result = scanner.processFrame(inputFrame.rgba());
            if (!result.info.isEmpty()){
                runOnUiThread(() -> Toast.makeText(CameraActivity.this, result.info, Toast.LENGTH_SHORT).show());
            }
            return result.resultMat;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (scanner != null)
            scanner.touch = true;
        return false;
    }

}
