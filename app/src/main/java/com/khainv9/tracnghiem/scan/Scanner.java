package com.khainv9.tracnghiem.scan;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.core.CvType.CV_8UC3;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;
import com.khainv9.tracnghiem.models.DeThi;
import com.khainv9.tracnghiem.models.DiemThi;
import com.khainv9.tracnghiem.models.HocSinh;

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

public class Scanner {
    Mat[] corners;
    Mat[] corners1;

    int myHeight;
    int myWidth;

    //hình chữ nhật
    Rect[] rects;

    //kích thước thử nghiệm (giấy thi, pixel)
    //kích thước cắt bởi 4 góc dấu chấm đen
    Template template;

    Mat src, bgrFrame, bilateralImage, denoisedImage, hsvImage, frameThreshed, imgray;
    Mat dst;

    List<Line> vLines = new ArrayList<>();
    List<Line> hLines = new ArrayList<>();
    ExamPaper examPaper = new ExamPaper();

    boolean touch = false;

    BaiThi baiThi;
    Scanner(BaiThi baiThi){
        this.baiThi = baiThi;
    }

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

    void init(int width, int height){

        this.myWidth = width;
        this.myHeight = (this.myWidth * 9) / 16;
        int heightCal = this.myHeight / 4;
        int widthCal = (this.myHeight * 9) / 8;
        this.corners = new Mat[4];
        this.corners1 = new Mat[4];
        this.rects = new Rect[4];
        this.rects[0] = new Rect(0, 0, heightCal, heightCal);
        this.rects[1] = new Rect(widthCal, 0, heightCal, heightCal);
        this.rects[2] = new Rect(0, this.myHeight - heightCal, heightCal, heightCal);
        this.rects[3] = new Rect(widthCal, this.myHeight - heightCal, heightCal, heightCal);


        template = Template.createDefaultTemplate();


        src = new Mat(height, width, CV_8UC3);

        bgrFrame = new Mat();
        bilateralImage = new Mat();
        denoisedImage = new Mat();
        hsvImage = new Mat();
        frameThreshed = new Mat();
        imgray = new Mat();
        dst = new Mat();
    }

    private static List<Point> verifyTopPoint(List<Point> points, int limit, int step, boolean isTop){
        int s = points.size();
        while (s > limit) s -= step;
        s += step;

        // Sắp xếp danh sách theo thứ tự giá trị y tăng dần
        if (isTop){
            Collections.sort(points, (o1, o2) -> Double.compare(o1.y, o2.y));
        } else {
            Collections.sort(points, (o1, o2) -> Double.compare(o2.y, o1.y));
        }


        for (int i = s; i <= points.size(); i+= step){
            // Lấy danh sách s điểm trong danh sách
            List<Point> subPoints = new ArrayList<>();
            for (int j = 0; j < i; j++){
                subPoints.add(points.get(j));
            }
            while (subPoints.size() > limit){
                subPoints.remove(findOutlier(subPoints));
            }

            double[] lineParams = findSlopeAndIntercept(subPoints);
            double avrDistance = 0;
            for (Point point : subPoints) {
                double distance = Math.abs(point.y - (lineParams[0] * point.x + lineParams[1]));
                avrDistance += distance;
            }
            avrDistance /= subPoints.size();

            Log.d("MyLog", "verify top with " + i + " points, avrDistance = " + avrDistance);

            if (avrDistance < step){
                return subPoints;
            }
        }


        List<Point> output = new ArrayList<>();
        return output;
    }


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

