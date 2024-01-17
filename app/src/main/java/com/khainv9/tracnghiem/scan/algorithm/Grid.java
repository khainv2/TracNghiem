package com.khainv9.tracnghiem.scan.algorithm;

import static com.khainv9.tracnghiem.scan.algorithm.GeometryUtil.calculateIntersection;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Grid {
    List<Line> vLines = new ArrayList<>();
    List<Line> hLines = new ArrayList<>();

    // P1, P2, P3 và P4 là 4 điểm của hình chữ nhật bị nghiêng đi do hướng của camera nhìn
    // Thuật toán tạo ra 2^depth đường thẳng ngang & dọc nằm bên trong 4 điểm p1, p2, p3, p4
    // Các đường thẳng này đôi một cắt nhau và tạo thành các hình chữ nhật nhỏ hơn (trong thực tế
    // là song song trên hình chữ nhật ban đầu)
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

    public void calculateVHLines(Point tl, Point tr, Point br, Point bl){
        Point pCenter = calculateIntersection(tl, br, tr, bl);
        Point pFar1 = calculateIntersection(tl, bl, tr, br);
        Point pFar2 = calculateIntersection(tl, tr, br, bl);
        vLines = findMidPoints(tl, tr, br, bl, pCenter, pFar1, 10);
        hLines = findMidPoints(tl, bl, br, tr, pCenter, pFar2, 10);
        vLines.sort(Comparator.comparingDouble(o -> o.p1.x));
        hLines.sort(Comparator.comparingDouble(o -> o.p1.y));
    }

    public Point getRefPoint(double x, double y){
        Line vLine1 = vLines.get((int) (x * vLines.size()));
        Line hLine1 = hLines.get((int) (y * hLines.size()));
        return calculateIntersection(vLine1.p1, vLine1.p2, hLine1.p1, hLine1.p2);
    }
}
