package com.khainv9.tracnghiem.scan;

import static org.opencv.core.CvType.CV_8UC3;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "CameraActivity";


    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView textViewInfo;
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

    List<Line> vLines = new ArrayList<>();
    List<Line> hLines = new ArrayList<>();

    void calculateVHLines(Point tl, Point tr, Point br, Point bl){
        Point pCenter = GeometryUtils.calculateIntersection(tl, br, tr, bl);
        Point pFar1 = GeometryUtils.calculateIntersection(tl, bl, tr, br);
        Point pFar2 = GeometryUtils.calculateIntersection(tl, tr, br, bl);
        vLines = GeometryUtils.findMidPoints(tl, tr, br, bl, pCenter, pFar1, 10);
        hLines = GeometryUtils.findMidPoints(tl, bl, br, tr, pCenter, pFar2, 10);
        Collections.sort(vLines, (o1, o2) -> Double.compare(o1.p1.x, o2.p1.x));
        Collections.sort(hLines, (o1, o2) -> Double.compare(o1.p1.y, o2.p1.y));
    }

    Point getRefPoint(double x, double y){
        Line vLine1 = vLines.get((int) (x * vLines.size()));
        Line hLine1 = hLines.get((int) (y * hLines.size()));
        return GeometryUtils.calculateIntersection(vLine1.p1, vLine1.p2, hLine1.p1, hLine1.p2);
    }

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


        template = Template.createDefaultTemplate();


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
        if (bottomLeftPoints.size() < template.fixedBottomLeft || rightPoints.size() < template.fixedRight || topPoints.size() == 0){
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
        while (rightPointsClone.size() > template.fixedRight){
            rightPointsClone.remove(findOutlier(rightPointsClone));
        }
        rightPoints.clear();
        for (Point point : rightPointsClone){
            rightPoints.add(new Point(point.y, point.x));
        }
        while (topPoints.size() > template.fixedTop) {
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

        calculateVHLines(tl, tr, br, bl);

        ExamPaper examPaper = new ExamPaper();
        for (int sectionIndex = 0; sectionIndex < template.sectionAreas.size(); sectionIndex++){

            SectionArea sectionArea = template.sectionAreas.get(sectionIndex);
            double xMin = sectionArea.xMin;
            double xMax = sectionArea.xMax;
            double yMin = sectionArea.yMin;
            double yMax = sectionArea.yMax;

            int numRow = sectionArea.numRow;
            int numCol = sectionArea.numCol;
            int type = sectionArea.type;
            List<PointMark> pointMarks = new ArrayList<>();
            // Đọc toàn bộ ô vuông theo hàng & cột, kiểm tra xem ô có được đánh dấu hay chưa (có điểm đen hay không)
            for (int row = 0; row < numRow; row++) {
                for (int col = 0; col < numCol; col++) {
                    Point ref1 = getRefPoint(xMin + (xMax - xMin) * col / numCol, yMin + (yMax - yMin) * row / numRow);
                    Point ref3 = getRefPoint(xMin + (xMax - xMin) * (col + 1) / numCol, yMin + (yMax - yMin) * (row + 1) / numRow);

                    Rect interest = new Rect(ref1, ref3);
                    Mat roi = new Mat(grayImage, interest);
                    Imgproc.GaussianBlur(roi, roi, new Size(5, 5), 0);
                    Imgproc.threshold(roi, roi, 100, 255, Imgproc.THRESH_BINARY);
                    Scalar mean = Core.mean(roi);
                    double meanValue = mean.val[0];
                    if (meanValue < 233){
                        Point center = new Point((ref1.x + ref3.x) / 2, (ref1.y + ref3.y) / 2);
                        Imgproc.circle(inputFrame, center,10, new Scalar(0, 0, 255), 2);
                        PointMark mark = new PointMark();
                        mark.row = row;
                        mark.col = col;
                        mark.type = type;
                        mark.point = center;
                        pointMarks.add(mark);
                    }
                    Scalar squareArea = new Scalar(0, 200, 0);
                    Imgproc.rectangle(inputFrame, ref1, ref3, squareArea, 1);
                }
            }

            List<Integer> values = new ArrayList<>();
            // Kiểm tra giá trị của các marker, warning những chỗ khoanh sai
            switch (type){
                case 0: case 1: case 4: {
                    // Tất cả các hàng đều phải có 1 dấu
                    int[] countMarks = new int[numRow];
                    int[] markValues = new int[numRow];
                    Arrays.fill(markValues, -1);
                    for (PointMark pointMark : pointMarks) {
                        if (pointMark.row < numRow) {
                            countMarks[pointMark.row]++;
                            markValues[pointMark.row] = pointMark.col;
                        }
                    }
                    for (int row = 0; row < numRow; row++) {
                        if (countMarks[row] != 1) {
                            Point ref1 = getRefPoint(xMin, yMin + (yMax - yMin) * row / numRow);
                            Point ref3 = getRefPoint(xMax, yMin + (yMax - yMin) * (row + 1) / numRow);
                            Scalar squareArea = new Scalar(255, 0, 0);
                            Imgproc.rectangle(inputFrame, ref1, ref3, squareArea, 2);
                        }
                    }
                    for (int i = numRow - 1; i >= 0; i--){
                        if (countMarks[i] != 1){
                            values.add(-1);
                        } else {
                            values.add(markValues[i]);
                        }
                    }
                    break;
                }
                case 2: case 3: {
                    // Tất cả các cột đều phải có 1 dấu
                    int[] countMarks = new int[numCol];
                    int[] markValues = new int[numCol];
                    Arrays.fill(markValues, -1);
                    for (PointMark pointMark : pointMarks) {
                        if (pointMark.col < numCol) {
                            countMarks[pointMark.col]++;
                            markValues[pointMark.col] = pointMark.row;
                        }
                    }
                    for (int col = 0; col < numCol; col++) {
                        if (countMarks[col] != 1) {
                            Point ref1 = getRefPoint(xMin + (xMax - xMin) * col / numCol, yMin);
                            Point ref3 = getRefPoint(xMin + (xMax - xMin) * (col + 1) / numCol, yMax);
                            Scalar squareArea = new Scalar(255, 0, 0);
                            Imgproc.rectangle(inputFrame, ref1, ref3, squareArea, 2);
                        }
                    }
                    for (int i = 0; i < numCol; i++){
                        if (countMarks[i] != 1){
                            values.add(-1);
                        } else {
                            values.add(markValues[i]);
                        }
                    }
                    break;
                }
            }

            Section section = new Section();
            section.type = type;
            section.pointMarks = pointMarks;
            section.values = values;

            examPaper.sections.add(section);
        }

        // Lấy tham số exam code
        Section examCodeSection = examPaper.sections.get(0);
        String examCode = "";
        for (int i = 0; i < 3; i++){
            if (i < examCodeSection.values.size() && examCodeSection.values.get(i) != -1){
                examCode += examCodeSection.values.get(i);
            } else {
                examCode += "_";
            }
        }

        examPaper.examCode = examCode;

        // Lấy tham số student code
        Section studentCodeSection = examPaper.sections.get(1);
        String studentId = "";
        for (int i = 0; i < 6; i++){
            if (i < studentCodeSection.values.size() && studentCodeSection.values.get(i) != -1){
                studentId += studentCodeSection.values.get(i);
            } else {
                studentId += "_";
            }
        }
        examPaper.studentId = studentId;

        // Lấy đáp án phần 1
        for (int i = 2; i < 6; i++){
            Section section = examPaper.sections.get(i);
            for (int j = 0; j < 10; j++){
                if (section.values.get(j) >= 0 && section.values.get(j) < 4){
                    examPaper.chapter1Answer += ("ABCD".charAt(section.values.get(j)));
                } else {
                    examPaper.chapter1Answer += ('_');
                }
            }
        }
        for (int i = 6; i < 14; i++){
            Section section = examPaper.sections.get(i);
            for (int j = 0; j < 4; j++){
                if (section.values.get(j) >= 0 && section.values.get(j) < 2){
                    examPaper.chapter2Answer += ("TF".charAt(section.values.get(j)));
                } else {
                    examPaper.chapter2Answer += ('_');
                }
            }
        }
        for (int i = 14; i < 20; i++){
            Section section = examPaper.sections.get(i);
            for (int j = 0; j < 4; j++){
                if (section.values.get(j) >= 0 && section.values.get(j) < 12){
                    examPaper.chapter3Answer += ("-,0123456789".charAt(section.values.get(j)));
                } else {
                    examPaper.chapter3Answer += ('_');
                }
            }
        }

        Log.d("MyLog", "Exam paper info: " + examPaper.toString());
        final String textResult = "Mã đề: " + examCode + "\n SBD: " + studentId;
        runOnUiThread(() -> textViewInfo.setText("Mã đề thi: " + textResult));

        Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);

        return inputFrame;
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return processFrame(inputFrame.rgba());
    }

    boolean touch;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touch = true;
        return false;
    }
}
