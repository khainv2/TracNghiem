package com.khainv9.tracnghiem.scan.algorithm;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.CvException;
import org.opencv.core.Mat;

public class BitmapUtil {

    public static Bitmap matToBitmap(Mat mat) {
        Bitmap bmp = null;
        Mat tmp = mat.clone();
        try {
            bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(tmp, bmp);
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }
        org.opencv.android.Utils.matToBitmap(mat, bmp);

        return bmp;
    }

    public static Mat bitmapToMat(Bitmap bmp) {
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        org.opencv.android.Utils.bitmapToMat(bmp32, mat);
        return mat;
    }
}
