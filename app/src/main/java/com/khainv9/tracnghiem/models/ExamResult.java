package com.khainv9.tracnghiem.models;

import android.graphics.Bitmap;
import android.util.Log;

import com.khainv9.tracnghiem.app.Utils;

import org.msgpack.annotation.Message;

import java.util.Random;


@Message
public class ExamResult {

    public static class Score {
        Score() {}

        public double p1, p2, p3;
        public double total() {
            return p1 + p2 + p3;
        }
    }

    public int id;
    public String sbd;
    public int maBaiThi;
    public String maDeThi;
    public String[] baiLam;
    public double diemSo;

    public ExamResult(String sbd, int maBaiThi, String maDeThi, Bitmap anhBaiThi, String[] baiLam, boolean isSaveImage) {
        this.id = new Random().nextInt();
        this.sbd = sbd;
        this.maBaiThi = maBaiThi;
        this.maDeThi = maDeThi;
        if (isSaveImage)
            Utils.saveImage(id + "", anhBaiThi);
        this.baiLam = baiLam;
        this.diemSo = chamBai().total();
    }

    public Score chamBai() {
        Examination examination = Utils.getBaiThi(maBaiThi);
        QuestionPaper questionPaper = Utils.getDethi(examination, maDeThi);
        if (examination == null || questionPaper == null)
            return new Score();
        String[] dapAnP1 = questionPaper.getDapAnP1();
        String[] dapAnP2 = questionPaper.getDapAnP2();
        String[] dapAnP3 = questionPaper.getDapAnP3();

        String p1 = baiLam[0];
        String p2 = baiLam[1];
        String p3 = baiLam[2];

        double diemP1 = 0;
        // Điểm thi phần 1, mỗi câu tính 0.25 điểm
        for (int i = 0; i < dapAnP1.length && i < p1.length(); i++){
            String actual = p1.charAt(i) + "";
            String expected = dapAnP1[i];
            if (expected.equals(actual)){
                diemP1 += 0.25;
            }
        }

        // Điểm thi phần 2, cứ 4 câu liên tiếp: 1 câu đúng được 0 điểm, 2 câu đúng được 0.25 điểm, 3 câu đúng được 0.5 điểm, 4 câu đúng được 1 điểm
        int count = 0;
        double diemP2 = 0;
        for (int i = 0; i < dapAnP2.length && i < p2.length(); i++){
            String actual = p2.charAt(i) + "";
            String expected = dapAnP2[i];
            if (expected.equals(actual)){
                count++;
            }
            if (i % 4 == 3){
                if (count == 2){
                    diemP2 += 0.25;
                } else if (count == 3){
                    diemP2 += 0.5;
                } else if (count == 4){
                    diemP2 += 1;
                }
                count = 0;
            }
        }

        // Điểm thi phần 3: cứ 4 câu liên tiếp: cả 4 câu đúng thì được 0.25 điểm
        count = 0;
        double diemP3 = 0;
        for (int i = 0; i < dapAnP3.length && i < p3.length(); i++){
            String actual = p3.charAt(i) + "";
            String expected = dapAnP3[i];
            if (expected.equals(actual)){
                count++;
            }
            if (i % 4 == 3){
                if (count == 4){
                    diemP3 += 0.25;
                }
                count = 0;
            }
        }

        String dapAnAll = "";
        for (int i = 0; i < questionPaper.dapAn.length; i++) dapAnAll += questionPaper.dapAn[i];
        Log.d("MyLog", "Start cham bai, bai lam: " + p1 + p2 + p3 + ", dap an" + dapAnAll +" , diem so hien tai: " + diemP1 + " " + diemP2 + " " + diemP3);

        Score scoreSo = new Score();
        scoreSo.p1 = diemP1;
        scoreSo.p2 = diemP2;
        scoreSo.p3 = diemP3;
        return scoreSo;
    }

    public Bitmap getAnhBaiThi() {
        return Utils.loadImage(id + "");
    }

    @Override
    public String toString() {
        String a = sbd + ":" + maDeThi + ":" + maBaiThi + ":" + diemSo + "\n";
        for (int i = 0; i < baiLam.length; i++)
            a += i + baiLam[i] + ":";
        return a;
    }
}
