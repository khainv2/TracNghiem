package chamdiemthi.datn.opencv_camera_1.scan;

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
import android.widget.Toast;

import com.rantea.opencv_camera_1.R;

import chamdiemthi.datn.opencv_camera_1.app.Utils;
import chamdiemthi.datn.opencv_camera_1.models.BaiThi;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
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
    private static final int FixedTop = 40;
    private static final int FixedRight = 18;
    private static final int FixedBottomLeft = 1;

    private CameraBridgeViewBase mOpenCvCameraView;
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

        //lấy vị trí của bài thi trong intent gửi đến
        int i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
        baiThi = Utils.dsBaiThi.get(i);
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




//    public void getFourSquare(float rate) {
//        for (int i = 0; i < 4; i++) {
//            this.corners[i] = this.mRga.submat(this.rects[i]);
//            this.corners1[i] = this.corners[i].clone();
//            this.corners[i].convertTo(this.corners1[i], -1, 1.0d, 100.0d);
//            Imgproc.cvtColor(this.corners1[i], this.corners1[i], 6);
//            Imgproc.GaussianBlur(this.corners1[i], this.corners1[i], new Size(3.0d, 3.0d), 2.0d);
//            Imgproc.adaptiveThreshold(this.corners1[i], this.corners1[i], 255.0d, 0, 0, 31, (double) (5.0f * rate));
//        }
//    }

    private static double[] findSlopeAndIntercept(List<Point> points){
        double[] lineParams = new double[2];
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        String str = "";
        for (Point point : points){
            str += point.x + " " + point.y + "| ";
            sumX += point.x;
            sumY += point.y;
            sumXY += point.x * point.y;
            sumX2 += point.x * point.x;
        }
        double n = points.size();
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        Log.d("MyLog", str + "Slope: " + slope + " Intercept: " + intercept);
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

    private double distance(Point p1, Point p2){
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

    private static final double MIN_RECTANGLE_AREA = 5000; // Adjust this threshold as needed
    private Mat processFrame(Mat inputFrame) {
        // Draw text center frame
        String textDisplay = "";
//        Imgproc.putText(inputFrame, textDisplay, new Point(inputFrame.cols() / 2 - textDisplay.length() * 5, inputFrame.rows() / 2),
//                Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(1, 1, 1), 1);


        // Convert the image to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(inputFrame, gray, Imgproc.COLOR_RGBA2GRAY);

        // Apply GaussianBlur to reduce noise and help contour detection
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

        // Use Canny edge detector to find edges in the image
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 200);

        // Dilate the edges to close gaps in contours
        Imgproc.dilate(edges, edges, Mat.ones(3, 3, CvType.CV_8UC1));

        // Vẽ toàn bộ edges lên khung hình
//        inputFrame.setTo(new Scalar(255, 0, 0), edges);

        // Find contours in the image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        List<Point> topPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();
        List<Point> bottomLeftPoints = new ArrayList<>();

        // Iterate through each contour and find rectangular shapes
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

            double epsilon = 0.02 * Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
            MatOfPoint mp = new MatOfPoint(contour2f.toArray());

            if (!Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray())))
                continue;

            Mat mask = new Mat(gray.size(), CvType.CV_8UC1, Scalar.all(0));
            Imgproc.drawContours(mask, Collections.singletonList(contour), 0, new Scalar(255), -1);

            Rect boundingRect = Imgproc.boundingRect(contour);
            double area = boundingRect.area();
            if (area > 300){
                continue;
            }
            boolean isValidPosition = false;
            if (rects[2].contains(boundingRect.tl())) {
                isValidPosition = true;
                bottomLeftPoints.add(boundingRect.tl());
            }
            if (boundingRect.tl().x > rects[1].tl().x) {
                isValidPosition = true;
                rightPoints.add(boundingRect.tl());
            }
            if (boundingRect.tl().y < rects[0].br().y) {
                isValidPosition = true;
                topPoints.add(boundingRect.tl());
            }

            if (isValidPosition){
                Imgproc.rectangle(inputFrame, boundingRect.tl(), boundingRect.br(), new Scalar(0, 255, 0), 2);
            }
        }

        for (int i = 0; i < 4; i++){
            Imgproc.rectangle(inputFrame, new Point(rects[i].x, rects[i].y),
                    new Point(rects[i].x + rects[i].width, rects[i].y + rects[i].height),
                    new Scalar(255, 0, 0), 3);
        }

        sortListPoints(rightPoints, false);
        sortListPoints(topPoints, true);

        // Clone rightPoints with exchange x and y
        List<Point> rightPointsClone = new ArrayList<>();
        for (Point point : rightPoints){
            rightPointsClone.add(new Point(point.y, point.x));
        }
        while (rightPointsClone.size() > FixedRight){
            rightPointsClone.remove(findOutlier(rightPointsClone));
        }

        // Update clone to origin rightPoints
        rightPoints.clear();
        for (Point point : rightPointsClone){
            rightPoints.add(new Point(point.y, point.x));
        }

        while (topPoints.size() > FixedTop) {
            topPoints.remove(findOutlier(topPoints));
        }

        if (bottomLeftPoints.size() != FixedBottomLeft || rightPoints.size() != FixedRight || topPoints.size() != FixedTop){
            textDisplay += "NOT VALID due to number of points";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }


        Point tl = topPoints.get(0);
        Point tr = topPoints.get(topPoints.size() - 1);

        Point bl = bottomLeftPoints.get(0);
        Point br = rightPoints.get(rightPoints.size() - 1);
        // Kiểm tra góc giữa 4 cặp trên
        double angleTL = Math.abs(Math.toDegrees(Math.atan2(tl.y - bl.y, tl.x - bl.x)));
        double angleTR = Math.abs(Math.toDegrees(Math.atan2(tr.y - br.y, tr.x - br.x)));
        double angleBL = Math.abs(Math.toDegrees(Math.atan2(bl.y - tl.y, bl.x - tl.x)));
        double angleBR = Math.abs(Math.toDegrees(Math.atan2(br.y - tr.y, br.x - tr.x)));

        final double DeltaAngle = 10;
        boolean isValidAngle = Math.abs(angleTL - 90) < DeltaAngle && Math.abs(angleTR - 90) < DeltaAngle &&
                Math.abs(angleBL - 90) < DeltaAngle && Math.abs(angleBR - 90) < DeltaAngle;
        String angleTLStr = String.format("%.2f", angleTL);
        String angleTRStr = String.format("%.2f", angleTR);
        String angleBLStr = String.format("%.2f", angleBL);
        String angleBRStr = String.format("%.2f", angleBR);

        textDisplay += "Angle TL: " + angleTLStr + " Angle TR: " + angleTRStr + " Angle BL: " + angleBLStr + " Angle BR: " + angleBRStr;
        if (!isValidAngle){
            textDisplay += "NOT VALID due to angle";
            Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            return inputFrame;
        }

        double distanceTLTR = distance(tl, tr);
        for (int i = 0; i < topPoints.size() - 1; i++){
            double d = distance(topPoints.get(i), topPoints.get(i + 1));
            double ratio = d / distanceTLTR;

        }




