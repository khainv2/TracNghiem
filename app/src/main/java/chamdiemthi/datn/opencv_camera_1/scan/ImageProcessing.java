package chamdiemthi.datn.opencv_camera_1.scan;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {

    //    public static Bitmap saveImage(Bitmap finalBitmap, String folder, String fname) {
//        if (finalBitmap != null) {
//            File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/.saved_images/contest" + HomeActivity.contestID + "/" + folder);
//            myDir.mkdirs();
//            File file = new File(myDir, fname + ".jpg");
//            if (file.exists()) {
//                file.delete();
//            }
//            try {
//                FileOutputStream out = new FileOutputStream(file);
//                finalBitmap.compress(CompressFormat.JPEG, 90, out);
//                out.flush();
//                out.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return finalBitmap;
//    }

    public static final float BORDER_WIDTH_THIN = 1.0f;

    public ArrayList<Point> sortCorner(ArrayList<Point> points, int myWidth, int myHeight, int halfRect) {
        ArrayList<Point> rs = new ArrayList();
        int i = 0;
        while (i < points.size()) {
            Point point;
            if (((Point) points.get(i)).x < ((double) (myWidth / 2)) && ((Point) points.get(i)).y < ((double) (myHeight / 2))) {
                point = (Point) points.get(i);
                point.x += (double) halfRect;
                point = (Point) points.get(i);
                point.y += (double) halfRect;
                rs.add(0, points.get(i));
            } else if (((Point) points.get(i)).x > ((double) (myWidth / 2)) && ((Point) points.get(i)).y < ((double) (myHeight / 2))) {
                point = (Point) points.get(i);
                point.x -= (double) halfRect;
                point = (Point) points.get(i);
                point.y += (double) halfRect;
                rs.add(1, points.get(i));
            } else if (((Point) points.get(i)).x < ((double) (myWidth / 2)) && ((Point) points.get(i)).y > ((double) (myHeight / 2))) {
                point = (Point) points.get(i);
                point.x += (double) halfRect;
                point = (Point) points.get(i);
                point.y -= (double) halfRect;
                rs.add(2, points.get(i));
            } else if (((Point) points.get(i)).x > ((double) (myWidth / 2)) && ((Point) points.get(i)).y > ((double) (myHeight / 2))) {
                point = (Point) points.get(i);
                point.x -= (double) halfRect;
                point = (Point) points.get(i);
                point.y -= (double) halfRect;
                rs.add(3, points.get(i));
            }
            i++;
        }
        return rs;
    }

    public int getFormFromSentence(int sentence) {
        if (sentence <= 20) {
            return 20;
        }
        if (sentence > 20 && sentence <= 40) {
            return 40;
        }
        if (sentence > 40 && sentence <= 60) {
            return 60;
        }
        if (sentence <= 60 || sentence > 100) {
            return 0;
        }
        return 100;
    }

    public int getNumberMid(int form) {
        switch (form) {
            case 20:
                return 5;
            case 40:
                return 6;
            case 100:
                return 12;
            default:
                return 9;
        }
    }

    public int getNumberSort2(int form) {
        switch (form) {
            case 20:
                return 9;
            case 100:
                return 8;
            default:
                return 6;
        }
    }

    public int getNumberArea(int form) {
        switch (form) {
            case 20:
                return 4;
            case 40:
                return 6;
            case 100:
                return 12;
            default:
                return 8;
        }
    }

    public double[] getWH(int form, double width, double height) {
        double[] wh = new double[2];
        switch (form) {
            case 20:
                wh[0] = (1.8d * width) / 5.0d;
                wh[1] = (1.8d * height) / 7.0d;
                break;
            case 40:
                wh[0] = (1.8d * width) / 5.0d;
                wh[1] = (1.8d * height) / 7.0d;
                break;
            case 60:
                wh[0] = (1.6d * width) / 5.0d;
                wh[1] = (1.6d * height) / 7.0d;
                break;
            case 100:
                wh[0] = (1.4d * width) / 5.0d;
                wh[1] = (1.4d * height) / 7.0d;
                break;
        }
        return wh;
    }

    public double[][] getMidPoints(ArrayList<Point> points, int form) {
        double width = ((Point) points.get(1)).x - ((Point) points.get(0)).x;
        double height = ((Point) points.get(3)).y - ((Point) points.get(0)).y;
        double middleX = width / 2.0d;
        double middleY = height / 2.0d;
        int number = getNumberMid(form);
        double[][] a = (double[][]) Array.newInstance(Double.TYPE, new int[]{number, 2});
        double[] wh = getWH(form, width, height);
        double w = wh[0];
        double h = wh[1];
        int i = 0;
        while (i < number) {
            if (form == 20) {
                if (i == 0) {
                    a[i][1] = middleY - h;
                } else if (i > 0 && i < 4) {
                    a[i][1] = middleY;
                } else if (i == 4) {
                    a[i][1] = middleY + h;
                }
                if (i == 1) {
                    a[i][0] = middleX - (w - (w / 3.0d));
                } else if (i % 2 == 0) {
                    a[i][0] = (w / 3.0d) + middleX;
                } else if (i == 3) {
                    a[i][0] = ((w / 3.0d) + w) + middleX;
                }
            } else if (form == 40) {
                if (i < 3) {
                    a[i][1] = middleY - (h / 2.0d);
                } else {
                    a[i][1] = (h / 2.0d) + middleY;
                }
                if (i % 3 == 0) {
                    a[i][0] = middleX - (w - (w / 3.0d));
                } else if (i % 3 == 1) {
                    a[i][0] = (w / 3.0d) + middleX;
                } else if (i % 3 == 2) {
                    a[i][0] = ((w / 3.0d) + w) + middleX;
                }
            } else if (form == 60) {
                if (i / 3 == 0) {
                    a[i][1] = middleY - h;
                } else if (i / 3 == 1) {
                    a[i][1] = middleY;
                } else if (i / 3 == 2) {
                    a[i][1] = middleY + h;
                }
                if (i % 3 == 0) {
                    a[i][0] = middleX - (w - (w / 2.0d));
                } else if (i % 3 == 1) {
                    a[i][0] = (w / 2.0d) + middleX;
                } else if (i % 3 == 2) {
                    a[i][0] = ((w / 2.0d) + w) + middleX;
                }
            } else if (form == 100) {
                if (i / 4 == 0) {
                    a[i][1] = middleY - h;
                } else if (i / 4 == 1) {
                    a[i][1] = middleY;
                } else if (i / 4 == 2) {
                    a[i][1] = middleY + h;
                }
                if (i % 4 == 0) {
                    a[i][0] = ((w / 5.0d) + middleX) - ((3.0d * w) / 2.0d);
                } else if (i % 4 == 1) {
                    a[i][0] = ((w / 5.0d) - (w / 2.0d)) + middleX;
                } else if (i % 4 == 2) {
                    a[i][0] = ((w / 5.0d) + (w / 2.0d)) + middleX;
                } else if (i % 4 == 3) {
                    a[i][0] = ((w / 5.0d) + ((3.0d * w) / 2.0d)) + middleX;
                }
            }
            i++;
        }
        return a;
    }

    public double[][] sortCorner2(double[][] a, int form) {
        double heightResult;
        int numberSort2 = getNumberSort2(form);
        double[][] b = (double[][]) Array.newInstance(Double.TYPE, new int[]{numberSort2, 2});
        if (form == 60) {
            heightResult = a[3][1] - a[0][1];
        } else {
            heightResult = a[4][1] - a[0][1];
        }
        for (int i = 0; i < numberSort2; i++) {
            if (form == 20) {
                if (i % 3 == 0) {
                    b[i][0] = a[1][0];
                } else if (i % 3 == 2) {
                    b[i][0] = a[3][0];
                } else {
                    b[i][0] = a[2][0];
                }
                if (i < 3) {
                    b[i][1] = a[0][1];
                } else if (i < 6) {
                    b[i][1] = a[i - 2][1];
                } else {
                    b[i][1] = a[4][1];
                }
            } else if (form == 40) {
                b[i][0] = a[i][0];
                if (i < 3) {
                    b[i][1] = a[i][1] - heightResult;
                } else {
                    b[i][1] = a[i][1] + heightResult;
                }
            } else if (form == 60) {
                b[i][0] = a[i][0];
                if (i < 3) {
                    b[i][1] = a[i][1] - heightResult;
                } else {
                    b[i][1] = a[i + 3][1] + heightResult;
                }
            } else if (form == 100) {
                b[i][0] = a[i][0];
                if (i < 4) {
                    b[i][1] = a[i][1] - heightResult;
                } else {
                    b[i][1] = a[i + 4][1] + heightResult;
                }
            }
        }
        return b;
    }

    public double[] whResult(double[][] a, double[][] b, int index, int form) {
        double[] rs = new double[2];
        int numberArea = getNumberArea(form);
        if (form == 20) {
            if (index < 2) {
                rs[0] = Math.abs(b[index + 1][0] - b[index][0]);
                rs[1] = Math.abs(b[index + 3][1] - b[index][1]);
            } else {
                rs[0] = Math.abs(b[index + 2][0] - b[index + 1][0]);
                rs[1] = Math.abs(b[index + 4][1] - b[index + 1][1]);
            }
        } else if (form == 60 || form == 40) {
            if (index < 2) {
                rs[0] = Math.abs(b[index + 1][0] - b[index][0]);
                rs[1] = Math.abs(a[index][1] - b[index][1]);
            } else if (index >= numberArea - 2) {
                if (form == 40) {
                    rs[0] = Math.abs(a[index][0] - a[index - 1][0]);
                    rs[1] = Math.abs(b[index - 1][1] - a[index - 1][1]);
                } else {
                    rs[0] = Math.abs(a[index + 1][0] - a[index][0]);
                    rs[1] = Math.abs(b[index - 3][1] - a[index][1]);
                }
            } else if (index < 4) {
                rs[0] = Math.abs(a[index - 1][0] - a[index - 2][0]);
                rs[1] = Math.abs(a[index + 1][1] - a[index - 2][1]);
            } else {
                rs[0] = Math.abs(a[index][0] - a[index - 1][0]);
                rs[1] = Math.abs(a[index + 2][1] - a[index - 1][1]);
            }
        } else if (form == 100) {
            if (index < 3) {
                rs[0] = Math.abs(b[index + 1][0] - b[index][0]);
                rs[1] = Math.abs(a[index][1] - b[index][1]);
            } else if (index >= numberArea - 3) {
                rs[0] = Math.abs(a[index][0] - a[index - 1][0]);
                rs[1] = Math.abs(b[index - 5][1] - a[index - 1][1]);
            } else if (index < 6) {
                rs[0] = Math.abs(a[index - 2][0] - a[index - 3][0]);
                rs[1] = Math.abs(a[index + 1][1] - a[index - 3][1]);
            } else {
                rs[0] = Math.abs(a[index - 1][0] - a[index - 2][0]);
                rs[1] = Math.abs(a[index + 2][1] - a[index - 2][1]);
            }
        }
        return rs;
    }

    public Point getPoint(Rect rect) {
        Point point = new Point();
        point.x = (double) (rect.x + (rect.width / 2));
        point.y = (double) (rect.y + (rect.height / 2));
        return point;
    }

    public PointF getStartPoint(int i, double[][] a, double[][] b, int form) {
        PointF pointF = new PointF();
        if (form == 20) {
            if (i < 2) {
                pointF.x = (float) b[i][0];
                pointF.y = (float) b[i][1];
            } else {
                pointF.x = (float) b[i + 1][0];
                pointF.y = (float) b[i + 1][1];
            }
        } else if (form == 40) {
            if (i < 2) {
                pointF.x = (float) b[i][0];
                pointF.y = (float) b[i][1];
            } else if (i < 4) {
                pointF.x = (float) a[i - 2][0];
                pointF.y = (float) a[i - 2][1];
            } else {
                pointF.x = (float) a[i - 1][0];
                pointF.y = (float) a[i - 1][1];
            }
        } else if (form == 60) {
            if (i < 2) {
                pointF.x = (float) b[i][0];
                pointF.y = (float) b[i][1];
            } else if (i < 4) {
                pointF.x = (float) a[i - 2][0];
                pointF.y = (float) a[i - 2][1];
            } else if (i < 6) {
                pointF.x = (float) a[i - 1][0];
                pointF.y = (float) a[i - 1][1];
            } else {
                pointF.x = (float) a[i][0];
                pointF.y = (float) a[i][1];
            }
        } else if (form == 100) {
            if (i < 3) {
                pointF.x = (float) b[i][0];
                pointF.y = (float) b[i][1];
            } else if (i < 6) {
                pointF.x = (float) a[i - 3][0];
                pointF.y = (float) a[i - 3][1];
            } else if (i < 9) {
                pointF.x = (float) a[i - 2][0];
                pointF.y = (float) a[i - 2][1];
            } else {
                pointF.x = (float) a[i - 1][0];
                pointF.y = (float) a[i - 1][1];
            }
        }
        return pointF;
    }

    public Point getStartTruePoint(int i, double[][] a, double[][] b, int form) {
        Point pointF = new Point();
        if (form == 20) {
            if (i < 2) {
                pointF.x = (double) ((float) b[i + 3][0]);
                pointF.y = (double) ((float) b[i + 3][1]);
            } else {
                pointF.x = (double) ((float) b[i + 4][0]);
                pointF.y = (double) ((float) b[i + 4][1]);
            }
        } else if (form == 40) {
            if (i < 2) {
                pointF.x = (double) ((float) a[i][0]);
                pointF.y = (double) ((float) a[i][1]);
            } else if (i < 4) {
                pointF.x = (double) ((float) a[i + 1][0]);
                pointF.y = (double) ((float) a[i + 1][1]);
            } else {
                pointF.x = (double) ((float) b[i - 1][0]);
                pointF.y = (double) ((float) b[i - 1][1]);
            }
        } else if (form == 60) {
            if (i < 2) {
                pointF.x = (double) ((float) a[i][0]);
                pointF.y = (double) ((float) a[i][1]);
            } else if (i < 4) {
                pointF.x = (double) ((float) a[i + 1][0]);
                pointF.y = (double) ((float) a[i + 1][1]);
            } else if (i < 6) {
                pointF.x = (double) ((float) a[i + 2][0]);
                pointF.y = (double) ((float) a[i + 2][1]);
            } else {
                pointF.x = (double) ((float) b[i - 3][0]);
                pointF.y = (double) ((float) b[i - 3][1]);
            }
        } else if (form == 100) {
            if (i < 3) {
                pointF.x = (double) ((float) a[i][0]);
                pointF.y = (double) ((float) a[i][1]);
            } else if (i < 6) {
                pointF.x = (double) ((float) a[i + 1][0]);
                pointF.y = (double) ((float) a[i + 1][1]);
            } else if (i < 9) {
                pointF.x = (double) ((float) a[i + 2][0]);
                pointF.y = (double) ((float) a[i + 2][1]);
            } else {
                pointF.x = (double) ((float) b[i - 5][0]);
                pointF.y = (double) ((float) b[i - 5][1]);
            }
        }
        return pointF;
    }

    public int[][] findAnswer(int area, double[][] rs, int[] findAnswer, int form) {
        int[][] draw = (int[][]) Array.newInstance(Integer.TYPE, new int[]{5, 10});
        int start = start(area, form);
        Log.d("vinhtuanleStart", start + " ");
        for (int i = 0; i < 10; i++) {
            int j;
            double[] temp = new double[5];
            int[] index = new int[5];
            for (j = 0; j < 5; j++) {
                temp[j] = rs[j][i];
                index[j] = j;
            }
            for (int m = 0; m < 5; m++) {
                for (int n = 0; n < 5; n++) {
                    if (temp[n] < temp[m]) {
                        double t = temp[n];
                        temp[n] = temp[m];
                        temp[m] = t;
                        int t1 = index[n];
                        index[n] = index[m];
                        index[m] = t1;
                    }
                }
            }
            for (j = 0; j < 5; j++) {
                Log.d("vinhtuanlesort", area + " " + temp[j]);
            }
            if (check(temp)) {
                if (findAnswer[start + i] == -1) {
                    findAnswer[start + i] = 4 - index[0];
                } else {
                    findAnswer[start + i] = -1;
                }
                draw[index[0]][i] = 1;
            } else {
                draw[index[0]][i] = 0;
            }
        }
        return draw;
    }

    public int findTrueAnswer(int area, int idy, int form) {
        return start(area, form) + idy;
    }

    public boolean check(double[] a) {
        if (Math.abs(a[0] - a[1]) <= 0.1d || Math.abs(a[1] - a[2]) > 0.2d) {
            return false;
        }
        return true;
    }

    int getAreaMade(int form) {
        switch (form) {
            case 20:
                return 0;
            case 40:
                return 2;
            case 60:
                return 4;
            case 100:
                return 6;
            default:
                return 0;
        }
    }

    int getAreaSBD(int form) {
        switch (form) {
            case 20:
                return 2;
            case 40:
                return 4;
            case 60:
                return 6;
            case 100:
                return 9;
            default:
                return 0;
        }
    }

    int checkAreaDraw(int area, int form, int j) {
        int areaMade = getAreaMade(form);
        int areaSBD = getAreaSBD(form);
        if (area == areaMade) {
            if (j > 1 && j < 5) {
                return 1;
            }
        } else if (area == areaSBD) {
            return 1;
        } else {
            if (j >= 0 && j < 5) {
                return 1;
            }
        }
        return 0;
    }

    public void printAnswer(int mode, int[] a) {
        for (int i = 0; i < a.length; i++) {
            if (mode == 0) {
                Log.d("vinhtuanleKey", "Cau" + ((i + 1) - 10) + ": " + a[i] + " ");
            } else {
                Log.d("vinhtuanleAnswer", "Cau" + ((i + 1) - 10) + ": " + a[i] + " ");
            }
        }
    }

    public int findScore(int[] myAnswer, int[] answer, int sentence) {
        int score = 0;
        int number = sentence + 20;
        Log.d("vinhtuanleNumber", number + " ");
        int i = 20;
        while (i < number) {
            if (myAnswer[i] != -1 && myAnswer[i] == answer[i]) {
                score++;
            }
            i++;
        }
        return score;
    }

    public float getValueSentence(int sentence, int maxScore) {
        return ((float) maxScore) / ((float) sentence);
    }

    public int[] getTrueAnswer(int[] myAnswer, int[] answer, int form) {
        int number = form + 20;
        int[] a = new int[number];
        int i = 20;
        while (i < number) {
            if (myAnswer[i] != -1 && myAnswer[i] == answer[i]) {
                a[i] = 1;
            }
            i++;
        }
        return a;
    }

    public int start(int area, int form) {
        return mapStart(form)[area];
    }

    public int[] mapStart(int form) {
        if (form == 20) {
            return new int[]{10, 30, 0, 20};
        }
        if (form == 40) {
            return new int[]{20, 50, 10, 40, 0, 30};
        }
        if (form == 60) {
            return new int[]{30, 70, 20, 60, 10, 50, 0, 40};
        }
        if (form == 100) {
            return new int[]{30, 70, 110, 20, 60, 100, 10, 50, 90, 0, 40, 80};
        }
        return null;
    }

    public Point[] getRectTrue(double[][] a, double[][] b, double widthRect, int form) {
        int i;
        int number = getNumberArea(form) * 10;
        Point[] pointTrue = new Point[number];
        for (i = 0; i < number; i++) {
            pointTrue[i] = new Point();
        }
        for (i = 0; i < getNumberArea(form); i++) {
            int start = start(i, form);
            Point startTruePoint = getStartTruePoint(i, a, b, form);
            for (int j = 0; j < 10; j++) {
                pointTrue[start + j].x = (double) ((int) (startTruePoint.x + (((double) (j + 1)) * (widthRect / 11.0d))));
                pointTrue[start + j].y = (double) ((int) startTruePoint.y);
            }
        }
        for (i = 0; i < number; i++) {
            Log.d("rectTrue", pointTrue[i].x + " " + pointTrue[i].y);
        }
        return pointTrue;
    }

    public Bitmap cropInfo(Point[] points, Bitmap bitmap) {
        Point p1 = points[2];
        int height = (int) p1.y;
        Log.d("length", ((int) p1.x) + " " + height);
        Matrix matrix = new Matrix();
        matrix.postRotate(270.0f);
        return Bitmap.createBitmap(bitmap, height / 5, 0, height / 2, height / 10, matrix, true);
    }

    public String getTextScore(int sentence, String text, float diem) {
        String textScore = "";
        return text + " / " + sentence + " = " + new DecimalFormat("0.00").format((double) diem);
    }

    public double getTH(float rate) {
        if (rate > BORDER_WIDTH_THIN) {
            return 0.7d;
        }
        if (rate < BORDER_WIDTH_THIN && ((double) rate) > 0.7d) {
            return 0.6d;
        }
        if (((double) rate) < 0.7d) {
            return 0.55d;
        }
        return 0.65d;
    }

    //
    public int getThreadhold(float rate) {
        if (rate < BORDER_WIDTH_THIN && ((double) rate) > 0.7d) {
            return 41;
        }
        if (((double) rate) < 0.7d) {
            return 21;
        }
        if (rate > BORDER_WIDTH_THIN) {
            return 71;
        }
        return 41;
    }

    public double getSubtract(float rate) {
        if (rate < BORDER_WIDTH_THIN && ((double) rate) > 0.7d) {
            return 10.0d;
        }
        if (((double) rate) < 0.7d) {
            return 10.0d;
        }
        if (rate > BORDER_WIDTH_THIN) {
            return 15.0d;
        }
        return 15.0d;
    }

    public Mat getMat(Mat src, Point point, int w, int h) {
        Rect rect = new Rect((int) (point.x - ((double) w)), (int) (point.y - ((double) h)), w * 2, h * 2);
        Log.e("historygetMat", rect.x + " " + rect.y + " " + rect.width + " " + w);
        return src.submat(rect);
    }

    public int getHalfRect(int form, float halfRect) {
        float rate = BORDER_WIDTH_THIN;
        switch (form) {
            case 20:
                rate = 1.1f;
                break;
            case 40:
                rate = 1.1f;
                break;
            case 60:
                rate = 1.05f;
                break;
            case 100:
                rate = 0.94f;
                break;
        }
        return (int) (halfRect * rate);
    }

    public double getRatePixel(Mat mat) {
        int myArea = mat.width() * mat.height();
        int black = myArea - Core.countNonZero(mat);
        Log.d("vinhtuanleBlack", black + " " + myArea);
        return ((double) black) / ((double) myArea);
    }

    public Point getHistogram(Mat mat, Point startPoint) {
        int j;
        Point point = new Point();
        int w = mat.width();
        int h = mat.height();
        Log.e("historyWH", w + " " + h + " " + startPoint.x + " " + startPoint.y);
        int startX = 0;
        int startY = 0;
        int finishX = 0;
        int finishY = 0;
        int[] arrX = new int[w];
        int[] arrY = new int[h];
        int i = 0;
        while (i < w) {
            for (j = 0; j < h; j++) {
                int value = (int) mat.get(j, i)[0];
                Log.e("value", value + " ");
                if (value == 0) {
                    arrX[i] = arrX[i] + 1;
                }
                if (arrX[i] > h / 2) {
                    startX = i;
                    Log.e("historyGramstartX", i + " ");
                    j = h;
                    i = w;
                    break;
                }
            }
            i++;
        }
        i = w - 1;
        while (i >= 0) {
            for (j = h - 1; j >= 0; j--) {
                if (((int) mat.get(j, i)[0]) == 0) {
                    arrX[i] = arrX[i] + 1;
                }
                if (arrX[i] > h / 2) {
                    finishX = i;
                    Log.e("historyGramfinishX", i + " ");
                    i = -1;
                    break;
                }
            }
            i--;
        }
        j = 0;
        while (j < h) {
            for (i = 0; i < w; i++) {
                int value = (int) mat.get(j, i)[0];
                Log.e("value", value + " ");
                if (value == 0) {
                    arrY[j] = arrY[j] + 1;
                }
                if (arrY[j] > w / 2) {
                    startY = j;
                    Log.e("historyGramstartY", j + " ");
                    j = h;
                    i = w;
                    break;
                }
            }
            j++;
        }
        j = h - 1;
        while (j >= 0) {
            for (i = w - 1; i >= 0; i--) {
                if (((int) mat.get(j, i)[0]) == 0) {
                    arrY[j] = arrY[j] + 1;
                }
                if (arrY[j] > w / 2) {
                    finishY = j;
                    Log.e("historyGramfinishY", j + " ");
                    j = -1;
                    i = -1;
                    break;
                }
            }
            j--;
        }
        point.x = startPoint.x + ((double) ((finishX + startX) / 2));
        point.y = startPoint.y + ((double) ((finishY + startY) / 2));
        Log.d("historyPoint", ((finishX + startX) / 2) + " " + ((finishY + startY) / 2));
        return point;
    }

    public boolean checkForm(double[] form, double value) {
        for (double d : form) {
            if (d < value) {
                return false;
            }
        }
        return true;
    }

    public String convertMadeToText(String made) {
        int[] arr = convertStringToArray(made);
        String str = "";
        String strSeparator = "";
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > -1) {
                str = str + arr[i];
                if (i < arr.length - 1) {
                    str = str + strSeparator;
                }
            } else {
                str = str + "-";
            }
        }
        return str;
    }

    public String convertArrayToString(int[] array) {
        String str = "";
        String strSeparator = "_";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i];
            if (i < array.length - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    public String convertIntToArray(int a) {
        int i;
        int[] arr = new int[5];
        for (i = 0; i < 5; i++) {
            arr[i] = -1;
        }
        String temp = Integer.toString(a);
        int[] newGuess = new int[temp.length()];
        for (i = 0; i < temp.length(); i++) {
            newGuess[i] = temp.charAt(i) - 48;
        }
        for (i = 0; i < temp.length(); i++) {
            arr[(5 - temp.length()) + i] = newGuess[i];
        }
        return convertArrayToString(arr);
    }

    public int[] convertStringToArray(String str) {
        String[] arr = str.split("_");
        int[] rs = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            rs[i] = Integer.parseInt(arr[i]);
        }
        return rs;
    }

    public int[] convertKeyToArray(String str, int form, int sentence) {
        int i;
        String[] arr = str.split("_");
        int[] rs = new int[(form + 20)];
        for (i = 0; i < rs.length; i++) {
            rs[i] = -1;
        }
        for (i = 20; i < sentence + 20; i++) {
            rs[i] = Integer.parseInt(arr[i]);
        }
        return rs;
    }

    public String getSoBaoDanh(double[][] a) {
        int[] sbd = new int[6];
        for (int i = 0; i < 6; i++) {
            int j;
            double[] temp = new double[10];
            int[] index = new int[10];
            for (j = 0; j < 10; j++) {
                temp[j] = a[i][j];
                index[j] = j;
            }
            for (int m = 0; m < 10; m++) {
                for (int n = 0; n < 10; n++) {
                    if (temp[n] < temp[m]) {
                        double t = temp[n];
                        temp[n] = temp[m];
                        temp[m] = t;
                        int t1 = index[n];
                        index[n] = index[m];
                        index[m] = t1;
                    }
                }
            }
            for (j = 0; j < 10; j++) {
                Log.d("vinhtuanlesortMade", "5 " + temp[j]);
            }
            Log.d("vinhtuanlesortMade", check(temp) + " ");
            if (check(temp)) {
                sbd[5 - i] = index[0];
            } else {
                sbd[5 - i] = -1;
            }
        }
        String sobaodanh = convertArrayToString(sbd);
        Log.d("vinhtuanleMade", sobaodanh + " ");
        return sobaodanh;
    }

    public String getMade(double[][] a) {
        int[] md = new int[3];
        for (int i = 2; i < 5; i++) {
            int j;
            double[] temp = new double[10];
            int[] index = new int[10];
            for (j = 0; j < 10; j++) {
                temp[j] = a[i][j];
                index[j] = j;
            }
            for (int m = 0; m < 10; m++) {
                for (int n = 0; n < 10; n++) {
                    if (temp[n] < temp[m]) {
                        double t = temp[n];
                        temp[n] = temp[m];
                        temp[m] = t;
                        int t1 = index[n];
                        index[n] = index[m];
                        index[m] = t1;
                    }
                }
            }
            for (j = 0; j < 10; j++) {
                Log.d("vinhtuanlesortMade", "4 " + temp[j]);
            }
            Log.d("vinhtuanlesortMade", check(temp) + " ");
            if (check(temp)) {
                md[4 - i] = index[0];
            } else {
                md[4 - i] = -1;
            }
        }
        String made = convertArrayToString(md);
        Log.d("vinhtuanleS", made + " ");
        return made;
    }

    public int[] convertNumberToArray(String made) {
        int[] rs = new int[3];
        char[] rs1 = made.toCharArray();
        int k = 0;
        while (k < 3) {
            if (rs1[k] < '0' || rs1[k] > '9') {
                rs[k] = -1;
            } else {
                rs[k] = rs1[k] - 48;
            }
            k++;
        }
        return rs;
    }

    public int convertDapanToInt(String s) {
        return s.charAt(0) - 65;
    }

    public String convertIntToDapan(int i) {
        if (i == -1) {
            return "null";
        }
        return "" + ((char) (i + 65));
    }

    public double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return ((dx1 * dx2) + (dy1 * dy2)) / Math.sqrt((((dx1 * dx1) + (dy1 * dy1)) * ((dx2 * dx2) + (dy2 * dy2))) + 1.0E-10d);
    }

    void extractChannel(Mat source, Mat out, int channelNum) {
        List<Mat> sourceChannels = new ArrayList();
        List<Mat> outChannel = new ArrayList();
        Core.split(source, sourceChannels);
        outChannel.add(new Mat(((Mat) sourceChannels.get(0)).size(), ((Mat) sourceChannels.get(0)).type()));
        Core.mixChannels(sourceChannels, outChannel, new MatOfInt(new int[]{channelNum, 0}));
        Core.merge(outChannel, out);
    }

    MatOfPoint approxPolyDP(MatOfPoint curve, double epsilon, boolean closed) {
        MatOfPoint2f tempMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(curve.toArray()), tempMat, epsilon, closed);
        return new MatOfPoint(tempMat.toArray());
    }
}
