package com.khainv9.tracnghiem.scan;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SectionArea {
    double xMin, yMin, xMax, yMax;
    int numRow, numCol;
    int type = 0; // 0: horizontal, 1: vertical, 2: horizontal with exception
}

class PointMark {
    Point point;
    int col, row;
    int type;
    // Giá trị màu của điểm (càng lớn càng sáng)
    double value = 0;
}


class Section {
    List<PointMark> pointMarks = new ArrayList<>();
    int type;
    List<Integer> values = new ArrayList<>();
}
class ExamPaper {
    List<Section> sections = new ArrayList<>();
    String examCode = "";
    String studentId = "";
    String chapter1Answer = ""; // A B C D
    String chapter2Answer = ""; // T F
    String chapter3Answer = ""; // - , 0 1 2 3 4 5 6 7 8 9

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Exam code: " + examCode + "\n");
        sb.append("Student id: " + studentId + "\n");
        sb.append("Chapter 1: " + chapter1Answer + "\n");
        sb.append("Chapter 2: " + chapter2Answer + "\n");
        sb.append("Chapter 3: " + chapter3Answer + "\n");
        return sb.toString();
    }
}

public class Template {
    String TAG = getClass().getSimpleName();

    public List<SectionArea> sectionAreas = new ArrayList<>();
    public int fixedTop = 40;
    public int fixedRight = 18;
    public int fixedBottomLeft = 1;

    public static Template createDefaultTemplate(){
        Template template = new Template();
        String pointMarks = "0.080922432\t0.048722519\t0.290566038\t0.110516934\t10\t3\t0\n" +
                "0.080922432\t0.146761735\t0.290566038\t0.270350564\t10\t6\t1\n" +
                "0.353878407\t0.77540107\t0.533333333\t0.947712418\t10\t4\t2\n" +
                "0.353878407\t0.536541889\t0.533333333\t0.707070707\t10\t4\t2\n" +
                "0.353878407\t0.297088532\t0.533333333\t0.467023173\t10\t4\t2\n" +
                "0.353878407\t0.058229352\t0.533333333\t0.22875817\t10\t4\t2\n" +
                "0.602515723\t0.86631016\t0.677148847\t0.944741533\t4\t2\t3\n" +
                "0.602515723\t0.777183601\t0.677148847\t0.862150921\t4\t2\t3\n" +
                "0.602515723\t0.629233512\t0.677148847\t0.707664884\t4\t2\t3\n" +
                "0.602515723\t0.540701129\t0.677148847\t0.625074272\t4\t2\t3\n" +
                "0.602515723\t0.389780154\t0.677148847\t0.469399881\t4\t2\t3\n" +
                "0.602515723\t0.301841949\t0.677148847\t0.386215092\t4\t2\t3\n" +
                "0.602515723\t0.151515152\t0.677148847\t0.229352347\t4\t2\t3\n" +
                "0.602515723\t0.066547831\t0.677148847\t0.146167558\t4\t2\t3\n" +
                "0.758071279\t0.841354724\t0.974004193\t0.943553179\t12\t4\t4\n" +
                "0.758071279\t0.689245395\t0.974004193\t0.79144385\t12\t4\t4\n" +
                "0.758071279\t0.535947712\t0.974004193\t0.638146168\t12\t4\t4\n" +
                "0.758071279\t0.383838384\t0.974004193\t0.486036839\t12\t4\t4\n" +
                "0.758071279\t0.232323232\t0.974004193\t0.335710042\t12\t4\t4\n" +
                "0.758071279\t0.080213904\t0.974004193\t0.183600713\t12\t4\t4\n";


        String[] lines = pointMarks.split("\n");
        for (String line : lines) {
            String[] values = line.split("\t");
            double x = Double.parseDouble(values[0]);
            double y = Double.parseDouble(values[1]);
            double x1 = Double.parseDouble(values[2]);
            double y1 = Double.parseDouble(values[3]);
            int w = Integer.parseInt(values[4]);
            int h = Integer.parseInt(values[5]);
            int type = Integer.parseInt(values[6]);

            SectionArea sectionArea = new SectionArea();
            sectionArea.xMin = x;
            sectionArea.yMin = y;
            sectionArea.xMax = x1;
            sectionArea.yMax = y1;
            sectionArea.numCol = w;
            sectionArea.numRow = h;
            sectionArea.type = type;
            template.sectionAreas.add(sectionArea);
        }
        template.fixedBottomLeft = 1;
        template.fixedRight = 18;
        template.fixedTop = 40;
        return template;
    }
}
//
//        // Tính toán mức độ sáng  trung bình của vùng nằm trong 4 điểm góc
//        double meanIntensity = 120;
//        {
//            double xMin = Math.min(tl.x, bl.x);
//            double xMax = Math.max(tr.x, br.x);
//            double yMin = Math.min(tl.y, tr.y);
//            double yMax = Math.max(bl.y, br.y);
//            Rect interest = new Rect(new Point(xMin, yMin), new Point(xMax, yMax));
//            Mat roi = new Mat(grayImage, interest);
//            Scalar mean = Core.mean(roi);
//            meanIntensity = mean.val[0];
//            Log.d("MyLog", "Mean intensity: " + meanIntensity);
//
//        }