//        textDisplay += "Angle TLTR: " + angleTLTR + " Angle TRBR: " + angleTRBR + " Angle BRBL: " + angleBRBL + " Angle BLTL: " + angleBLTL;

        Imgproc.circle(inputFrame, tl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, tr, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, bl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, br, 12, new Scalar(255, 0, 0), 2);

        // Draw lines from tl, tr, bl, br
        Imgproc.line(inputFrame, tl, tr, new Scalar(0, 0, 0), 1);
        Imgproc.line(inputFrame, tr, br, new Scalar(0, 0, 0), 1);
        Imgproc.line(inputFrame, br, bl, new Scalar(0, 0, 0), 1);
        Imgproc.line(inputFrame, bl, tl, new Scalar(0, 0, 0), 1);


        for (int i = 0; i < topPoints.size(); i++){
            double distanceTopToTL = distance(topPoints.get(i), tl);
            Point refPointBottom = new Point(bl.x + (br.x - bl.x) * distanceTopToTL / distanceTLTR,
                    bl.y + (br.y - bl.y) * distanceTopToTL / distanceTLTR);
            Imgproc.line(inputFrame, topPoints.get(i), refPointBottom, new Scalar(0, 0, 0), 1);
        }

        double distanceTRBR = distance(tr, br);
        for (int i = 0; i < rightPoints.size(); i++){
            double distanceRightToTR = distance(rightPoints.get(i), tr);
            Point refPointLeft = new Point(tl.x + (bl.x - tl.x) * distanceRightToTR / distanceTRBR,
                    tl.y + (bl.y - tl.y) * distanceRightToTR / distanceTRBR);
            Imgproc.line(inputFrame, rightPoints.get(i), refPointLeft, new Scalar(0, 0, 0), 1);
        }


        // Draw all right points, top points and bottom left points
        for (Point point : rightPoints){
            Imgproc.circle(inputFrame, point, 5, new Scalar(0, 0, 255), 2);
        }
        for (Point point : topPoints){
            Imgproc.circle(inputFrame, point, 5, new Scalar(0, 255, 255), 2);
        }

        Imgproc.putText(inputFrame, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);



