package com.khainv9.tracnghiem.scan.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class GeometryUtil {
    // Tìm kiếm limit điểm gần phía phải nhất sao cho các điểm đều nằm trên cùng 1 đường thẳng
    public static List<Point> verifyTopPoint(List<Point> points, int limit, int step, boolean isTop){
        int s = points.size();
        while (s > limit) s -= step;
        s += step;

        // Sắp xếp danh sách theo thứ tự giá trị y tăng dần
        if (isTop){
            points.sort(Comparator.comparingDouble(o -> o.y));
        } else {
            points.sort((o1, o2) -> Double.compare(o2.y, o1.y));
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

            if (avrDistance < step){
                return subPoints;
            }
        }
        return new ArrayList<>();
    }



    public static Point calculateIntersection(Point line1Start, Point line1End, Point line2Start, Point line2End) {
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

    public static final double MIN_RECTANGLE_AREA = 500; // Adjust this threshold as needed


    public static double[] findSlopeAndIntercept(List<Point> points){
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

    public static int findOutlier(List<Point> points) {
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

    public static double distance(Point p1, Point p2){
        return sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public static double lengthSquare(Point p1, Point p2) {
        double xDiff = p1.x- p2.x;
        double yDiff = p1.y- p2.y;
        return xDiff*xDiff + yDiff*yDiff;
    }
    public static double calculateAngle(Point A, Point B, Point C) {
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

}
