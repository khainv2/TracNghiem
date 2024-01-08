package com.khainv9.tracnghiem.scan;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

class Line {
    Point p1, p2;
}
public class GeometryUtils {


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

    private static final double MIN_RECTANGLE_AREA = 500; // Adjust this threshold as needed

    public static List<Line> findMidPoints(Point p1, Point p2, Point p3, Point p4, Point pCenter, Point pFar, int depth){
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

}