//
//        Imgproc.cvtColor(frameThreshed, dst, Imgproc.COLOR_HSV2RGB_FULL);

        return inputFrame;
//        Imgproc.cvtColor(inputFrame, bgrFrame, Imgproc.COLOR_RGBA2BGR);
////        Imgproc.bilateralFilter(dst, bilateralImage, 9, 75, 75);
////        Photo.fastNlMeansDenoisingColored(bilateralImage, denoisedImage, 10, 10, 7, 21);
//        Imgproc.cvtColor(bgrFrame, hsvImage, Imgproc.COLOR_BGR2HSV);
////
//        Core.inRange(hsvImage, new Scalar(0, 0, 0), new Scalar(255, 255, 30), frameThreshed);
//
//        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
//        Imgproc.findContours(frameThreshed, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        Log.d("MyLog", "Contours count: " + contours.size());
//        Imgproc.drawContours(inputFrame, contours, -1, new Scalar(0, 255, 0), 2);

//        Imgproc.GaussianBlur(imgray, imgray, new Size(3.0d, 3.0d), 2.0d);
//        Imgproc.adaptiveThreshold(imgray, imgray, 255.0d, 0, 0, 31, 5.0d);




        // Thực hiện xử lý trên khung hình ở đây
        // Ví dụ: vẽ một hình tròn màu đỏ
//        Imgproc.circle(inputFrame, new Point(inputFrame.cols() / 2, inputFrame.rows() / 2),
//                50, new Scalar(255, 0, 0), 3);

//        Core.inRange(inputFrame, new Scalar(0, 230, 230), new Scalar(255, 255, 255), inputFrame);


//        Imgproc.cvtColor(inputFrame, inputFrame, Imgproc.COLOR_RGBA2GRAY);
//        Imgproc.GaussianBlur(inputFrame, inputFrame, new Size(3.0d, 3.0d), 2.0d);
//        Imgproc.adaptiveThreshold(inputFrame, inputFrame, 255.0d, 0, 0, 31, 5.0d);
//        for (int i = 0; i < 4; i++) {
//            Mat mat = inputFrame.submat(rects[i]);
//            Point point = new Point(rects[i].x, rects[i].y);
//            point = new Point(rects[i].x + rects[i].width, rects[i].y + rects[i].height);
//            Imgproc.rectangle(inputFrame, point, point, new Scalar(255.0d, 0.0d, 0.0d), 5);
//        }


        // Hiển thị khung hình đã xử lý
//        return inputFrame;
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat processedFrame = processFrame(inputFrame.rgba());

        // Hiển thị khung hình đã xử lý
        return processedFrame;

