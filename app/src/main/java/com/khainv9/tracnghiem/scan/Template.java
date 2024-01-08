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
        String pointMarks = "0.0809224318658281\t0.0487225193107546\t0.290566037735849\t0.110516934046346\t10\t3\t0\n" +
                "0.0809224318658281\t0.146761734997029\t0.290566037735849\t0.270350564468211\t10\t6\t1\n" +
                "0.353878406708595\t0.775401069518717\t0.533333333333333\t0.947712418300653\t10\t4\t2\n" +
                "0.353878406708595\t0.536541889483066\t0.533333333333333\t0.707070707070707\t10\t4\t2\n" +
                "0.353878406708595\t0.29708853238265\t0.533333333333333\t0.467023172905526\t10\t4\t2\n" +
                "0.353878406708595\t0.0582293523469994\t0.533333333333333\t0.22875816993464\t10\t4\t2\n" +
                "0.60251572327044\t0.866310160427807\t0.677148846960168\t0.944741532976827\t4\t2\t3\n" +
                "0.60251572327044\t0.777183600713012\t0.677148846960168\t0.86215092097445\t4\t2\t3\n" +
                "0.60251572327044\t0.629233511586453\t0.677148846960168\t0.707664884135472\t4\t2\t3\n" +
                "0.60251572327044\t0.540701128936423\t0.677148846960168\t0.625074272133096\t4\t2\t3\n" +
                "0.60251572327044\t0.389780154486037\t0.677148846960168\t0.469399881164587\t4\t2\t3\n" +
                "0.60251572327044\t0.301841948900772\t0.677148846960168\t0.386215092097445\t4\t2\t3\n" +
                "0.60251572327044\t0.151515151515152\t0.677148846960168\t0.229352346999406\t4\t2\t3\n" +
                "0.60251572327044\t0.0665478312537136\t0.677148846960168\t0.146167557932264\t4\t2\t3\n" +
                "0.758071278825996\t0.841354723707665\t0.974004192872117\t0.943553178847296\t12\t4\t4\n" +
                "0.758071278825996\t0.689245395127748\t0.974004192872117\t0.79144385026738\t12\t4\t4\n" +
                "0.758071278825996\t0.535947712418301\t0.974004192872117\t0.638146167557932\t12\t4\t4\n" +
                "0.758071278825996\t0.383838383838384\t0.974004192872117\t0.486036838978015\t12\t4\t4\n" +
                "0.758071278825996\t0.230540701128936\t0.974004192872117\t0.333927510398099\t12\t4\t4\n" +
                "0.758071278825996\t0.0790255496137849\t0.974004192872117\t0.180629827688651\t12\t4\t4\n";

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

