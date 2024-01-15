package com.khainv9.tracnghiem.scan;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

import android.graphics.Bitmap;
import android.util.Log;

import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.QuestionPaper;
import com.khainv9.tracnghiem.models.ExamResult;
import com.khainv9.tracnghiem.models.Student;

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
import java.util.List;

public class Scanner {
    Mat[] corners;
    Mat[] corners1;

    Mat testMat;
    Mat testMat2;

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

    Examination examination;
    Scanner(Examination examination){
        this.examination = examination;
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


        src = new Mat(height, width, CvType.CV_8UC3);

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

//        Log.d("MyLog", "verify top with " + s + " points");
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

//            Log.d("MyLog", "verify top with " + i + " points, avrDistance = " + avrDistance + ", is top " + isTop);

            if (avrDistance < step){
                return subPoints;
            }
        }

//        Log.d("MyLog", "verify top failed");
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
        return sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    static double lengthSquare(Point p1, Point p2) {
        double xDiff = p1.x- p2.x;
        double yDiff = p1.y- p2.y;
        return xDiff*xDiff + yDiff*yDiff;
    }
    static double calculateAngle(Point A, Point B, Point C) {
        // Square of lengths be a2, b2, c2
        double a2 = lengthSquare(B, C);
        double b2 = lengthSquare(A, C);
        double c2 = lengthSquare(A, B);
        // length of sides be a, b, c
        double a = sqrt(a2);
//        double b = sqrt(b2);
        double c = sqrt(b2);
        // From Cosine law
        double gamma = acos((a2 + c2 - b2)/(2*a*c));
        // Converting to degree
        return gamma * 180 / PI;
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

        Imgproc.line(inputFrame, new Point(cornerTL.x, rects[0].br().y), new Point(rects[1].tl().x, rects[0].br().y), new Scalar(150, 150, 255), 1);
        Imgproc.line(inputFrame, new Point(rects[1].tl().x, rects[0].br().y), new Point(rects[1].tl().x, cornerBR.y), new Scalar(150, 150, 255), 1);
        Imgproc.line(inputFrame, new Point(cornerTL.x, rects[2].tl().y), new Point(rects[2].br().x, rects[2].tl().y), new Scalar(150, 150, 255), 1);
        Imgproc.line(inputFrame, new Point(rects[2].br().x, cornerBR.y), new Point(rects[2].br().x, rects[2].tl().y), new Scalar(150, 150, 255), 1);

        Rect paperCorner = new Rect(cornerTL, cornerBR);

        // Use Canny edge detector to find edges in the image
        Mat edges = new Mat();
        Imgproc.Canny(binaryImage, edges, 50, 200);

        // Dilate the edges to close gaps in contours
        Imgproc.dilate(edges, edges, Mat.ones(3, 3, CvType.CV_8UC1));

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
                Imgproc.circle(inputFrame, boundingRect.tl(), 2, new Scalar(255, 0, 0), 2);
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
            double threshold = 170; // Adjust this threshold as needed
            boolean isBlack = meanIntensity.val[0] < threshold;

            Rect aboveMarker = new Rect(marker.x - marker.width, marker.y - marker.height,
                    marker.width * 3, marker.height);
            Rect belowMarker = new Rect(marker.x - marker.width, marker.y + marker.height,
                    marker.width * 3, marker.height);
            Mat aMat = new Mat(binaryImage, aboveMarker);
            Mat bMat = new Mat(binaryImage, belowMarker);
            Scalar aIntensity = Core.mean(aMat);
            Scalar bIntensity = Core.mean(bMat);
            boolean isAboveBlack = aIntensity.val[0] < threshold;
            boolean isBelowBlack = bIntensity.val[0] < threshold;

            if (marker.width < marker.height && isBlack && !isAboveBlack && !isBelowBlack) {
                blackMarkers.add(marker);            // Vẽ giá trị màu trung bình lên marker
            }
        }

        for (Rect rect: blackMarkers) {
            Point tl = rect.tl();
            Point br = rect.br();
            Imgproc.rectangle(inputFrame, tl, br, new Scalar(255, 0, 0), 1);
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
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
            return result;
        }