//
//        //lấy tấm nền từ camera
//        this.mRga1 = inputFrame.rgba();
//        //sao chép tấm nền
//        this.mRga = this.mRga1.submat(this.startY, this.startY + this.myHeight, 0, this.myWidth);
//        //tỉ lệ khung hình
//        float rate = ((float) this.myWidth) / 1280.0f;
//        //tìm kiếm 4 ô vuông với tỷ lệ
//        getFourSquare(rate);
//        //biến đếm số ô vuông tìm thấy
//        this.count = 0;
//        int[] check = new int[4];
//        //khu vực quét
//        int k = 0;
//        btFPoint.clear();
//        //bắt đầu quét. vòng while (quét tại 4 khu vực, nếu 4 khu vực tìm thấy 4 điểm => ok)
//        while (k < 4) {
//            //dan sách đường viền tìm thấy
//            ArrayList<MatOfPoint> contours = new ArrayList();
//            //xử lý đường viền hình ảnh
//            Imgproc.findContours(this.corners1[k], contours, this.hierarchy, 1, 2, new Point(0.0d, 0.0d));
//            int i = 0;
//            //kiểm tra từng đường viền (giống như vòng for i++)
//            while (i < contours.size()) {
//
//                // tìm kiếm tọa độ 4 ô vuông
//                getCountContour(contours, i, k, rate, check);
//                if (this.count == 4) { //nếu tìm thấy cả 4 ô vuông tại 4 góc giấy
//                    //xóa ds đường viền
//                    contours.clear();
//                    //dừng quét (nhảy sang vùng quét 5 => ko tồn tại)
//                    k++;
//                    //tọa độ 4 ô vuông
//                    Point p0 = btFPoint.get(0);
//                    Point p1 = btFPoint.get(1);
//                    Point p2 = btFPoint.get(2);
//                    Point p3 = btFPoint.get(3);
//
//                    //vẽ khung hình vuông bằng cách nối các điểm
//                    Imgproc.line(mRga1, p0, p1, new Scalar(255, 128, 128, 255), 3);
//                    Imgproc.line(mRga1, p1, p3, new Scalar(255, 128, 128, 255), 3);
//                    Imgproc.line(mRga1, p3, p2, new Scalar(255, 128, 128, 255), 3);
//                    Imgproc.line(mRga1, p2, p0, new Scalar(255, 128, 128, 255), 3);
//
//                    //tính toán chiều dài, rộng của khung vuông
//                    double w = khoangCach(p0, p1), h = khoangCach(p0, p2);
//                    //nếu người dùng chạm màn hình thì bắt đầu quét để lưu bài thi
//                    if (touch) {
//                        //reset trạng thái chạm
//                        touch = false;
//                        //lấy bài làm quét trong giấy thi
//                        ArrayList<String> baiLam = template.scanBaiLam(w, h, p0, mRga);
//                        //lấy mã đề quét trong giấy thi
//                        String maDe = template.scanMaDe(w, h, p0, mRga1);
//                        //lấy số báo danh quét trong giấy thi
//                        String sbd = template.scanSBD(w, h, p0, mRga1);
//                        //chuyển bài thi sang dạng hình ảnh bitmap để lưu trữ
//                        Bitmap save = matToBitmap(mRga1);
//                        //Tạo đối tượng điểm thi (để lưu trữ)
//                        DiemThi diemThi = new DiemThi(sbd, baiThi.maBaiThi, maDe, save, baiLam.toArray(new String[baiLam.size()]));
//                        //Cập nhật thông tin bài thi này trong bảng điểm thi
//                        Utils.update(diemThi);
//                    } else
//                        //nếu không chạm thì quét và hiển thị bình thường
//                        template.scan(w, h, p0, mRga1);
//                } else {
//                    contours.remove(i);
//                    i++;
//                }
//            }
//            contours.clear();
//            k++;
//        }
//        for (k = 0; k < 4; k++) {
//            check[k] = 0;
//        }
//        return this.mRga1;
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

    //    public boolean getMidRect(int widthCal, int heightCal, float rate) {
