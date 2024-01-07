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
import android.widget.TextView;

import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "CameraActivity";
    private static final int FixedTop = 40;
    private static final int FixedRight = 18;
    private static final int FixedBottomLeft = 1;

    private static final int ThresholdMean = 230;

    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView textViewInfo;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    BaiThi baiThi;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.tutorial1_surface_view);

        //
        mOpenCvCameraView = findViewById(R.id.activity_camera);

        //dặt camera hiển thị
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        //
        mOpenCvCameraView.setCvCameraViewListener(this);
        this.imageProcessing = new ImageProcessing();

        textViewInfo = findViewById(R.id.textViewInfo);

        //lấy vị trí của bài thi trong intent gửi đến
        int i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
//        baiThi = Utils.dsBaiThi.get(i);
//        Toast.makeText(this, "Chạm để bắt đầu chấm bài", Toast.LENGTH_SHORT).show();
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

    private static int halfRect = 1000;
    //    public static float ratio;
    //    Mat clone;
    Mat[] corners;
    Mat[] corners1;
    int count = 0;
    Mat hierarchy;
    public ImageProcessing imageProcessing;
    //định dạng màu RGA
    Mat mRga;
    //định dạng màu RGA (bản sao)
    Mat mRga1;
    //kích thước hiển thị
    int myHeight;
    int myWidth;

    //hình chữ nhật
    Rect[] rects;
    //điểm bắt đầu
    int startX = 0;
    int startY = 0;

    //kích thước thử nghiệm (giấy thi, pixel)
    //kích thước cắt bởi 4 góc dấu chấm đen
    Template template;

    Mat src, bgrFrame, bilateralImage, denoisedImage, hsvImage, frameThreshed, imgray;
    Mat dst;


    //khởi tạo khi bắt đầu chạy ứng dụng
    @Override
    public void onCameraViewStarted(int width, int height) {
        this.myWidth = width;
        this.myHeight = (this.myWidth * 9) / 16;
        this.startX = (width - this.myWidth) / 2; // add
        this.startY = (height - this.myHeight) / 2;
        this.mRga1 = new Mat(height, width, CvType.CV_8UC4);
        this.mRga = new Mat(this.myHeight, this.myWidth, CvType.CV_8UC4);
        int heightCal = this.myHeight / 4;
        int widthCal = (this.myHeight * 9) / 8;
        this.hierarchy = new Mat();
        this.corners = new Mat[4];
        this.corners1 = new Mat[4];
        this.rects = new Rect[4];
        this.rects[0] = new Rect(0, 0, heightCal, heightCal);
        this.rects[1] = new Rect(widthCal, 0, heightCal, heightCal);
        this.rects[2] = new Rect(0, this.myHeight - heightCal, heightCal, heightCal);
        this.rects[3] = new Rect(widthCal, this.myHeight - heightCal, heightCal, heightCal);
        this.btFPoint = new ArrayList<>();
        //tạo template quét 20 câu
        template = Template.createTemplate20();


        src = new Mat(height, width, CV_8UC3);

        bgrFrame = new Mat();
        bilateralImage = new Mat();
        denoisedImage = new Mat();
        hsvImage = new Mat();
        frameThreshed = new Mat();
        imgray = new Mat();
        hierarchy = new Mat();
        dst = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        //khi camera dừng, giải phòng các tấm nền
        this.mRga.release();
        this.mRga1.release();
        this.hierarchy.release();
    }

    //danh sách các điểm quét tìm thấy (tìm 4 điểm khung hình vuông)
    ArrayList<Point> btFPoint;

    private static double[] findSlopeAndIntercept(List<Point> points){
        double[] lineParams = new double[2];
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        for (Point point : points){
            sumX += point.x;
            sumY += point.y;
            sumXY += point.x * point.y;
            sumX2 += point.x * point.x;
        }
        double n = points.size();
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        lineParams[0] = slope;
        lineParams[1] = intercept;
        return lineParams;
    }

    private static void sort(List<Integer> values){
        for (int i = 0; i < values.size(); i++){
            for (int j = i + 1; j < values.size(); j++){
                if (values.get(i) > values.get(j)){
                    int temp = values.get(i);
                    values.set(i, values.get(j));
                    values.set(j, temp);
                }
            }
        }
    }

    private static int findOutlier(List<Point> points) {
        // Tìm hệ số góc và sai số của đường thẳng tốt nhất (best-fit line)
        double[] lineParams = findSlopeAndIntercept(points);

        // Tính khoảng cách từ mỗi điểm đến đường thẳng
        List<Double> distances = new ArrayList<>();
        for (Point point : points) {
            double distance = Math.abs(point.y - (lineParams[0] * point.x + lineParams[1]));
            distances.add(distance);
        }

        // Tìm điểm có khoảng cách lớn nhất (nằm xa đường thẳng)
        int outlierIndex = 0;
        double maxDistance = -1;
        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i) > maxDistance) {
                maxDistance = distances.get(i);
                outlierIndex = i;
            }
        }
        return outlierIndex;
    }

    private static double distance(Point p1, Point p2){
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private static boolean sortListPoints(List<Point> points, boolean isHorizontal) {
        if (points.size() < 2) {
            return false;
        }
        // Sort points by x
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                if (isHorizontal) {
                    if (points.get(i).x > points.get(j).x) {
                        Point temp = points.get(i);
                        points.set(i, points.get(j));
                        points.set(j, temp);
                    }
                } else {
                    if (points.get(i).y > points.get(j).y) {
                        Point temp = points.get(i);
                        points.set(i, points.get(j));
                        points.set(j, temp);
                    }
                }
            }
        }
        return true;
    }

    private static boolean isBlackRegion(Mat region) {
        // Check if the mean intensity of the region is below a certain threshold
        Scalar meanIntensity = Core.mean(region);
        double threshold = 50; // Adjust this threshold as needed
        return meanIntensity.val[0] < threshold;
    }


    private static Point calculatePointRef(Point tl, Point tr, Point bl, Point br, double ratioWithTop, double ratioWithRight){
        double distanceTLTR = distance(tl, tr);
        double distanceTRBR = distance(tr, br);
        double distanceTLBL = distance(tl, bl);
        double distanceBLBR = distance(bl, br);

        ratioWithTop = ratioWithTop * distanceTLBL / distanceTRBR;
        ratioWithRight = ratioWithRight * distanceTLTR / distanceBLBR;
        Point testTop = new Point(tl.x + (tr.x - tl.x) * ratioWithTop,
                tl.y + (tr.y - tl.y) * ratioWithTop);
        Point testRight = new Point(tr.x + (br.x - tr.x) * ratioWithRight, tr.y + (br.y - tr.y) * ratioWithRight);

        double distanceTestTopToTL = distance(testTop, tl);
        double distanceTestRightToTR = distance(testRight, tr);

        Point refPointBottom = new Point(bl.x + (br.x - bl.x) * distanceTestTopToTL / distanceTLTR,
                bl.y + (br.y - bl.y) * distanceTestTopToTL / distanceTLTR);

        Point refPointLeft = new Point(tl.x + (bl.x - tl.x) * distanceTestRightToTR / distanceTRBR,
                tl.y + (bl.y - tl.y) * distanceTestRightToTR / distanceTRBR);

        return calculateIntersection(testTop, refPointBottom, testRight, refPointLeft);
    }

    private static Point calculateIntersection(Point line1Start, Point line1End, Point line2Start, Point line2End) {
        double x1 = line1Start.x;
        double y1 = line1Start.y;
        double x2 = line1End.x;
        double y2 = line1End.y;

        double x3 = line2Start.x;
        double y3 = line2Start.y;
        double x4 = line2End.x;
        double y4 = line2End.y;

        // Calculate the determinant
        double det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        // If the determinant is zero, the lines are parallel
        if (det == 0) {
            return null;
        }

        // Calculate the intersection point
        double intersectionX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / det;
        double intersectionY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / det;

        return new Point(intersectionX, intersectionY);
    }

    private static final double MIN_RECTANGLE_AREA = 500; // Adjust this threshold as needed

    class Line {
        Point p1, p2;
    }
    List<Line> findMidPoints(Point p1, Point p2, Point p3, Point p4, Point pCenter, Point pFar, int depth){
        if (depth == 0){
            return new ArrayList<>();
        }
        Line line = new Line();
        if (pFar == null){
            line.p1 = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            line.p2 = new Point((p3.x + p4.x) / 2, (p3.y + p4.y) / 2);
        } else {
            line.p1 = calculateIntersection(p1, p2, pCenter, pFar);
            line.p2 = calculateIntersection(p3, p4, pCenter, pFar);
        }
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        Point center1 = calculateIntersection(p1, line.p2, line.p1, p4);
        List<Line> lines1 = findMidPoints(p1, line.p1, line.p2, p4, center1, pFar, depth - 1);
        Point center2 = calculateIntersection(line.p1, p3, p2, line.p2);
        List<Line> lines2 = findMidPoints(line.p1, p2, p3, line.p2, center2, pFar, depth - 1);
        lines.addAll(lines1);
        lines.addAll(lines2);
        return lines;
    }

    private Mat processTestFrame(Mat inputFrame){
        // Clear all input frame
        inputFrame.setTo(new Scalar(255, 255, 255));

        Point p1 = new Point(223, 163);
        Point p2 = new Point(1231, 163);
        Point p3 = new Point(1223, 875);
        Point p4 = new Point(223, 868);

        // {223.0, 163.0} {1231.0, 163.0} {223.0, 868.0} {1223.0, 875.0}
//
//        Point p1 = new Point(800, 50);
//        Point p2 = new Point(100, 500);
//        Point p3 = new Point(840, 1020);
//        Point p4 = new Point(1700, 200);
//
//        Point p1 = new Point(100, 100);
//        Point p2 = new Point(900, 100);
//        Point p3 = new Point(900, 500);
//        Point p4 = new Point(100, 500);

        Imgproc.line(inputFrame, p1, p2, new Scalar(0, 0, 0), 2);
        Imgproc.line(inputFrame, p2, p3, new Scalar(0, 0, 0), 2);
        Imgproc.line(inputFrame, p3, p4, new Scalar(0, 0, 0), 2);
        Imgproc.line(inputFrame, p4, p1, new Scalar(0, 0, 0), 2);

        Imgproc.putText(inputFrame, "A", p1, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 2);
        Imgproc.putText(inputFrame, "B", p2, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 2);
        Imgproc.putText(inputFrame, "C", p3, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 2);
        Imgproc.putText(inputFrame, "D", p4, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 2);

        Point pCenter = calculateIntersection(p1, p3, p2, p4);
        Point pFar1 = calculateIntersection(p1, p4, p2, p3);
        Point pFar2 = calculateIntersection(p1, p2, p3, p4);


        long startTime = System.currentTimeMillis();
        List<Line> vLines = findMidPoints(p1, p2, p3, p4, pCenter, pFar1, 6);
        List<Line> hLines = findMidPoints(p1, p4, p3, p2, pCenter, pFar2, 6);
        Log.d("MyLog", "Time to find lines: " + (System.currentTimeMillis() - startTime) + "ms");
        Imgproc.putText(inputFrame, "Lines: " + vLines.size(), new Point(120, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 2);
        for (Line line : vLines){
            Imgproc.line(inputFrame, line.p1, line.p2, new Scalar(0, 0, 255), 2);
        }
        for (Line line : hLines){
            Imgproc.line(inputFrame, line.p1, line.p2, new Scalar(0, 0, 255), 2);
        }

//        Imgproc.circle(inputFrame, pCenter, 12, new Scalar(0, 0, 255), 2);
//        if (pFar1 != null)
//            Imgproc.circle(inputFrame, pFar1, 12, new Scalar(0, 0, 255), 2);


//        Point pCenter = calculateIntersection(p1, p2, p3, p4);
//        Point pFar = calculateIntersection(p1, p3, p2, p4);

        return inputFrame;
    }

    private Mat processFrame(Mat inputFrame) {
        // Create timer to measure processing time
        long startTime = System.currentTimeMillis();

        // Draw text center frame
        String textDisplay = "";
        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(inputFrame, grayImage, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);

//
//        for (int i = 0; i < 4; i++){
//            Rect r = rects[i];
//            Imgproc.line(inputFrame, new Point(r.x + r.width / 3, r.y + r.height / 3),
//                    new Point(r.x + r.width * 2 / 3, r.y + r.height * 2 / 3), new Scalar(0, 0, 0), 1);
//            Imgproc.line(inputFrame, new Point(r.x + r.width * 2 / 3, r.y + r.height / 3),
//                    new Point(r.x + r.width / 3, r.y + r.height * 2 / 3), new Scalar(0, 0, 0), 1);
//        }


        // Threshold the image to create a binary image
        Mat binaryImage = new Mat();
        Imgproc.adaptiveThreshold(grayImage, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);


        // Clear area
        {
            Rect areaClear = new Rect(0, rects[0].height,
                    rects[1].x, rects[2].y - rects[0].height);
            Mat roi = new Mat(binaryImage, areaClear);
            roi.setTo(new Scalar(0));
        }
        {
            Rect areaClear = new Rect((int)rects[2].br().x, rects[2].y,
                    rects[3].x - (int)rects[2].br().x, rects[2].height);
            Mat roi = new Mat(binaryImage, areaClear);
            roi.setTo(new Scalar(0));
        }

        {
            Rect areaClear = new Rect((int) rects[1].br().x, 0, myWidth - (int) rects[1].br().x, myHeight);
            Mat roi = new Mat(binaryImage, areaClear);
            roi.setTo(new Scalar(0));
        }

        Point cornerTL = new Point(rects[0].x + rects[0].width / 3, rects[0].y + rects[0].height / 3);
        Point cornerBR = new Point(rects[3].x + rects[3].width * 2 / 3, rects[3].y + rects[3].height * 2 / 3);
        Imgproc.rectangle(inputFrame, cornerTL, cornerBR, new Scalar(0, 0, 255), 4);
        Rect paperCorner = new Rect(cornerTL, cornerBR);

//
//        if (true){
//            return binaryImage;
//        }

        // Use Canny edge detector to find edges in the image
        Mat edges = new Mat();
        Imgproc.Canny(binaryImage, edges, 50, 200);

        // Dilate the edges to close gaps in contours
        Imgproc.dilate(edges, edges, Mat.ones(3, 3, CvType.CV_8UC1));

        // Vẽ toàn bộ edges lên khung hình
//        inputFrame.setTo(new Scalar(255, 0, 0), edges);

        // Find contours in the image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d("MyLog", "Time to find contours: " + (System.currentTimeMillis() - startTime) + "ms");

        List<Point> topPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();
        List<Point> bottomLeftPoints = new ArrayList<>();

        // Loại bỏ các contour có chứa contour khác bên trong
        List<Rect> markers = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            boolean validWidth = boundingRect.width > 5 && boundingRect.width < 12;
            boolean validHeight = boundingRect.height > 10 && boundingRect.height < 40;
            double area = boundingRect.area();
            boolean validArea = area > 80 && area < 250;
            boolean inCorner = paperCorner.contains(boundingRect.tl());

            if (validWidth && validHeight && validArea && inCorner) {
                markers.add(boundingRect);
            }
        }

        List<Integer> markerIndexes = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++){
            Rect marker = markers.get(i);
            for (int j = 0; j < markers.size(); j++){
                if (i == j){
                    continue;
                }
                Rect otherMarker = markers.get(j);
                if (marker.contains(otherMarker.tl()) && marker.contains(otherMarker.br())){
                    markerIndexes.add(j);
                }
            }
        }
        // Sort markerIndexes
        sort(markerIndexes);
        for (int i = markerIndexes.size() - 1; i >= 0; i--){
            markers.remove((int)markerIndexes.get(i));
        }
        // Draw all markers
        List<Rect> blackMarkers = new ArrayList<>();
        for (Rect marker : markers){
            // Lấy giá trị màu trung bình của marker trong image gray để xác định màu đen hay màu trắng
            Mat markerImage = new Mat(binaryImage, marker);
            Scalar meanIntensity = Core.mean(markerImage);
            double threshold = 200; // Adjust this threshold as needed
            boolean isBlack = meanIntensity.val[0] < threshold;
            // Vẽ giá trị màu trung bình lên marker
            if (isBlack) {
                blackMarkers.add(marker);
            }
        }

        for (Rect rect: blackMarkers) {
            Point tl = rect.tl();
            Point br = rect.br();
            Imgproc.rectangle(inputFrame, tl, br, new Scalar(0, 255, 0), 1);
            if (rects[2].contains(tl)) {
                bottomLeftPoints.add(tl);
            }
            if (tl.x > rects[1].tl().x) {
                rightPoints.add(tl);
            }
            if (tl.y < rects[0].br().y) {
                topPoints.add(tl);
            }
        }
        if (bottomLeftPoints.size() < FixedBottomLeft || rightPoints.size() < FixedRight || topPoints.size() == 0){
            textDisplay += "NOT VALID due to number of points";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }

        // Sắp xếp right points & top points theo thứ tự x & y tăng dần
        sortListPoints(rightPoints, false);
        sortListPoints(topPoints, true);

        // Loại bỏ các điểm ngoại lai do tính chất của các điểm top & right: các điểm đều nằm trên
        // 1 đường thẳng
        List<Point> rightPointsClone = new ArrayList<>();
        for (Point point : rightPoints){
            rightPointsClone.add(new Point(point.y, point.x));
        }
        while (rightPointsClone.size() > FixedRight){
            rightPointsClone.remove(findOutlier(rightPointsClone));
        }
        rightPoints.clear();
        for (Point point : rightPointsClone){
            rightPoints.add(new Point(point.y, point.x));
        }
        while (topPoints.size() > FixedTop) {
            topPoints.remove(findOutlier(topPoints));
        }

        // Loại bỏ điểm ngoại lai của bottom left points, chỉ giữ lại 1 điểm
        Point tl = topPoints.get(0);
        Point tr = topPoints.get(topPoints.size() - 1);
        Point br = rightPoints.get(rightPoints.size() - 1);

        final int DeltaAngle = 4;
        double angleTR = Math.atan2(tr.y - br.y, tr.x - br.x) - Math.atan2(tr.y - tl.y, tr.x - tl.x);
        double angleTRDegree = Math.abs(Math.toDegrees(angleTR));
        if (Math.abs(angleTRDegree - 90) > DeltaAngle){
            textDisplay += "NOT VALID due to angle";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }
        Point bl = bottomLeftPoints.get(0);
        double minDeltaAngle = 9999;
        for (int i = 0; i < bottomLeftPoints.size(); i++){
            Point point = bottomLeftPoints.get(i);
            double angle = Math.toDegrees(Math.atan2(point.y - tl.y, point.x - tl.x)
                    - Math.atan2(point.y - br.y, point.x - br.x));
            while (angle < 0){ angle += 180; }
            while (angle > 180){ angle -= 180; }
            double deltaAngle = Math.abs(angle - angleTRDegree);
            if (deltaAngle < minDeltaAngle){
                minDeltaAngle = deltaAngle;
                bl = point;
            }
        }
        bottomLeftPoints.clear();
        bottomLeftPoints.add(bl);
        double angleBL = Math.atan2(bl.y - tl.y, bl.x - tl.x) - Math.atan2(bl.y - br.y, bl.x - br.x);
        double angleBLDegree = Math.abs(Math.toDegrees(angleBL));
        if (angleBLDegree > 180) angleBLDegree -= 180;
        double angleTL = Math.atan2(tl.y - bl.y, tl.x - bl.x) - Math.atan2(tl.y - tr.y, tl.x - tr.x);
        double angleTLDegree = Math.abs(Math.toDegrees(angleTL));
        if (angleTLDegree > 180) angleTLDegree -= 180;
        double angleBR = Math.atan2(br.y - tr.y, br.x - tr.x) - Math.atan2(br.y - bl.y, br.x - bl.x);
        double angleBRDegree = Math.abs(Math.toDegrees(angleBR));
        if (angleBRDegree > 180) angleBRDegree -= 180;
        if (Math.abs(angleBLDegree - 90) > DeltaAngle
                || Math.abs(angleTLDegree - 90) > DeltaAngle
                || Math.abs(angleBRDegree - 90) > DeltaAngle){
            textDisplay += "NOT VALID due to angle";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }

        Point centerTop = topPoints.get(topPoints.size() / 2);
        Point centerRight = rightPoints.get(rightPoints.size() / 2);

        double ratioCenterTop = (centerTop.x - tl.x) / (tr.x - tl.x);
        double ratioCenterRight = (centerRight.y - tr.y) / (br.y - tr.y);

        if (ratioCenterTop < 0.48 || ratioCenterTop > 0.52 || ratioCenterRight < 0.53 || ratioCenterRight > 0.57){
            textDisplay += "NOT VALID due to center ratio";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }

        Point t1 = topPoints.get(topPoints.size() - 1);
        Point t2 = rightPoints.get(0);
        boolean isValidMatch = t1.x == t2.x && t1.y == t2.y;
        if (!isValidMatch){
            textDisplay += "NOT VALID due to match";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }

        Imgproc.circle(inputFrame, tl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, tr, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, bl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, br, 12, new Scalar(255, 0, 0), 2);

        Point pCenter = calculateIntersection(tl, br, tr, bl);
        Point pFar1 = calculateIntersection(tl, bl, tr, br);
        Point pFar2 = calculateIntersection(tl, tr, br, bl);
        List<Line> vLines = findMidPoints(tl, tr, br, bl, pCenter, pFar1, 10);
        List<Line> hLines = findMidPoints(tl, bl, br, tr, pCenter, pFar2, 10);

        // Sort list vLines and hLines
        Collections.sort(vLines, (o1, o2) -> Double.compare(o1.p1.x, o2.p1.x));
        Collections.sort(hLines, (o1, o2) -> Double.compare(o1.p1.y, o2.p1.y));

        String pointMarks = "0.080922432\t0.048722519\t0.290566038\t0.110516934\t10\t3\n" +
                "0.080922432\t0.146761735\t0.290566038\t0.270350564\t10\t6\n" +
                "0.353878407\t0.77540107\t0.533333333\t0.947712418\t10\t4\n" +
                "0.353878407\t0.536541889\t0.533333333\t0.707070707\t10\t4\n" +
                "0.353878407\t0.297088532\t0.533333333\t0.467023173\t10\t4\n" +
                "0.353878407\t0.058229352\t0.533333333\t0.22875817\t10\t4\n" +
                "0.602515723\t0.86631016\t0.677148847\t0.944741533\t4\t2\n" +
                "0.602515723\t0.777183601\t0.677148847\t0.862150921\t4\t2\n" +
                "0.602515723\t0.629233512\t0.677148847\t0.707664884\t4\t2\n" +
                "0.602515723\t0.540701129\t0.677148847\t0.625074272\t4\t2\n" +
                "0.602515723\t0.389780154\t0.677148847\t0.469399881\t4\t2\n" +
                "0.602515723\t0.301841949\t0.677148847\t0.386215092\t4\t2\n" +
                "0.602515723\t0.151515152\t0.677148847\t0.229352347\t4\t2\n" +
                "0.602515723\t0.066547831\t0.677148847\t0.146167558\t4\t2\n" +
                "0.758071279\t0.841354724\t0.974004193\t0.943553179\t12\t4\n" +
                "0.758071279\t0.689245395\t0.974004193\t0.79144385\t12\t4\n" +
                "0.758071279\t0.535947712\t0.974004193\t0.638146168\t12\t4\n" +
                "0.758071279\t0.383838384\t0.974004193\t0.486036839\t12\t4\n" +
                "0.758071279\t0.230540701\t0.974004193\t0.33392751\t12\t4\n" +
                "0.758071279\t0.07902555\t0.974004193\t0.180629828\t12\t4\n";
       String[] lines = pointMarks.split("\n");
       for (String line : lines) {
           String[] values = line.split("\t");
           double x = Double.parseDouble(values[0]);
           double y = Double.parseDouble(values[1]);
           double x1 = Double.parseDouble(values[2]);
           double y1 = Double.parseDouble(values[3]);
           int w = Integer.parseInt(values[4]);
           int h = Integer.parseInt(values[5]);

           Point rTL = new Point(x, y);
           Point rBR = new Point(x1, y1);

           for (int index = 0; index < h; index++) {
               for (int row = 0; row < w; row++) {
                   Point p1 = new Point(rTL.x + (rBR.x - rTL.x) * row / w, rTL.y + (rBR.y - rTL.y) * index / h);
                   Point p2 = new Point(rTL.x + (rBR.x - rTL.x) * (row + 1) / w, rTL.y + (rBR.y - rTL.y) * index / h);
                   Point p3 = new Point(rTL.x + (rBR.x - rTL.x) * (row + 1) / w, rTL.y + (rBR.y - rTL.y) * (index + 1) / h);
                   Point p4 = new Point(rTL.x + (rBR.x - rTL.x) * row / w, rTL.y + (rBR.y - rTL.y) * (index + 1) / h);

                   if (p1.x < 0 || p1.x >= 1 || p1.y < 0 || p1.y >= 1
                           || p2.x < 0 || p2.x >= 1 || p2.y < 0 || p2.y >= 1
                           || p3.x < 0 || p3.x >= 1 || p3.y < 0 || p3.y >= 1
                           || p4.x < 0 || p4.x >= 1 || p4.y < 0 || p4.y >= 1){
                       continue;
                   }

                   Line vline1 = vLines.get((int) (p1.x * vLines.size()));
                   Line hLine1 = hLines.get((int) (p1.y * hLines.size()));
                   Point ref1 = calculateIntersection(vline1.p1, vline1.p2, hLine1.p1, hLine1.p2);

                   Line vline2 = vLines.get((int) (p2.x * vLines.size()));
                   Line hLine2 = hLines.get((int) (p2.y * hLines.size()));
                   Point ref2 = calculateIntersection(vline2.p1, vline2.p2, hLine2.p1, hLine2.p2);

                   Line vline3 = vLines.get((int) (p3.x * vLines.size()));
                   Line hLine3 = hLines.get((int) (p3.y * hLines.size()));
                   Point ref3 = calculateIntersection(vline3.p1, vline3.p2, hLine3.p1, hLine3.p2);

                   Line vline4 = vLines.get((int) (p4.x * vLines.size()));
                   Line hLine4 = hLines.get((int) (p4.y * hLines.size()));
                   Point ref4 = calculateIntersection(vline4.p1, vline4.p2, hLine4.p1, hLine4.p2);


                   Imgproc.line(inputFrame, ref1, ref2, new Scalar(255, 0, 0), 2);
                   Imgproc.line(inputFrame, ref2, ref3, new Scalar(255, 0, 0), 2);
                   Imgproc.line(inputFrame, ref3, ref4, new Scalar(255, 0, 0), 2);
                   Imgproc.line(inputFrame, ref4, ref1, new Scalar(255, 0, 0), 2);
               }
           }
       }


