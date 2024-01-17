package com.khainv9.tracnghiem.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.QuestionPaper;
import com.khainv9.tracnghiem.models.ExamResult;
import com.khainv9.tracnghiem.models.Student;
import com.rantea.rsmanager.StoreManager;

import org.msgpack.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseManager {

    public static String ARG_P_BAI_THI = "P_BAI_THI", ARG_P_DE_THI = "P_DE_THI";
    private static final String dbName = "t3.sqlite";
    private static final String BAI_THI = "BaiThi", HOC_SINH = "HocSinh", DIEM_THI = "DiemThi", HINH_ANH = "HinhAnh";

    private static ArrayList<Examination> examinations;
    public static ArrayList<Student> students;
    private static ArrayList<ExamResult> examResults;

    private static StoreManager storeManager;

    public static void init(Context context) {
        storeManager = StoreManager.open(context, dbName, new String[]{BAI_THI, HOC_SINH, DIEM_THI, HINH_ANH});

        examinations = storeManager.loads(BAI_THI, Examination.class);
        students = storeManager.loads(HOC_SINH, Student.class);
        examResults = storeManager.loads(DIEM_THI, ExamResult.class);
        sort();
    }

    private static void sort() {
        for (int i = 0; i < examinations.size(); i++) {
            for (int j = i + 1; j < examinations.size(); j++) {
                Examination bt1 = examinations.get(i);
                Examination bt2 = examinations.get(j);
                if (bt1.getLNgayTao() > bt2.getLNgayTao()) {
                    examinations.set(i, bt2);
                    examinations.set(j, bt1);
                }
            }
        }
    }

    public static List<ExamResult> getExamResults() {
        return examResults;
    }

    public static ExamResult getExamResultById(int id) {
        for (ExamResult examResult : examResults) if (examResult.id == id) return examResult;
        return null;
    }

    public static String dateString(Date date) {
        return String.format("%s tháng %s lúc %s:%s", date.getDate(), date.getMonth() + 1, date.getHours(), date.getMinutes());
    }

    public static void update(Examination examination) {
        for (int i = 0; i < examinations.size(); i++) {
            if (examinations.get(i).id == examination.id) {
                examinations.remove(i);
                break;
            }
        }
        examinations.add(examination);
        Log.d("Util", "Write bai thi with ds de thi " + examination.questionPapers.size());
        storeManager.overide(BAI_THI, examination.id + "", 0, examination);
    }

    public static void update(ExamResult examResult) {
        examResults.add(examResult);

        MessagePack msgpack = storeManager.getMessagePack();
        try {
            byte[] b = msgpack.write(examResult);
            storeManager.overide(DIEM_THI, String.valueOf(examResult.id), 0, b);
            ExamResult restore = msgpack.read(b, ExamResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        storeManager.overide(DIEM_THI, examResult.id + "", 0, examResult);
        ExamResult restore = storeManager.load(DIEM_THI, String.valueOf(examResult.id), ExamResult.class);
    }

    public static void update(Student hs) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).id.equals(hs.id)) {
                students.remove(i);
                break;
            }
        }
        students.add(hs);
        storeManager.overide(HOC_SINH, hs.id, 0, hs);
    }

    public static Bitmap loadImage(String id) {
        Log.e("loadImage: ", id);
        byte[] data = storeManager.load(HINH_ANH, id);
        if (data != null)
            return getImage(data);
        return null;
    }

    public static void saveImage(String id, Bitmap bm) {
        storeManager.overide(HINH_ANH, id, 0, getBytes(bm));
    }

    // convert from bitmap to byte array
    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static void delete(Examination examination) {
        del(examination);
        examinations.remove(examination);
    }

    public static void delete(Student student) {
        del(student);
        students.remove(student);
    }

    public static void delete(ExamResult examResult) {
        del(examResult);
        storeManager.delete(HINH_ANH, examResult.id + "");
        examResults.remove(examResult);
    }

    public static void deleteAllExaminations() {
        for (int i = 0; i < examinations.size(); i++) del(examinations.get(i));
        examinations.clear();
    }

    public static void deleteAllStudents() {
        for (int i = 0; i < students.size(); i++) del(students.get(i));
        students.clear();
    }

    public static void deleteAllDT() {
        for (int i = 0; i < examResults.size(); i++) del(examResults.get(i));
        examResults.clear();
    }

    public static Examination getExamination(int maBaiThi) {
        for (Examination exam : examinations)
            if (exam.id == maBaiThi) return exam;
        return null;
    }

    public static List<Examination> getExaminations() {
        return examinations;
    }

    public static QuestionPaper getQuestionPaper(Examination examination, String paperCode) {
        for (int i = 0; i < examination.questionPapers.size(); i++) {
            QuestionPaper qp = examination.questionPapers.get(i);
            if (qp == null || qp.paperCode == null)
                continue;
            if (qp.paperCode.equals(paperCode))
                return examination.questionPapers.get(i);
        }
        return null;
    }

    private static void del(Examination examination) {
        storeManager.delete(BAI_THI, examination.id + "");
    }

    private static void del(ExamResult examResult) {
        storeManager.delete(DIEM_THI, examResult.id + "");
    }

    private static void del(Student student) {
        storeManager.delete(HOC_SINH, student.id);
    }

    public static Student getStudentById(String id) {
        for (Student student : students) if (student.id.equals(id)) return student;
        return null;
    }
}