        // Loại bỏ các điểm ngoại lai do tính chất của các điểm top & right: các điểm đều nằm trên
        // 1 đường thẳng
        List<Point> rightPointsClone = new ArrayList<>();
        for (Point point : rightPoints){
            rightPointsClone.add(new Point(point.y, point.x));
        }
        rightPointsClone = verifyTopPoint(rightPointsClone, template.fixedRight, 5, false);

        rightPoints.clear();
        for (Point point : rightPointsClone){
            rightPoints.add(new Point(point.y, point.x));
        }
        topPoints = verifyTopPoint(topPoints, template.fixedTop, 10, true);

        if (topPoints.size() < template.fixedTop || rightPoints.size() < template.fixedRight){
            textDisplay += "Chua tim duoc diem tham chieu";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "Top points size " + topPoints.size() + ", right points size " + rightPoints.size() + ", bottom left points size " + bottomLeftPoints.size(), new Point(40, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "Top points size " + topPoints.size() + ", right points size " + rightPoints.size() + ", bottom left points size " + bottomLeftPoints.size(), new Point(40, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
            return result;
        }

        // Sắp xếp right points & top points theo thứ tự x & y tăng dần
        Collections.sort(rightPoints, (o1, o2) -> Double.compare(o1.y, o2.y));
        Collections.sort(topPoints, (o1, o2) -> Double.compare(o1.x, o2.x));

        // Draw all top points
        for (Point point : topPoints){
            Imgproc.circle(inputFrame, point, 10, new Scalar(0, 255, 255), 2);
        }

        // Draw all right points
        for (Point point : rightPoints){
            Imgproc.circle(inputFrame, point, 10, new Scalar(255, 255, 0), 2);
        }
        // Draw all bottom left points
        for (Point point : bottomLeftPoints){
            Imgproc.circle(inputFrame, point, 10, new Scalar(255, 0, 255), 2);
        }

        // Loại bỏ điểm ngoại lai của bottom left points, chỉ giữ lại 1 điểm
        Point tl = topPoints.get(0);
        Point tr = topPoints.get(topPoints.size() - 1);
        Point br = rightPoints.get(rightPoints.size() - 1);

        Imgproc.circle(inputFrame, tl, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, tr, 12, new Scalar(255, 0, 0), 2);
        Imgproc.circle(inputFrame, br, 12, new Scalar(255, 0, 0), 2);

        final int DeltaAngle = 7;
        double newAngleTR = calculateAngle(tl, tr, br);
        if (Math.abs(newAngleTR - 90) > DeltaAngle){
            textDisplay = "Goc chua dung, vui long can chinh lai camera";
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "angleTR = " + String.format("%.2f", newAngleTR), new Point(40, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);

            Imgproc.putText(whiteSquare, "TL " + tl.x + ", " + tl.y + ", TR " + tr.x + ", " + tr.y + ", BR " + br.x + ", " + br.y, new Point(40, 180), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "BR " + br.x + ", " + br.y, new Point(40, 240), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
            return result;
        }
        Point bl = bottomLeftPoints.get(0);
        double minDeltaAngle = 9999;
        for (int i = 0; i < bottomLeftPoints.size(); i++){
            Point point = bottomLeftPoints.get(i);
            double angle = calculateAngle(tl, point, br);
            double deltaAngle = Math.abs(angle - newAngleTR);
            if (deltaAngle < minDeltaAngle){
                minDeltaAngle = deltaAngle;
                bl = point;
            }
        }
        Imgproc.circle(inputFrame, bl, 12, new Scalar(255, 0, 0), 2);
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
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "angleBL = " + String.format("%.2f", angleBLDegree) + String.format("%.2f", angleTLDegree) + String.format("%.2f", angleBRDegree) , new Point(40, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
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
            Imgproc.putText(whiteSquare, textDisplay, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "t1 = " + t1.x + ", " + t1.y, new Point(40, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            Imgproc.putText(whiteSquare, "t2 = " + t2.x + ", " + t2.y, new Point(40, 180), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
            putBottomInfo(inputFrame, whiteSquare, cornerBR);
//            while(true);
            return result;
        }

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
            PointMark[][] marks = new PointMark[numRow][];

            for (int row = 0; row < numRow; row++) {
                marks[row] = new PointMark[numCol];
                for (int col = 0; col < numCol; col++) {
                    Point ref1 = getRefPoint(xMin + (xMax - xMin) * col / numCol, yMin + (yMax - yMin) * row / numRow);
                    Point ref3 = getRefPoint(xMin + (xMax - xMin) * (col + 1) / numCol, yMin + (yMax - yMin) * (row + 1) / numRow);

                    Rect interest = new Rect(ref1, ref3);
                    Mat roi = new Mat(grayImage, interest);
                    Imgproc.GaussianBlur(roi, roi, new Size(5, 5), 0);

                    Scalar mean = Core.mean(roi);
                    double meanValue = mean.val[0];

                    Mat thresholded = new Mat();
                    Core.compare(roi, new Scalar(meanValue), thresholded, Core.CMP_LT);
                    double mean2 = Core.mean(roi, thresholded).val[0];


//
//                    // Tính tổng của các pixel có giá trị nhỏ hơn meanValue
//                    int mean2 = 0;
//                    int sum2 = 0;
//                    for (int i = 0; i < roi.rows(); i++){
//                        for (int j = 0; j < roi.cols(); j++){
//                            double[] pixel = roi.get(i, j);
//                            if (pixel[0] < meanValue){
//                                mean2 += pixel[0];
//                                sum2 ++;
//                            }
//                        }
//                    }
//                    mean2 /= sum2;



                    Point center = new Point((ref1.x + ref3.x) / 2, (ref1.y + ref3.y) / 2);
                    PointMark mark = new PointMark();
                    mark.row = row;
                    mark.col = col;
                    mark.type = type;
                    mark.point = center;
                    mark.value = mean2;
                    marks[row][col] = mark;
                    Scalar squareArea = new Scalar(200, 200, 200);
                    Imgproc.rectangle(inputFrame, ref1, ref3, squareArea, 1);

                    if (sectionIndex == 2 && col == 3){
//                        Imgproc.rectangle(inputFrame, ref1, ref3, new Scalar(0, 0, 0), 3);
                        Log.d("MyLog", "Mean value at row " + row + ", col " + col + " = " + String.format("%.2f", meanValue) + ", mean2 = " + mean2);
//                        Imgproc.putText(inputFrame, , center, FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0), 1);
                    }

                }
            }

            List<Integer> values = new ArrayList<>();
            // Kiểm tra giá trị của các marker, warning những chỗ khoanh sai
            switch (type){
                case 0: case 1: case 4: {
                    // Lấy mark có giá trị nhỏ nhất trong hàng
                    for (int row = 0; row < numRow; row++) {
                        PointMark maxMark = null;
                        for (int col = 0; col < numCol; col++) {
                            PointMark mark = marks[row][col];
                            if (maxMark == null || mark.value < maxMark.value){
                                maxMark = mark;
                            }
                        }
                        if (maxMark != null){
                            values.add(maxMark.col);
                            if (type == 0 || type == 1) {
                                // Chỉ khoanh vùng trong trường hợp mã đề & SBD
                                Imgproc.circle(inputFrame, maxMark.point, 10, new Scalar(0, 0, 255), 2);
                            } else {
                                Imgproc.circle(inputFrame, maxMark.point, 10, new Scalar(0, 0, 255), 1);
                            }
                        } else {
                            values.add(-1);
                        }
                    }
                    break;
                }
                case 2: case 3: {
                    // Lấy mark có giá trị nhỏ nhất trong cột
                    for (int col = 0; col < numCol; col++) {
                        PointMark maxMark = null;
                        for (int row = 0; row < numRow; row++) {
                            PointMark mark = marks[row][col];
                            if (maxMark == null || mark.value < maxMark.value){
                                maxMark = mark;
                            }
                        }
                        if (maxMark != null){
                            values.add(maxMark.row);
                            Imgproc.circle(inputFrame, maxMark.point, 10, new Scalar(0, 0, 255), 1);
                        } else {
                            values.add(-1);
                        }
                    }
                    break;
                }
            }

            Section section = new Section();
            section.type = type;
            section.values = values;

            examPaper.sections.add(section);
        }
//        if (true){
//            result.resultMat = grayImage;
//            return result;
//        }

        // Lấy tham số student code
        Section studentCodeSection = examPaper.sections.get(1);
        String studentId = "";
        for (int i = 5; i >= 0; i--){
            if (i < studentCodeSection.values.size() && studentCodeSection.values.get(i) != -1){
                studentId += studentCodeSection.values.get(i);
            } else {
                studentId += "_";
            }
        }
        String hsName = "";
        for (Student student : Utils.dsStudent){
            if (student.id.equals(studentId)){
                hsName = " [ " + student.name + " ]";
                break;
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
            for (int j = 3; j >= 0; j--){
                if (section.values.get(j) >= 0 && section.values.get(j) < 12){
                    examPaper.chapter3Answer += ("-,0123456789".charAt(section.values.get(j)));
                } else {
                    examPaper.chapter3Answer += ('_');
                }
            }
        }
        // Lấy tham số exam code
        Section examCodeSection = examPaper.sections.get(0);
        String examCode = "";
        for (int i = 2; i >= 0; i--){
            if (i < examCodeSection.values.size() && examCodeSection.values.get(i) != -1){
                examCode += examCodeSection.values.get(i);
            } else {
                examCode += "_";
            }
        }
        // Tìm kiếm bài thi có mã đề bằng examCode
        QuestionPaper questionPaperFound = null;
        String txtDeThi = "";
        if (examination != null){
            for (QuestionPaper dethi: examination.questionPapers){
                if (dethi.maDeThi.equals(examCode)){
                    questionPaperFound = dethi;
                    break;
                }
            }
        }
        if (questionPaperFound != null){
            txtDeThi = examCode + " OK!";
            Examination bai = Utils.getBaiThi(examination.id);
            QuestionPaper de = Utils.getDethi(examination, examCode);
            if (bai != null && de != null){
                String[] dapAnP1 = de.getDapAnP1();
                String[] dapAnP2 = de.getDapAnP2();
                String[] dapAnP3 = de.getDapAnP3();
                String p1 = examPaper.chapter1Answer;
                String p2 = examPaper.chapter2Answer;
                String p3 = examPaper.chapter3Answer;
                for (int i = 0; i < dapAnP1.length && i < p1.length(); i++) {
                    String actual = p1.charAt(i) + "";
                    String expected = dapAnP1[i];

                    int sectionIndex = i / 10 + 2;
                    int col = i % 10;
                    int row = "DCBA".indexOf(p1.charAt(i));
                    SectionArea sectionArea = template.sectionAreas.get(sectionIndex);
                    double xMin = sectionArea.xMin;
                    double xMax = sectionArea.xMax;
                    double yMin = sectionArea.yMin;
                    double yMax = sectionArea.yMax;
                    int numRow = sectionArea.numRow;
                    int numCol = sectionArea.numCol;
                    Point ref1 = getRefPoint(xMin + (xMax - xMin) * col / numCol, yMin + (yMax - yMin) * row / numRow);
                    Point ref3 = getRefPoint(xMin + (xMax - xMin) * (col + 1) / numCol, yMin + (yMax - yMin) * (row + 1) / numRow);
                    Point center = new Point((ref1.x + ref3.x) / 2, (ref1.y + ref3.y) / 2);
                    if (expected.equals(actual)) {
                        Imgproc.circle(inputFrame, center, 10, new Scalar(0, 255, 0), 2);
                    } else {
                        Imgproc.circle(inputFrame, center, 10, new Scalar(255, 0, 0), 2);
                    }
                }
                for (int i = 0; i < dapAnP2.length && i < p2.length(); i++) {
                    String actual = p2.charAt(i) + "";
                    String expected = dapAnP2[i];
                    int sectionIndex = i / 4 + 6;
                    int col = i % 4;
                    int row = "SĐ".indexOf(p2.charAt(i));

                    SectionArea sectionArea = template.sectionAreas.get(sectionIndex);
                    double xMin = sectionArea.xMin;
                    double xMax = sectionArea.xMax;
                    double yMin = sectionArea.yMin;
                    double yMax = sectionArea.yMax;
                    int numRow = sectionArea.numRow;
                    int numCol = sectionArea.numCol;
                    Point ref1 = getRefPoint(xMin + (xMax - xMin) * col / numCol, yMin + (yMax - yMin) * row / numRow);
                    Point ref3 = getRefPoint(xMin + (xMax - xMin) * (col + 1) / numCol, yMin + (yMax - yMin) * (row + 1) / numRow);
                    Point center = new Point((ref1.x + ref3.x) / 2, (ref1.y + ref3.y) / 2);
                    if (expected.equals(actual)) {
                        Imgproc.circle(inputFrame, center, 10, new Scalar(0, 255, 0), 2);
                    } else {
                        Imgproc.circle(inputFrame, center, 10, new Scalar(255, 0, 0), 2);
                    }
                }
                for (int i = 0; i < dapAnP3.length && i < p3.length(); i++) {
                    String actual = p3.charAt(i) + "";
                    String expected = dapAnP3[i];
                    int sectionIndex = i / 4 + 14;
                    int row = 3 - (i % 4);
                    int col = "-,0123456789".indexOf(p3.charAt(i));
                    SectionArea sectionArea = template.sectionAreas.get(sectionIndex);
                    double xMin = sectionArea.xMin;
                    double xMax = sectionArea.xMax;
                    double yMin = sectionArea.yMin;
                    double yMax = sectionArea.yMax;
                    int numRow = sectionArea.numRow;
                    int numCol = sectionArea.numCol;
                    Point ref1 = getRefPoint(xMin + (xMax - xMin) * col / numCol, yMin + (yMax - yMin) * row / numRow);
                    Point ref3 = getRefPoint(xMin + (xMax - xMin) * (col + 1) / numCol, yMin + (yMax - yMin) * (row + 1) / numRow);
                    Point center = new Point((ref1.x + ref3.x) / 2, (ref1.y + ref3.y) / 2);
                    if (expected.equals(actual)) {
                        Imgproc.circle(inputFrame, center, 10, new Scalar(0, 255, 0), 2);
                    } else {
                        Imgproc.circle(inputFrame, center, 10, new Scalar(255, 0, 0), 2);
                    }
                }
                ExamResult examResult = new ExamResult(examPaper.studentId,
                        examination.id,
                        examPaper.examCode,
                        null,
                        new String[] { examPaper.chapter1Answer, examPaper.chapter2Answer, examPaper.chapter3Answer },
                        false
                );
                ExamResult.Score score = examResult.chamBai();
                Imgproc.putText(whiteSquare, "Diem thi P1: " + score.p1 + ", P2: " + score.p2 + ", P3: " + score.p3 + ", Tong " + score.total(), new Point(40, 180), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
                Imgproc.putText(whiteSquare, "Cham de luu ket qua", new Point(40, 240), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);

//                Imgproc.putText(whiteSquare, examPaper.chapter1Answer + " " + examPaper.chapter2Answer + " " + examPaper.chapter3Answer, new Point(40, 300), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
//                Imgproc.putText(whiteSquare, dapAnP1[0] + " " + dapAnP2[0] + " " + dapAnP3[0], new Point(40, 360), FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
//
            }
        } else {
            txtDeThi = examCode + " [Sai ma de thi]!";
        }
        examPaper.examCode = examCode;


        Imgproc.putText(whiteSquare, "Ma de: " + txtDeThi, new Point(40, 60), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
        Imgproc.putText(whiteSquare, "SBD: " + studentId, new Point(40, 120), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
        putBottomInfo(inputFrame, whiteSquare, cornerBR);



        if (touch){
            touch = false;
            ExamResult examResult = new ExamResult(examPaper.studentId,
                    examination.id,
                    examPaper.examCode,
                    matToBitmap(inputFrame),
                    new String[] { examPaper.chapter1Answer, examPaper.chapter2Answer, examPaper.chapter3Answer },
                    true
            );
            Utils.update(examResult);

            result.info = "Đã lưu kết quả chấm";

            Log.d("MyLog", "Diem thi: " + examResult.toString());
        }

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