//        if (this.points.size() != 4) {
//            return false;
//        }
//        this.points = this.imageProcessing.sortCorner(this.points, this.myWidth, this.myHeight, halfRect);
//        for (int v = 0; v < 4; v++) {
//            Log.e("sort" + v, ((Point) this.points.get(v)).x + " " + ((Point) this.points.get(v)).y);
//        }
//        Log.e("sortCount", this.points.size() + " ");
//        Log.e("success", "successful");
//        this.arraySrc = new Point[]{(Point) this.points.get(0), (Point) this.points.get(1), (Point) this.points.get(2), (Point) this.points.get(3)};
//        Mat matOfPoint2f = new MatOfPoint2f(this.arraySrc);
//        this.targets.add(new Point(0.0d, 0.0d));
//        this.targets.add(new Point((double) (widthCal - 1), 0.0d));
//        this.targets.add(new Point(0.0d, (double) ((this.myHeight - 1) - heightCal)));
//        this.targets.add(new Point((double) (widthCal - 1), (double) ((this.myHeight - 1) - heightCal)));
//        this.arrayDst = new Point[]{(Point) this.targets.get(0), (Point) this.targets.get(1), (Point) this.targets.get(2), (Point) this.targets.get(3)};
//        matOfPoint2f = new MatOfPoint2f(this.arrayDst);
//        Size size = new Size((double) widthCal, (double) (this.myHeight - heightCal));
//        Mat perspectiveMatrix = Imgproc.getPerspectiveTransform(matOfPoint2f, matOfPoint2f);
//        matOfPoint2f = new Mat(widthCal, this.myHeight - heightCal, CvType.CV_8UC4);
//        long timeCV = System.currentTimeMillis();
//        long perTime = System.currentTimeMillis();
//        Imgproc.warpPerspective(this.mRga, matOfPoint2f, perspectiveMatrix, size);
//        Log.d("vinhtuanleTimePer", (System.currentTimeMillis() - perTime) + " ");
//        long perTime1 = System.currentTimeMillis();
//        this.clone = matOfPoint2f;
//        Log.d("vinhtuanleTimeGan", (System.currentTimeMillis() - perTime1) + " ");
//        long colorTime = System.currentTimeMillis();
//        Imgproc.cvtColor(matOfPoint2f, this.mGray, 6);
//        Log.d("vinhtuanleTimeColor", (System.currentTimeMillis() - colorTime) + " ");
//        long gaussTime = System.currentTimeMillis();
//        Imgproc.GaussianBlur(this.mGray, this.mGaussBlur, new Size(3.0d, 3.0d), 2.0d);
//        Log.d("vinhtuanleTimeGauss", (System.currentTimeMillis() - gaussTime) + " ");
//        int threadhold = this.imageProcessing.getThreadhold(rate);
//        double subtract = this.imageProcessing.getSubtract(rate);
//        long timeThread = System.currentTimeMillis();
//        Imgproc.adaptiveThreshold(this.mGaussBlur, this.mBinary, 255.0d, 0, 0, threadhold, subtract);
//        Imgproc.adaptiveThreshold(this.mGaussBlur, this.mBinary1, 255.0d, 0, 0, threadhold, subtract);
//        Log.d("vinhtuanleTimeTH", (System.currentTimeMillis() - timeThread) + " ");
//        Log.d("vinhtuanleTimeCV", (System.currentTimeMillis() - timeCV) + " ");
//        Log.e("success", "tranformed row" + this.mBinary.rows() + "col" + this.mBinary.cols());
//        this.tempPoints1 = this.imageProcessing.getMidPoints(this.targets, 40);
//        Log.d("vinhtuanleCheck", this.numberArea + " " + this.numberMid + " " + this.numberSort2);
//        double[] formArray = new double[this.numberMid];
//        for (int x = 0; x < this.numberMid; x++) {
//
//            int halfDraw = this.imageProcessing.getHalfRect(40, (float) halfRect);
//
//            Log.d("vinhtuanleHalfDraw", halfDraw + " " + halfRect);
//            this.tempPoints1[x][0] = (double) ((int) this.tempPoints1[x][0]);
//            this.tempPoints1[x][1] = (double) ((int) this.tempPoints1[x][1]);
//            Log.d("getPoint", this.tempPoints1[x][0] + " " + this.tempPoints1[x][1]);
//            Log.e("historyHalfRect" + x, halfDraw + " " + (this.tempPoints1[x][0] - ((double) halfDraw)) + " " + (this.tempPoints1[x][1] - ((double) halfDraw)));
//            Mat mat = this.imageProcessing.getMat(this.mBinary, new Point(this.tempPoints1[x][0], this.tempPoints1[x][1]), halfDraw, halfDraw);
//            long timeHistory = System.currentTimeMillis();
//            Point center = this.imageProcessing.getHistogram(mat, new Point(this.tempPoints1[x][0] - ((double) halfDraw), this.tempPoints1[x][1] - ((double) halfDraw)));
//            Log.d("vinhtuanleTimeHistory", (System.currentTimeMillis() - timeHistory) + " ");
//            long timeDraw = System.currentTimeMillis();
//            Imgproc.circle(this.clone, new Point(center.x, center.y), 2, new Scalar(255.0d, 0.0d, 0.0d, 1.0d), 1);
//            Log.d("vinhtuanleTimeCircle", (System.currentTimeMillis() - timeDraw) + " ");
//            this.points1[x][0] = center.x;
//            this.points1[x][1] = center.y;
//            int area = mat.width() * mat.height();
//            formArray[x] = ((double) (area - Core.countNonZero(mat))) / ((double) area);
//        }
//        Log.d("vinhtuanleTimeCheckForm", (System.currentTimeMillis() - System.currentTimeMillis()) + " ");
//        this.points2 = this.imageProcessing.sortCorner2(this.points1, 40);
//        return true;
//    }

