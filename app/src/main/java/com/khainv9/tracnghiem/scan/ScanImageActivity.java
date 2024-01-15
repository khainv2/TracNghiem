package com.khainv9.tracnghiem.scan;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.khainv9.tracnghiem.R;
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.models.Examination;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ScanImageActivity extends AppCompatActivity {

    private static final String TAG = "ScanImageActivity";

    Scanner scanner;
    Examination examination;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {

                //lấy vị trí của bài thi trong intent gửi đến
                int examId = getIntent().getIntExtra(DatabaseManager.ARG_P_BAI_THI, 0);
                examination = DatabaseManager.getExamination(examId);
                if (examination == null){
                    finish();
                    return;
                }
                scanner = new Scanner(examination);
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    void readImageFromPath(String path) {
        Log.d("TAG", "readImageFromPath: path = " + path);
        Mat imageMat = Imgcodecs.imread(path);
        Log.d(TAG, "onActivityResult: imageMat = " + imageMat);

        // Rotate mat 180 degrees
        int width = imageMat.width();
        int height = imageMat.height();

        if (width > height){
            org.opencv.core.Core.flip(imageMat, imageMat, -1);
        } else {
            imageMat = imageMat.t();
            org.opencv.core.Core.flip(imageMat, imageMat, 0);
        }

        width = imageMat.width();
        height = imageMat.height();

        Log.d(TAG, "onActivityResult: next imageMat = " + imageMat);
        int fixedWidth = height * 1920 / 1080;

        // Create large mat for processing with height and fixedWidth
        Mat largeMat = new Mat(height, fixedWidth, imageMat.type());

        // Fill all large mat with black color
        largeMat.setTo(org.opencv.core.Scalar.all(0));

        // Copy imageMat to largeMat
        Rect roi = new Rect(0, 0, width, height);
        imageMat.copyTo(largeMat.submat(roi));


        Imgproc.resize(largeMat, largeMat, new org.opencv.core.Size(1920, 1080));

        scanner.init(largeMat.width(), largeMat.height());
        Scanner.ProcessResult result = scanner.processFrame(largeMat);
        if (!result.info.isEmpty()){
            largeMat = result.resultMat;
        }

        // Rotate mat 90 degrees
        org.opencv.core.Core.flip(largeMat.t(), largeMat, 1);
//
//        // Convert mat to bitmap
        imageMat = largeMat;
        Bitmap bitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(imageMat, bitmap);
//


        // Show bitmap to image view
        ImageView iv = findViewById(R.id.iv_Scanner);
        iv.setImageBitmap(bitmap);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String imagePath = getRealPathFromURI(this, selectedImageUri);
            Log.d(TAG, "getMatFromUri: imagePath = " + imagePath);
            if (imagePath != null) {
                readImageFromPath(imagePath);
            }
        }
    }

    private static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            return null; // Error handling
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }


    private void onButtonImageSelect(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image);

        Button bt = findViewById(R.id.bt_SelectImage);
        bt.setOnClickListener(v -> onButtonImageSelect());
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
}