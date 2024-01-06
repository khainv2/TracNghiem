package com.khainv9.tracnghiem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.khainv9.tracnghiem.R;

public class InitActivity extends AppCompatActivity {

    final int CAMERA_PERMISSION = 0;
    boolean CAMERA_PERMISSION_GRANT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        // Yêu cầu quyền truy cập trên Android 6.0 trở lên
        CAMERA_PERMISSION_GRANT = checkIfAlreadyHavePermission();
        if (!CAMERA_PERMISSION_GRANT)
            requestPermissions();
        startCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    CAMERA_PERMISSION_GRANT = true;
                startCamera();
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermissions() {
        CAMERA_PERMISSION_GRANT = false;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CAMERA_PERMISSION);
    }

    private void startCamera() {
        if (CAMERA_PERMISSION_GRANT)
            startActivity(new Intent(this, MainActivity.class));
    }
}