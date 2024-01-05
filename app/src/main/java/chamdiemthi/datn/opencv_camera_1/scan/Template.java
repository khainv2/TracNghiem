package chamdiemthi.datn.opencv_camera_1.scan;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

import static chamdiemthi.datn.opencv_camera_1.models.DeThi.A;
import static chamdiemthi.datn.opencv_camera_1.models.DeThi.B;
import static chamdiemthi.datn.opencv_camera_1.models.DeThi.C;
import static chamdiemthi.datn.opencv_camera_1.models.DeThi.D;
import static chamdiemthi.datn.opencv_camera_1.models.DeThi.NOT;

public class Template {
    String TAG = getClass().getSimpleName();
    //Tọa độ trên camera (theo %)
    //điểm bắt đầu khu vực quét câu trả lời (điểm 1A, 11A, 21A, 31A,...)
    public double[] SAX, SAY;
    //khoảng cách câu
    public double kcX, kcY;
    //kích thước ô quét (màu xanh)
    public double cellSize;
    //tọa độ bắt đầu mã đề, số báo danh
    public double mdX, mdY, sbdX, sbdY;
    int DARK_COLOR = 110;

    public static Template createTemplate20() {
        double tw, th;
        //tọa độ thử nghiệm câu 1A
        double tspX, tspY;
        //tọa độ thử nghiệm câu 10A
        double tlpX, tlpY;
        //tạo template
        Template template = new Template();
        //khởi tạo các giá trị đã test trên thực tế
        tw = 2706.0; //chiều rộng thử nghiệm
        th = 1832.0; //chiều cao thử nghiệm

        //tọa độ khu vực quét test 1
        tspX = 1912.0;//câu 1A (x)
        tspY = 1296.0;//câu 1A 󰀀
        tlpX = 2687.0;//câu 10A (x)
        tlpY = 1074.0;//câu 10A 󰀀

        //danh sách các điểm bắt đầu của các vùng quét
        //đối với giấy 20 câu có 2 khu vực quét (khu vực câu 1-10 và khu vực câu 11-20)
        //=> khởi tạo 2 tọa độ bắt đầu của khu vực quét
        template.SAX = new double[2];
        template.SAY = new double[2];
        template.SAX[0] = 1699 / tw;        //kích thước theo % của chiều rộng thử nghiệm
        template.SAY[0] = 1231 / th;        //kích thước theo % của chiều cao thử nghiệm
        template.SAX[1] = 1699 / tw;        //kích thước theo % của chiều rộng thử nghiệm
        template.SAY[1] = 775 / th;        //kích thước theo % của chiều cao thử nghiệm

        //tọa độ bắt đầu quét mã đề
        template.mdX = 747.0 / tw;
        template.mdY = 628.0 / th;

        //tọa độ bắt đầu quét số báo danh
        template.sbdX = 747.0 / tw;
        template.sbdY = 937.0 / th;

        Log.e(template.TAG, "SPX,SPY: (" + template.SAX[0] + "x" + template.SAY[0] + ")");//log start point (%)
        //tính toán khoảng cách câu theo % kích thước (rộng, cao thử nghiệm)
        template.kcX = (Math.abs(tspX - tlpX) / 9.0) / tw;
        template.kcY = 74.0 / th;
        Log.e(template.TAG, "KCC,KCD: (" + template.kcX + "x" + template.kcY + ")");//log khoảng cách câu (%)
        template.cellSize = 50 / tw;
        Log.e(template.TAG, "Cellsize: (" + template.cellSize + ")");//log kích thước cell (%)
        return template;
    }

    public static Template createTemplate40() {
        double tw, th;
        //tọa độ câu 1A
        double tspX, tspY;
        //tọa độ câu 10A
        double tlpX, tlpY;
        Template template = new Template();
        //Tính toán tọa độ
        tw = 2706.0;
        th = 1832.0;
        Log.e(template.TAG, "TW x TH:" + tw + "x" + th);
        tspX = 1912.0;
        tspY = 1296.0;
        tlpX = 2687.0;
        tlpY = 1074.0;
        template.SAX = new double[2];
        template.SAY = new double[2];
        template.SAX[0] = 1699 / tw;
        template.SAY[0] = 1231 / th;
        template.SAX[1] = 1699 / tw;
        template.SAY[1] = 775 / th;

        template.mdX = 747.0 / tw;
        template.mdY = 628.0 / th;

        template.sbdX = 747.0 / tw;
        template.sbdY = 937.0 / th;

        Log.e(template.TAG, "SPX,SPY: (" + template.SAX[0] + "x" + template.SAY[0] + ")");
        template.kcX = (Math.abs(tspX - tlpX) / 9.0) / tw;
        template.kcY = 74.0 / th;
        Log.e(template.TAG, "KCC,KCD: (" + template.kcX + "x" + template.kcY + ")");
        template.cellSize = 50 / tw;
        Log.e(template.TAG, "Cellsize: (" + template.cellSize + ")");

        return template;
    }

