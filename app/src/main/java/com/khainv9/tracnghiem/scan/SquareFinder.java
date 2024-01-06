package com.khainv9.tracnghiem.scan;

import org.opencv.core.*;

public class SquareFinder {

    private double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }
//
//    public List<MatOfPoint> findSquaresInImage(Mat image) {
//        List<MatOfPoint> squares = new ArrayList<>();
//        Mat pyr = new Mat();
//        Mat timg = new Mat();
//        Mat gray0 = new Mat(image.size(), CvType.CV_8U);
//        Mat gray = new Mat();
//        int thresh = 50, N = 11;
//
//        Imgproc.pyrDown(image, pyr, new Size(image.cols() / 2, image.rows() / 2));
//        Imgproc.pyrUp(pyr, timg, image.size());
//
//        List<MatOfPoint> contours = new ArrayList<>();
//        for (int c = 0; c < 3; c++) {
//            int[] ch = {c, 0};
//            MatOfPoint2f approx = new MatOfPoint2f();
//            MatOfPoint2f contour2f = new MatOfPoint2f();
//            MatOfPoint approxMatOfPoint = new MatOfPoint();
//
//            Core.mixChannels(new ArrayList<>(List.of(timg)), new ArrayList<>(List.of(gray0)), new MatOfInt(ch));
////            Core.mix
//
//            for (int l = 0; l < N; l++) {
//                if (l == 0) {
//                    Imgproc.Canny(gray0, gray, 0, thresh, 5);
//                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1));
//                } else {
//                    gray = gray0;
//                }
//
//                Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//                for (MatOfPoint contour : contours) {
//                    contour.convertTo(contour2f, CvType.CV_32FC2);
//                    Imgproc.approxPolyDP(contour2f, approx, Imgproc.arcLength(contour2f, true) * 0.02, true);
//                    approx.convertTo(approxMatOfPoint, CvType.CV_32S);
//
//                    if (approxMatOfPoint.size().height == 4 && Math.abs(Imgproc.contourArea(approxMatOfPoint)) > 1000 &&
//                            Imgproc.isContourConvex(approxMatOfPoint)) {
//                        double maxCosine = 0;
//
//                        for (int j = 2; j < 5; j++) {
//                            double cosine = Math.abs(angle(approx.toArray()[j % 4], approx.toArray()[j - 2], approx.toArray()[j - 1]));
//                            maxCosine = Math.max(maxCosine, cosine);
//                        }
//
//                        if (maxCosine < 0.3) {
//                            squares.add(approxMatOfPoint);
//                        }
//                    }
//                }
//            }
//        }
//        return squares;
//    }
}