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


public class Utils {

    public static String ARG_P_BAI_THI = "P_BAI_THI", ARG_P_DE_THI = "P_DE_THI";
    private static final String dbName = "test6.sqlite";
    private static final String BAI_THI = "BaiThi", HOC_SINH = "HocSinh", DIEM_THI = "DiemThi", HINH_ANH = "HinhAnh";

    public static ArrayList<Examination> dsExamination;
    public static ArrayList<Student> dsStudent;
    public static ArrayList<ExamResult> dsExamResult;

    private static StoreManager storeManager;

    public static void init(Context context) {
        storeManager = StoreManager.open(context, dbName, new String[]{BAI_THI, HOC_SINH, DIEM_THI, HINH_ANH});

        dsExamination = loadBaiThi();
        dsStudent = loadHocSinh();
        dsExamResult = loadDiemThi();
        sort();
    }

    private static void sort() {
        for (int i = 0; i < dsExamination.size(); i++) {
            for (int j = i + 1; j < dsExamination.size(); j++) {
                Examination bt1 = dsExamination.get(i);
                Examination bt2 = dsExamination.get(j);
                if (bt1.getLNgayTao() > bt2.getLNgayTao()) {
                    dsExamination.set(i, bt2);
                    dsExamination.set(j, bt1);
                }
            }
        }
    }

    public static String dateString(Date date) {
        return String.format("%s tháng %s lúc %s:%s", date.getDate(), date.getMonth() + 1, date.getHours(), date.getMinutes());
    }

    public static ArrayList<Examination> loadBaiThi() {
        return storeManager.loads(BAI_THI, Examination.class);
    }

    public static ArrayList<Student> loadHocSinh() {
        return storeManager.loads(HOC_SINH, Student.class);
    }

    public static ArrayList<ExamResult> loadDiemThi() {
        return storeManager.loads(DIEM_THI, ExamResult.class);
    }

    public static void update(Examination examination) {
        for (int i = 0; i < dsExamination.size(); i++) {
            if (dsExamination.get(i).id == examination.id) {
                dsExamination.remove(i);
                break;
            }
        }
        dsExamination.add(examination);
        Log.d("Util", "Write bai thi with ds de thi " + examination.questionPapers.size());
        storeManager.overide(BAI_THI, examination.id + "", 0, examination);
    }

    public static void update(ExamResult examResult) {
        dsExamResult.add(examResult);

        MessagePack msgpack = storeManager.getMessagePack();
        try {
            byte[] b = msgpack.write(examResult);
            storeManager.overide(DIEM_THI, String.valueOf(examResult.id), 0, b);
            ExamResult restore = msgpack.read(b, ExamResult.class);
            Log.d("update: DiemThi", restore.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        storeManager.overide(DIEM_THI, examResult.id + "", 0, examResult);
        ExamResult restore = storeManager.load(DIEM_THI, String.valueOf(examResult.id), ExamResult.class);
        Log.e("update: DiemThi", restore.toString());
    }

    public static void update(Student hs) {
        for (int i = 0; i < dsStudent.size(); i++) {
            if (dsStudent.get(i).id.equals(hs.id)) {
                dsStudent.remove(i);
            }
        }
        dsStudent.add(hs);
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
        dsExamination.remove(examination);
    }

    public static void delete(Student student) {
        del(student);
        dsStudent.remove(student);
    }

    public static void delete(ExamResult examResult) {
        del(examResult);
        storeManager.delete(HINH_ANH, examResult.id + "");
        dsExamResult.remove(examResult);
    }

    public static void deleteAllBT() {
        for (int i = 0; i < dsExamination.size(); i++) del(dsExamination.get(i));
        dsExamination.clear();
    }

    public static void deleteAllHS() {
        for (int i = 0; i < dsStudent.size(); i++) del(dsStudent.get(i));
        dsStudent.clear();
    }

    public static void deleteAllDT() {
        for (int i = 0; i < dsExamResult.size(); i++) del(dsExamResult.get(i));
        dsExamResult.clear();
    }

    public static Examination getBaiThi(int maBaiThi) {
        for (Examination baithi : dsExamination) if (baithi.id == maBaiThi) return baithi;
        return dsExamination.get(0);
    }

    public static QuestionPaper getDethi(Examination examination, String maDeThi) {
        for (int i = 0; i < examination.questionPapers.size(); i++)
            if (examination.questionPapers.get(i).maDeThi.equals(maDeThi)) return examination.questionPapers.get(i);
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

    public static Student getHocSinh(String sbd) {
        for (Student baithi : dsStudent) if (baithi.id.equals(sbd)) return baithi;
        return dsStudent.get(0);
    }
}