    public static Template createTemplate60() {
        double tw, th;
        //tọa độ câu 1A
        double tspX, tspY;
        //tọa độ câu 10A
        double tlpX, tlpY;
        Template template = new Template();
        //Tính toán tọa độ
        tw = 2706.0;
        th = 1832.0;
        Log.e(template.TAG, "TW x TH:" + tw + "x" + th);
        tspX = 1912.0;
        tspY = 1296.0;
        tlpX = 2687.0;
        tlpY = 1074.0;
        template.SAX = new double[2];
        template.SAY = new double[2];
        template.SAX[0] = 1699 / tw;
        template.SAY[0] = 1231 / th;
        template.SAX[1] = 1699 / tw;
        template.SAY[1] = 775 / th;

        template.mdX = 747.0 / tw;
        template.mdY = 628.0 / th;

        template.sbdX = 747.0 / tw;
        template.sbdY = 937.0 / th;

        Log.e(template.TAG, "SPX,SPY: (" + template.SAX[0] + "x" + template.SAY[0] + ")");
        template.kcX = (Math.abs(tspX - tlpX) / 9.0) / tw;
        template.kcY = 74.0 / th;
        Log.e(template.TAG, "KCC,KCD: (" + template.kcX + "x" + template.kcY + ")");
        template.cellSize = 50 / tw;
        Log.e(template.TAG, "Cellsize: (" + template.cellSize + ")");

        return template;
    }

    public static Template createTemplate80() {
        double tw, th;
        //tọa độ câu 1A
        double tspX, tspY;
        //tọa độ câu 10A
        double tlpX, tlpY;
        Template template = new Template();
        //Tính toán tọa độ
        tw = 2706.0;
        th = 1832.0;
        Log.e(template.TAG, "TW x TH:" + tw + "x" + th);
        tspX = 1912.0;
        tspY = 1296.0;
        tlpX = 2687.0;
        tlpY = 1074.0;
        template.SAX = new double[2];
        template.SAY = new double[2];
        template.SAX[0] = 1699 / tw;
        template.SAY[0] = 1231 / th;
        template.SAX[1] = 1699 / tw;
        template.SAY[1] = 775 / th;

        template.mdX = 747.0 / tw;
        template.mdY = 628.0 / th;

        template.sbdX = 747.0 / tw;
        template.sbdY = 937.0 / th;

        Log.e(template.TAG, "SPX,SPY: (" + template.SAX[0] + "x" + template.SAY[0] + ")");
        template.kcX = (Math.abs(tspX - tlpX) / 9.0) / tw;
        template.kcY = 74.0 / th;
        Log.e(template.TAG, "KCC,KCD: (" + template.kcX + "x" + template.kcY + ")");
        template.cellSize = 50 / tw;
        Log.e(template.TAG, "Cellsize: (" + template.cellSize + ")");

        return template;
    }

    public static Template createTemplate100() {
        double tw, th;
        //tọa độ câu 1A
        double tspX, tspY;
        //tọa độ câu 10A
        double tlpX, tlpY;
        Template template = new Template();
        //Tính toán tọa độ
        tw = 2706.0;
        th = 1832.0;
        Log.e(template.TAG, "TW x TH:" + tw + "x" + th);
        tspX = 1912.0;
        tspY = 1296.0;
        tlpX = 2687.0;
        tlpY = 1074.0;
        template.SAX = new double[2];
        template.SAY = new double[2];
        template.SAX[0] = 1699 / tw;
        template.SAY[0] = 1231 / th;
        template.SAX[1] = 1699 / tw;
        template.SAY[1] = 775 / th;

        template.mdX = 747.0 / tw;
        template.mdY = 628.0 / th;

        template.sbdX = 747.0 / tw;
        template.sbdY = 937.0 / th;

        Log.e(template.TAG, "SPX,SPY: (" + template.SAX[0] + "x" + template.SAY[0] + ")");
        template.kcX = (Math.abs(tspX - tlpX) / 9.0) / tw;
        template.kcY = 74.0 / th;
        Log.e(template.TAG, "KCC,KCD: (" + template.kcX + "x" + template.kcY + ")");
        template.cellSize = 50 / tw;
        Log.e(template.TAG, "Cellsize: (" + template.cellSize + ")");

        return template;
    }