    private static void removeTooClose(List<Point> points, double minDistance, int numCloseAccept, int limit){
        if (points.size() == limit)
            return;

        for (int i = points.size() - 1; i >= 0; i--){
            int countTooClose = 0;
            for (int j = i - 1; j >= 0; j--){
                if (distance(points.get(i), points.get(j)) < minDistance){
                    countTooClose++;
                    points.remove(i);
                    break;
                }
            }
            if (countTooClose >= numCloseAccept){
                points.remove(i);
                if (points.size() == limit)
                    return;
            }
        }

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

    void putBottomInfo(Mat inputFrame, Mat whiteSquare, Point cornerBR){
        Core.rotate(whiteSquare, whiteSquare, Core.ROTATE_90_COUNTERCLOCKWISE);
        int ww = myWidth - (int) cornerBR.x - 40;
        Mat r1 = new Mat(whiteSquare, new Rect(0, 0, ww, myHeight));
        Mat inputRoi = new Mat(inputFrame, new Rect((int) cornerBR.x + 40, 0, ww, myHeight));
        r1.copyTo(inputRoi);
    }

    static class ProcessResult {
        Mat resultMat;
        String info = "";
    }

    public ProcessResult processFrame(Mat inputFrame) {
        examPaper = new ExamPaper();
//
//        if (touch){
//            Bitmap bmp = matToBitmap(inputFrame);
//            Utils.saveImage("123", bmp);
//            Log.d("MyLog", "Saved image");
//            touch = false;
//        }

        ProcessResult result = new ProcessResult();
        result.resultMat = inputFrame;

        Mat whiteSquare = new Mat(myHeight, myHeight, inputFrame.type(), new Scalar(255, 255, 255));

        // Create timer to measure processing time
        long startTime = System.currentTimeMillis();

        // Draw text center frame
        String textDisplay = "";
        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(inputFrame, grayImage, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
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
        Imgproc.line(inputFrame, new Point(cornerTL.x, rects[0].br().y), new Point(rects[1].tl().x, rects[0].br().y), new Scalar(0, 0, 255), 4);
        Imgproc.line(inputFrame, new Point(rects[1].tl().x, rects[0].br().y), new Point(rects[1].tl().x, cornerBR.y), new Scalar(0, 0, 255), 4);

        Imgproc.line(inputFrame, new Point(cornerTL.x, rects[2].tl().y), new Point(rects[2].br().x, rects[2].tl().y), new Scalar(0, 0, 255), 4);
        Imgproc.line(inputFrame, new Point(rects[2].br().x, cornerBR.y), new Point(rects[2].br().x, rects[2].tl().y), new Scalar(0, 0, 255), 4);
        Rect paperCorner = new Rect(cornerTL, cornerBR);

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
            textDisplay += "Chua tim duoc diem tham chieu";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
            return result;
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
        rightPointsClone = verifyTopPoint(rightPointsClone, template.fixedRight, 5, false);
//        while (rightPointsClone.size() > template.fixedRight){
//            rightPointsClone.remove(findOutlier(rightPointsClone));
//        }
        rightPoints.clear();
        for (Point point : rightPointsClone){
            rightPoints.add(new Point(point.y, point.x));
        }

        topPoints = verifyTopPoint(topPoints, template.fixedTop, 10, true);
        if (topPoints.size() == 0 || rightPoints.size() == 0){
            textDisplay += "Chua tim duoc diem tham chieu";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);

            return result;
        }

        // Draw all top points
        for (Point point : topPoints){
            Imgproc.circle(inputFrame, point, 10, new Scalar(0, 255, 255), 2);
        }

        // Draw all right points
        for (Point point : rightPoints){
            Imgproc.circle(inputFrame, point, 10, new Scalar(255, 255, 0), 2);
        }

        // Loại bỏ điểm ngoại lai của bottom left points, chỉ giữ lại 1 điểm
        Point tl = topPoints.get(0);
        Point tr = topPoints.get(topPoints.size() - 1);
        Point br = rightPoints.get(rightPoints.size() - 1);

        final int DeltaAngle = 7;
        double angleTR = Math.atan2(tr.y - br.y, tr.x - br.x) - Math.atan2(tr.y - tl.y, tr.x - tl.x);
        double angleTRDegree = Math.abs(Math.toDegrees(angleTR));
        if (Math.abs(angleTRDegree - 90) > DeltaAngle){
            textDisplay = "Goc chua dung, vui long can chinh lai camera (" + angleTRDegree + ")";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
            return result;
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
            textDisplay = "Goc chua dung, vui long can chinh lai camera";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "angleBL = " + String.format("%.2f", angleBLDegree) + String.format("%.2f", angleTLDegree) + String.format("%.2f", angleBRDegree) , new Point(40, 120), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
            return result;
        }

        Point centerTop = topPoints.get(topPoints.size() / 2);
        Point centerRight = rightPoints.get(rightPoints.size() / 2);

        double ratioCenterTop = (centerTop.x - tl.x) / (tr.x - tl.x);
        double ratioCenterRight = (centerRight.y - tr.y) / (br.y - tr.y);

//        if (ratioCenterTop < 0.48 || ratioCenterTop > 0.52 || ratioCenterRight < 0.53 || ratioCenterRight > 0.57){
//            textDisplay += "Cac diem tham chieu khong hop le (ratio not valid)";
//            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
//            putBottomInfo(inputFrame, whiteSquare, cornerBR);
//            return result;
//        }

        Point t1 = topPoints.get(topPoints.size() - 1);
        Point t2 = rightPoints.get(0);
        boolean isValidMatch = t1.x == t2.x && t1.y == t2.y;
        if (!isValidMatch){
            textDisplay += "Cac diem tham chieu khong hop le";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "t1 = " + t1.x + ", " + t1.y, new Point(40, 120), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "t2 = " + t2.x + ", " + t2.y, new Point(40, 180), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
//            while(true);
            return result;
        }

        Imgproc.circle(inputFrame, tl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, tr, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, bl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, br, 12, new Scalar(255, 0, 0), 2);

        calculateVHLines(tl, tr, br, bl);

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
                    Imgproc.adaptiveThreshold(roi, roi, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);
//                    Imgproc.threshold(roi, roi, 150, 255, Imgproc.THRESH_BINARY);
                    Scalar mean = Core.mean(roi);
                    double meanValue = mean.val[0];
                    if (meanValue < 240){
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
////
//        if (true){
//            result.resultMat = grayImage;
//            return result;
//        }

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
                    examPaper.chapter1Answer += ("DCBA".charAt(section.values.get(j)));
                } else {
                    examPaper.chapter1Answer += ('_');
                }
            }
        }
        for (int i = 6; i < 14; i++){
            Section section = examPaper.sections.get(i);
            for (int j = 0; j < 4; j++){
                if (section.values.get(j) >= 0 && section.values.get(j) < 2){
                    examPaper.chapter2Answer += ("SĐ".charAt(section.values.get(j)));
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

        // Tìm kiếm học sinh có sbd bằng studentId
        HocSinh hsFound = null;
        String hsName = "";
        for (HocSinh hocSinh: Utils.dsHocSinh){
            if (hocSinh.sbd.equals(studentId)){
                hsFound = hocSinh;
                hsName = " [ " + hocSinh.name + " ]";
                break;
            }
        }

        // Tìm kiếm bài thi có mã đề bằng examCode
        DeThi deThiFound = null;
        String txtDeThi = "";
        if (baiThi != null){
            for (DeThi dethi: baiThi.dsDeThi){
                if (dethi.maDeThi.equals(examCode)){
                    deThiFound = dethi;
                    break;
                }
            }
        }
        if (deThiFound != null){
            txtDeThi = examCode + " OK!";
        } else {
            txtDeThi = examCode + " Not found!";
        }

        Imgproc.putText(whiteSquare, "Ma de: " + txtDeThi, new Point(40, 60), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
        Imgproc.putText(whiteSquare, "SBD: " + studentId + hsName, new Point(40, 120), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
        putBottomInfo(inputFrame, whiteSquare, cornerBR);


//
//        if (touch){
//            touch = false;
//            DiemThi diemThi = new DiemThi(examPaper.studentId,
//                    baiThi.maBaiThi,
//                    examPaper.examCode,
//                    matToBitmap(inputFrame),
//                    new String[] { examPaper.chapter1Answer, examPaper.chapter2Answer, examPaper.chapter3Answer });
//            Utils.update(diemThi);
//
//            result.info = "Đã lưu kết quả chấm";
//
//            Log.d("MyLog", "Diem thi: " + diemThi.toString());
//        }

        return result;
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

    public static Mat bitmapToMat(Bitmap bmp) {
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        org.opencv.android.Utils.bitmapToMat(bmp32, mat);
        return mat;
    }
}