//    public void getTrueAnswer(int widthCal, int heightCal) {
//        this.myCount = 0;
//        this.draw = new int[this.numberArea][][];
//        this.widthRect = new float[this.numberArea];
//        this.heightRect = new float[this.numberArea];
//        this.startPoint = new PointF[this.numberArea];
//        int j = 0;
//        while (j < this.numberArea) {
//            int idx;
//            Mat mat;
//            Point point;
//            double[] myRs = this.imageProcessing.whResult(this.points1, this.points2, j, 40);
//            this.widthResult = myRs[0];
//            this.heightResult = myRs[1];
//            Log.d("vinhtuanleWH", this.widthResult + " " + this.heightResult);
//            Log.d("vinhtuanleWH1", (this.widthResult / 11.0d) + " " + (this.heightResult / 6.0d));
//            Rect[][] myAnswer = (Rect[][]) Array.newInstance(Rect.class, new int[]{6, 10});
//            double[][] myPixel = (double[][]) Array.newInstance(Double.TYPE, new int[]{6, 10});
//            this.widthRect[j] = (float) (this.widthResult / 11.0d);
//            this.heightRect[j] = (float) (this.heightResult / 6.0d);
//            this.startPoint[j] = this.imageProcessing.getStartPoint(j, this.points1, this.points2, 40);
//            Log.d("vinhtuanlestartPoint", this.startPoint[j].x + " " + this.startPoint[j].y + " " + this.widthResult + " " + this.heightResult);
//            long timePx = System.currentTimeMillis();
//            for (idx = 0; idx < 6; idx++) {
//                int idy;
//                for (idy = 0; idy < 10; idy++) {
//                    int startX = (int) (((double) this.startPoint[j].x) + ((((double) idy) + 0.5d) * ((double) this.widthRect[j])));
//                    int startY = (int) (((double) this.startPoint[j].y) + ((((double) idx) + 0.5d) * ((double) this.heightRect[j])));
//                    myAnswer[idx][idy] = new Rect(startX, startY, (int) this.widthRect[j], (int) this.heightRect[j]);
//                    Mat matResult = this.mBinary.submat(myAnswer[idx][idy]);
//                    PointF center = new PointF((float) (matResult.width() / 2), (float) (matResult.height() / 2));
//                    int radius = (int) ((this.heightRect[j] * 9.0f) / 20.0f);
//                    if (this.imageProcessing.checkAreaDraw(j, 40, idx) == 1) {
//                        mat = this.clone;
//                        point = new Point((double) startX, (double) startY);
//                        point = new Point((double) (((int) this.widthRect[j]) + startX), (double) (((float) startY) + this.heightRect[j]));
//                        Imgproc.rectangle(mat, point, point, new Scalar(255.0d, 255.0d, 255.0d, 1.0d), 2);
//                    }
//                    Log.d("vltRect", "heightRect " + this.heightRect[j] + "radius" + radius + " thichness" + ((int) (this.heightRect[j] / 7.0f)));
//                    mat = this.mBinary1;
//                    point = new Point((double) (((int) center.x) + startX), (double) (((int) center.y) + startY));
//                    Imgproc.circle(mat, point, radius, new Scalar(255.0d, 0.0d, 0.0d, 1.0d), (int) (this.heightRect[j] / 7.0f));
//                    mat = this.mBinary1;
//                    point = new Point((double) (((int) center.x) + startX), (double) (((int) center.y) + startY));
//                    Imgproc.circle(mat, point, (((int) (this.heightRect[j] / 7.0f)) + radius) + 1, new Scalar(255.0d, 0.0d, 0.0d, 1.0d), 2);
//                    double r = this.imageProcessing.getRatePixel(this.mBinary1.submat(myAnswer[idx][idy]));
//                    myPixel[idx][idy] = r;
//                    Log.d("vinhtuanlePixel", r + " ");
//                }
//            }
//            Log.d("vinhtuanleTimePixcel", (System.currentTimeMillis() - timePx) + " ");
//            Log.d("vinhtuanleCount", this.myCount + " ");
//            long timeAnswer = System.currentTimeMillis();
//            this.draw[j] = this.imageProcessing.findAnswer(j, myPixel, MyScore.findAnswer, 40);
//            Log.d("vinhtuanleTimeFind", (System.currentTimeMillis() - timeAnswer) + " ");
//            long timeDraw = System.currentTimeMillis();
//            if (j == this.imageProcessing.getAreaMade(40) || j == this.imageProcessing.getAreaSBD(40)) {
//                int radius;//add
//                if (j == this.imageProcessing.getAreaMade(40)) {
//                    MyScore.made = this.imageProcessing.getMade(myPixel);
//                    Log.d("vinhtuanleMade", MyScore.made + " ");
//                    int[] md = this.imageProcessing.convertStringToArray(MyScore.made);
//                    for (idx = 2; idx < 5; idx++) {
//                        if (md[4 - idx] > -1) {
//                            startX = (int) (((double) this.startPoint[j].x) + ((((double) md[4 - idx]) + 0.5d) * ((double) this.widthRect[j])));
//                            startY = (int) (((double) this.startPoint[j].y) + ((((double) idx) + 0.5d) * ((double) this.heightRect[j])));
//                            radius = (int) ((this.heightRect[j] * 9.0f) / 20.0f);
//                            mat = this.clone;
//                            point = new Point((double) (((float) startX) + (this.widthRect[j] / BaseField.BORDER_WIDTH_MEDIUM)), (double) (((float) startY) + (this.widthRect[j] / BaseField.BORDER_WIDTH_MEDIUM)));
//                            Imgproc.circle(mat, point, radius, new Scalar(0.0d, 255.0d, 0.0d, 1.0d), (int) (this.heightRect[j] / 7.0f));
//                        }
//                    }
//                } else if (j == this.imageProcessing.getAreaSBD(40)) {
//                    MyScore.sobaodanh = this.imageProcessing.getSoBaoDanh(myPixel);
//                    int[] sbd = this.imageProcessing.convertStringToArray(MyScore.sobaodanh);
//                    for (idx = 0; idx < 6; idx++) {
//                        if (sbd[5 - idx] > -1) {
//                            startX = (int) (((double) this.startPoint[j].x) + ((((double) sbd[5 - idx]) + 0.5d) * ((double) this.widthRect[j])));
//                            startY = (int) (((double) this.startPoint[j].y) + ((((double) idx) + 0.5d) * ((double) this.heightRect[j])));
//                            radius = (int) ((this.heightRect[j] * 9.0f) / 20.0f);
//                            mat = this.clone;
//                            point = new Point((double) (((float) startX) + (this.widthRect[j] / BaseField.BORDER_WIDTH_MEDIUM)), (double) (((float) startY) + (this.widthRect[j] / BaseField.BORDER_WIDTH_MEDIUM)));
//                            Imgproc.circle(mat, point, radius, new Scalar(0.0d, 255.0d, 0.0d, 1.0d), (int) (this.heightRect[j] / 7.0f));
//                        }
//                    }
//                }
//            } else if (this.mode == 0) {
//                int idy;//add
//                int radius;//add
//                for (idx = 0; idx < 5; idx++) {
//                    for (idy = 0; idy < 10; idy++) {
//                        startX = (int) (((double) this.startPoint[j].x) + ((((double) idy) + 0.5d) * ((double) this.widthRect[j])));
//                        startY = (int) (((double) this.startPoint[j].y) + ((((double) idx) + 0.5d) * ((double) this.heightRect[j])));
//                        if (this.draw[j][idx][idy] == 1) {
//                            Log.d("vinhtuanleAbc11", j + " " + idx + " " + idy);
//                            radius = (int) ((this.heightRect[j] * 9.0f) / 20.0f);
//                            mat = this.clone;
//                            point = new Point((double) (((float) startX) + (this.widthRect[j] / BaseField.BORDER_WIDTH_MEDIUM)), (double) (((float) startY) + (this.widthRect[j] / BaseField.BORDER_WIDTH_MEDIUM)));
//                            Imgproc.circle(mat, point, radius, new Scalar(0.0d, 255.0d, 0.0d, 1.0d), (int) (this.heightRect[j] / 7.0f));
//                        }
//                    }
//                }
//            }
//            Log.d("vinhtuanleTimeDraw", (System.currentTimeMillis() - timeDraw) + " ");
//            j++;
//        }
//        bitmap1 = Bitmap.createBitmap(widthCal, this.myHeight - heightCal, Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(this.mBinary1, bitmap1);
//        saveImage(bitmap1, "answer/binary", "binaryImage1", 0);
//    }

    int minH = 255, maxH = 0, minS = 255, maxS = 0, minV = 255, maxV = 0;
    boolean touch;

    //độ sáng tại điểm đen (tối đa 173)
    //độ sáng tại điểm đen (tối thiểu 173)
    //test dark
    //1 min: x,12,145
    //1 max: x,35,168
    //2 min: x,4,75
    //2 max: x,41,173
    //tối đa 41, 173

    //test light
    //1 min: x,48,253
    //1 max: x,52,255
    //2 min: x,2,181
    //2 max: x,58,255
    //tối thiểu: 2,181

