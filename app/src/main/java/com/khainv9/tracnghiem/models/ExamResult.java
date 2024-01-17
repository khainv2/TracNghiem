package com.khainv9.tracnghiem.models;

import android.graphics.Bitmap;
import android.util.Log;

import com.khainv9.tracnghiem.app.DatabaseManager;

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
    public String studentId;
    public int examinationId;
    public String questionPaperCode;
    public String[] responses;
    public double score;

    public ExamResult(){}
    public ExamResult(String studentId, int examinationId, String questionPaperCode, Bitmap anhBaiThi, String[] responses, boolean isSaveImage) {
        long createdTime = System.currentTimeMillis();
        this.id = (int) (createdTime / 1000);
        this.studentId = studentId;
        this.examinationId = examinationId;
        this.questionPaperCode = questionPaperCode;
        if (isSaveImage)
            DatabaseManager.saveImage(id + "", anhBaiThi);
        this.responses = responses;
        this.score = calculateScore().total();
    }

    public Score calculateScore() {
        Examination examination = DatabaseManager.getExamination(examinationId);
        if (examination == null) {
            Log.e("MyLog", "Khong tim thay bai thi");
            return new Score();
        }
        QuestionPaper questionPaper = DatabaseManager.getQuestionPaper(examination, questionPaperCode);
        if (questionPaper == null) {
            Log.e("MyLog", "Khong tim thay ma de " + questionPaperCode + " trong bai thi " + examination.name);
            return new Score();
        }
        String[] p1Answers = questionPaper.chapterAAnswers();
        String[] p2Answers = questionPaper.chapterBAnswers();
        String[] p3Answers = questionPaper.chapterCAnswers();

        String p1 = responses[0];
        String p2 = responses[1];
        String p3 = responses[2];

        double diemP1 = 0;
        // Điểm thi phần 1, mỗi câu tính 0.25 điểm
        for (int i = 0; i < p1Answers.length && i < p1.length(); i++){
            String actual = p1.charAt(i) + "";
            String expected = p1Answers[i];
            if (expected.equals(actual)){
                diemP1 += 0.25;
            }
        }

        // Điểm thi phần 2, cứ 4 câu liên tiếp: 1 câu đúng được 0 điểm, 2 câu đúng được 0.25 điểm, 3 câu đúng được 0.5 điểm, 4 câu đúng được 1 điểm
        int count = 0;
        double diemP2 = 0;
        for (int i = 0; i < p2Answers.length && i < p2.length(); i++){
            String actual = p2.charAt(i) + "";
            String expected = p2Answers[i];
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
        for (int i = 0; i < p3Answers.length && i < p3.length(); i++){
            String actual = p3.charAt(i) + "";
            String expected = p3Answers[i];
            if (expected.equals(actual) || expected.equals("_")){
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
        for (int i = 0; i < questionPaper.answers.length; i++) dapAnAll += questionPaper.answers[i];
        Log.d("MyLog", "Start cham bai, bai lam: " + p1 + p2 + p3 + ", dap an" + dapAnAll +" , diem so hien tai: " + diemP1 + " " + diemP2 + " " + diemP3);

        Score scoreSo = new Score();
        scoreSo.p1 = diemP1;
        scoreSo.p2 = diemP2;
        scoreSo.p3 = diemP3;
        return scoreSo;
    }

    public Bitmap getAnhBaiThi() {
        return DatabaseManager.loadImage(id + "");
    }

    @Override
    public String toString() {
        String a = studentId + ":" + questionPaperCode + ":" + examinationId + ":" + score + "\n";
        for (int i = 0; i < responses.length; i++)
            a += i + responses[i] + ":";
        return a;
    }
}
