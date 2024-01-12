package com.khainv9.tracnghiem.scan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.khainv9.tracnghiem.R;
import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ScanImageActivity extends AppCompatActivity {

    private static final String TAG = "ScanImageActivity";

    Scanner scanner;
    BaiThi baiThi;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {

                //lấy vị trí của bài thi trong intent gửi đến
                int i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
                if (i >= 0 && i < Utils.dsBaiThi.size()){
                    baiThi = Utils.dsBaiThi.get(i);
                } else {
                    baiThi = new BaiThi("abc", 17, 2, 2);
                }
                scanner = new Scanner(baiThi);
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Lấy ảnh từ thư viện
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            ImageView iv = findViewById(R.id.iv_Scanner);
            Uri selectedImageUri = data.getData();
//            iv.setImageURI(selectedImageUri);

            Mat imageMat = getMatFromUri(this, selectedImageUri);
            if (imageMat == null) {
                Log.e(TAG, "onActivityResult: imageMat is null");
                return;
            }

            Log.d(TAG, "onActivityResult: imageMat = " + imageMat);

            // Rotate mat 180 degrees
            org.opencv.core.Core.flip(imageMat, imageMat, -1);
            int width = imageMat.width();
            int height = imageMat.height();


            // Convert mat to bitmap
            Bitmap bitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(imageMat, bitmap);

            // Show bitmap to image view
            iv.setImageBitmap(bitmap);
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

    public static Mat getMatFromUri(Context context, Uri uri) {
        // Step 1: Convert the URI to a file path
        String imagePath = getRealPathFromURI(context, uri);

        Log.d(TAG, "getMatFromUri: imagePath = " + imagePath);
        if (imagePath == null) {
            return null; // Error handling
        }

        // Step 2: Read the image into an OpenCV Mat
        Mat imageMat = Imgcodecs.imread(imagePath);

        return imageMat;
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
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