//        {
//            String examCode = "";
//            // Ma de thi
//            Point rTL = new Point(0.077987421, 0.047534165);
//            Point rBR = new Point(0.288050314, 0.10932858);
//
//            int[] codes = new int[]{ -1, -1, -1 };
//            for (int index = 0; index < 3; index++){
//                for (int row = 0; row < 10; row++){
//                    Point p1 = new Point(rTL.x + (rBR.x - rTL.x) * row / 10, rTL.y + (rBR.y - rTL.y) * index / 3);
//                    Point p2 = new Point(rTL.x + (rBR.x - rTL.x) * (row + 1) / 10, rTL.y + (rBR.y - rTL.y) * index / 3);
//                    Point p3 = new Point(rTL.x + (rBR.x - rTL.x) * (row + 1) / 10, rTL.y + (rBR.y - rTL.y) * (index + 1) / 3);
//                    Point p4 = new Point(rTL.x + (rBR.x - rTL.x) * row / 10, rTL.y + (rBR.y - rTL.y) * (index + 1) / 3);
//
//                    Point ref1 = calculatePointRef(tl, tr, bl, br, p1.x, p1.y);
//                    Point ref2 = calculatePointRef(tl, tr, bl, br, p2.x, p2.y);
//                    Point ref3 = calculatePointRef(tl, tr, bl, br, p3.x, p3.y);
//                    Point ref4 = calculatePointRef(tl, tr, bl, br, p4.x, p4.y);
//
//                    Imgproc.line(inputFrame, ref1, ref2, new Scalar(255, 0, 0), 2);
//                    Imgproc.line(inputFrame, ref2, ref3, new Scalar(255, 0, 0), 2);
//                    Imgproc.line(inputFrame, ref3, ref4, new Scalar(255, 0, 0), 2);
//                    Imgproc.line(inputFrame, ref4, ref1, new Scalar(255, 0, 0), 2);
//
//                    Rect interest = new Rect(ref1, ref3);
//                    Mat roi = new Mat(grayImage, interest);
//                    Imgproc.GaussianBlur(roi, roi, new Size(5, 5), 0);
//                    Imgproc.threshold(roi, roi, 150, 255, Imgproc.THRESH_BINARY);
//
//                    Scalar mean = Core.mean(roi);
//                    double meanValue = mean.val[0];
//                    if (meanValue < ThresholdMean){
//                        codes[2 - index] = row;
//                    }
//                }
//            }
//            boolean isValid = true;
//            for (int i = 0; i < 3; i++){
//                if (codes[i] == -1){
//                    examCode += "X";
//                    isValid = false;
//                } else {
//                    examCode += codes[i];
//                }
//            }
//            if (!isValid){
//                examCode += " - NOT VALID";
//            }
//            final String finalExamCode = examCode;
//            runOnUiThread(() -> textViewInfo.setText("Mã đề thi: " + finalExamCode));
//        }


        // Draw all right points, top points and bottom left points
        for (Point point : rightPoints){
            Imgproc.circle(inputFrame, point, 5, new Scalar(0, 0, 255), 2);
        }
        for (Point point : topPoints){
            Imgproc.circle(inputFrame, point, 5, new Scalar(0, 255, 255), 2);
        }
        for (Point point : bottomLeftPoints){
            Imgproc.circle(inputFrame, point, 5, new Scalar(0, 255, 255), 2);
        }
        Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);

        return inputFrame;
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return processFrame(inputFrame.rgba());
    }

    public double khoangCach(Point p, Point p2) {
        return Math.sqrt(Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));
    }

    public void getCountContour(ArrayList<MatOfPoint> contours, int i, int k, float rate, int[] check) {
        Rect rect = Imgproc.boundingRect((MatOfPoint) contours.get(i));
        int area = (int) Imgproc.contourArea((Mat) contours.get(i));
        int w = rect.width;
        int h = rect.height;
        float ratio1 = ((float) w) / ((float) h);
        double r1 = ((double) (area - Core.countNonZero(this.corners1[k].submat(rect)))) / ((double) area);
        if (r1 > this.imageProcessing.getTH(rate) && ratio1 > 0.8f && ratio1 < 1.2f) {
            Rect rect2 = new Rect(this.rects[k].x + rect.x, this.rects[k].y + rect.y, rect.width, rect.height);
            if (check[k] == 0) {
                this.count++;
                Mat mat = this.mRga;
//                Point point = new Point((double) (this.rects[k].x + rect.x), (double) (this.rects[k].y + rect.y));
                Point point = new Point((double) ((rect.width + rect.x) + this.rects[k].x), (double) ((rect.height + rect.y) + this.rects[k].y));
                btFPoint.add(point);//add
                Imgproc.rectangle(mat, point, point, new Scalar(255.0d, 0.0d, 0.0d), 5);
//                this.points.add(this.imageProcessing.getPoint(rect2));
                if (Math.min(rect2.width, rect2.height) < halfRect) {
                    halfRect = Math.min(rect2.width, rect2.height) / 2;
                }
                check[k] = 1;
            }
        }
    }

    public Bitmap matToBitmap(Mat mat) {
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


    int minH = 255, maxH = 0, minS = 255, maxS = 0, minV = 255, maxV = 0;
    boolean touch;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touch = true;
        return false;
    }

    double calculateAngle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (Math.atan2(dy1, dx1) - Math.atan2(dy2, dx2)) * 180 / Math.PI;
    }
}
