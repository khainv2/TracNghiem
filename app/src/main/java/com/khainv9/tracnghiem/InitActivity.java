package com.khainv9.tracnghiem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

public class InitActivity extends AppCompatActivity {

    final int CAMERA_PERMISSION = 0;
    boolean isPermissionGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        // Yêu cầu quyền truy cập trên Android 6.0 trở lên
        isPermissionGrant = checkIfAlreadyHavePermission();
        if (!isPermissionGrant)
            requestPermissions();
        startCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                isPermissionGrant = true;
            startCamera();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        isPermissionGrant = false;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CAMERA_PERMISSION);
    }

    private void startCamera() {
        if (isPermissionGrant) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}