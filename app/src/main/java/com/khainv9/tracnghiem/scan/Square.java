package com.khainv9.tracnghiem.scan;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Square {
    Point point;//tọa độ vẽ
    int size;//kích thước (pixel)

    public Square(Point point, int size) {
        this.point = point;
        this.size = size;
    }

    //Vẽ khung hình vuông xanh
    public void drawTo(Mat frame) {
        Imgproc.rectangle(frame, getPoint1(), getPoint4(), new Scalar(128, 255, 128, 255), 1);
    }

    //Vẽ khung hình vuông đỏ
    public void drawCheckTo(Mat frame) {
        Imgproc.rectangle(frame, getPoint1(), getPoint4(), new Scalar(255, 128, 128, 255), 2);
    }

    public Mat getMat(Mat frame) {
//            Log.e(TAG, "getMat: " + point.x + ":" + point.y + " - " + point.x + size + ":" + point.y + size);
//            return frame.submat((int) point.x, (int) point.x + size, (int) point.y, (int) point.y + size);
        return frame.submat(new Rect((int) point.x, (int) point.y, 50, 50));
    }

    public Mat getCMat(Mat frame) {
        Point center = getPointCenter();
        return frame.submat(new Rect((int) center.x, (int) center.y, 1, 1));
    }

    public int getSize() {
        return size;
    }

    public Point getPoint1() {
        return point;
    }

    public Point getPoint2() {
        return new Point(point.x + size, point.y);
    }

    public Point getPoint3() {
        return new Point(point.x, point.y + size);
    }

    public Point getPoint4() {
        return new Point(point.x + size, point.y + size);
    }

    public Point getPointCenter() {
        return new Point(point.x + (size / 2), point.y + (size / 2));
    }

    public double[] getColor(Mat frame) {
        return getCMat(frame).get(0, 0);
    }
}