    private Template() {
    }

    public ArrayList<String> scanBaiLam(double w, double h, Point p0, Mat mRga1) {
        ArrayList<String> baiLam = new ArrayList<>();
        for (int i = 0; i < SAX.length; i++) {
            String[] ctl = getCauTraLoiArea(i, w, h, p0, mRga1);
            for (int j = 0; j < ctl.length; j++) {
                baiLam.add(ctl[j]);
            }
        }
        return baiLam;
    }

    //lấy ds câu trả lời theo khu vực (khu vực câu 1-10, 11-20)
    public String[] getCauTraLoiArea(int area, double w, double h, Point p0, Mat mRga1) {
        //tạo mảng 10 câu
        String[] cauTraLoi = new String[10];
        String[] da = {A, B, C, D};
        for (int j = 0; j < 10; j++) {
            boolean[] ans = new boolean[4];
            for (int l = 0; l < 4; l++) {
                //tạo khung hình vuông
                Square square = new Square(new Point(SAX[area] * w + j * (kcX * w) + p0.x, SAY[area] * h - l * (kcY * h) + p0.y), (int) (cellSize * w));
                square.getPointCenter();
                //lấy ra màu trung bình
                double[] color = square.getColor(mRga1);
                if (color[0] < DARK_COLOR) {//nếu ô quét màu tối
                    //check đáp án
                    ans[l] = true;
                    //vẽ khung đỏ
                    square.drawCheckTo(mRga1);
                } else square.drawTo(mRga1); //vẽ khung xanh
            }
            //
            cauTraLoi[j] = getDaTL(da, ans);
        }
        return cauTraLoi;
    }

    public String getDaTL(String[] da, boolean[] ans) {
        String cauTraLoi = NOT;
        int maxSelected = 0;
        for (int i = 0; i < ans.length; i++) {
            if (ans[i]) {
                cauTraLoi = da[i];
                maxSelected++;
            }
        }
        if (maxSelected == 1) return cauTraLoi;
        return NOT;
    }

    public String scanMaDe(double w, double h, Point p0, Mat mRga1) {
        String[] maDe = new String[3];
        for (int i = 0; i < maDe.length; i++) {
            for (int j = 0; j < 10; j++) {
                Square square = new Square(
                        new Point(mdX * w + j * (kcX * w) + p0.x,
                                mdY * h + i * (kcY * h) + p0.y),
                        (int) (cellSize * w));
                double[] color = square.getColor(mRga1);
                if (color[0] < DARK_COLOR) {
                    square.drawCheckTo(mRga1);
                    maDe[i] = j + "";
                } else square.drawTo(mRga1);
            }
        }
        String md = maDe[2] + maDe[1] + maDe[0];
        return md;
    }

    public String scanSBD(double w, double h, Point p0, Mat mRga1) {
        String[] sbd = new String[6];
        for (int i = 0; i < sbd.length; i++) {
            for (int j = 0; j < 10; j++) {
                Square square = new Square(
                        new Point(sbdX * w + j * (kcX * w) + p0.x,
                                sbdY * h + i * (kcY * h) + p0.y),
                        (int) (cellSize * w));
                double[] color = square.getColor(mRga1);
                if (color[0] < DARK_COLOR) {
                    square.drawCheckTo(mRga1);
                    sbd[i] = j + "";
                } else square.drawTo(mRga1);
            }
        }
        String SBD = sbd[5] + sbd[4] + sbd[3] + sbd[2] + sbd[1] + sbd[0];
        return SBD;
    }

    public void scan(double w, double h, Point p0, Mat mRga1) {
        scanBaiLam(w, h, p0, mRga1);
        scanMaDe(w, h, p0, mRga1);
        scanSBD(w, h, p0, mRga1);
    }
}