//    int SIZE = 200;
//    public Mat recognize2(Mat inFrame) {
//        Mat frame = inFrame;
////        Imgproc.adaptiveThreshold(inFrame, frame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 3);
////        Imgproc.threshold(inFrame, frame, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
////        Imgproc.adaptiveThreshold(inFrame, frame, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
//
////        Point[] p = new Point[]{
////                new Point(200, 0),
////                new Point(1730, 0),
////                new Point(200, frame.rows() - 50),
////                new Point(1730, frame.rows() - 50)
////        };
//
//        Point[] p = new Point[]{
//                new Point(100, 0),
//                new Point(1530, 0),
//                new Point(100, frame.rows() - SIZE),
//                new Point(1530, frame.rows() - SIZE)
//        };
//
//        Square[] squares = new Square[p.length];
//        for (int i = 0; i < squares.length; i++) {
//            squares[i] = new Square(p[i], SIZE);
//            squares[i].drawTo(frame);
//        }
//
//        if (touch) {
//            touch = false;
//            boolean accept = true;
//            double[] fcolor = getColor(frame);
//            for (int i = 0; i < squares.length; i++) {
//                double[] colors = getColor(squares[i].getMat(frame));
//                if (colors[0] > fcolor[0] - 5) {
//                    accept = false;
//                    break;
//                }
//            }
//
//            final boolean finalAccept = accept;
//            Log.e(TAG, "recognize2: " + (finalAccept ? "OK" : "Failed"));
//
//            if (accept) {
//            }
//
//        }
//        return frame;
//    }

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
