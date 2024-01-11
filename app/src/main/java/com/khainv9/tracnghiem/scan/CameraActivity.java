package com.khainv9.tracnghiem.scan;

import static org.opencv.core.CvType.CV_8UC3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

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

                // Load image from R.drawable.test to bitmap
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                test = Scanner.bitmapToMat(bmp);
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

        //lấy vị trí của bài thi trong intent gửi đến
        int i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
        if (i >= 0 && i < Utils.dsBaiThi.size()){
            baiThi = Utils.dsBaiThi.get(i);
        } else {
            baiThi = new BaiThi("abc", 17, 2, 2);
        }
        scanner = new Scanner(baiThi);

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
//        if (true)
//            return test;
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
