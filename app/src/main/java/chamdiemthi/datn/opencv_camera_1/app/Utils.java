package chamdiemthi.datn.opencv_camera_1.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import chamdiemthi.datn.opencv_camera_1.models.BaiThi;
import chamdiemthi.datn.opencv_camera_1.models.DeThi;
import chamdiemthi.datn.opencv_camera_1.models.DiemThi;
import chamdiemthi.datn.opencv_camera_1.models.RawBaiThi;
import chamdiemthi.datn.opencv_camera_1.models.HocSinh;
import com.rantea.rsmanager.StoreManager;

import org.msgpack.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class Utils {

    public static String ARG_P_BAI_THI = "P_BAI_THI", ARG_P_DE_THI = "P_DE_THI";
    private static final String dbName = "test.sqlite";
    private static final String BAI_THI = "BaiThi", HOC_SINH = "HocSinh", DIEM_THI = "DiemThi", HINH_ANH = "HinhAnh";

    public static ArrayList<BaiThi> dsBaiThi;
    public static ArrayList<HocSinh> dsHocSinh;
    public static ArrayList<DiemThi> dsDiemThi;

    private static StoreManager storeManager;

    public static void init(Context context) {
        storeManager = StoreManager.open(context, dbName, new String[]{BAI_THI, HOC_SINH, DIEM_THI, HINH_ANH});
        dsBaiThi = loadBaiThi();
        dsHocSinh = loadHocSinh();
        dsDiemThi = loadDiemThi();
        sort();
    }

    private static void sort() {
        for (int i = 0; i < dsBaiThi.size(); i++) {
            for (int j = i + 1; j < dsBaiThi.size(); j++) {
                BaiThi bt1 = dsBaiThi.get(i);
                BaiThi bt2 = dsBaiThi.get(j);
                if (bt1.getLNgayTao() > bt2.getLNgayTao()) {
                    dsBaiThi.set(i, bt2);
                    dsBaiThi.set(j, bt1);
                }
            }
        }
    }

    public static String dateString(Date date) {
        return String.format("%s tháng %s lúc %s:%s", date.getDate(), date.getMonth() + 1, date.getHours(), date.getMinutes());
    }

    public static ArrayList<BaiThi> loadBaiThi() {
        ArrayList<RawBaiThi> raws = storeManager.loads(BAI_THI, RawBaiThi.class);
        ArrayList<BaiThi> listBaiThi = new ArrayList<>();
        for (int i = 0; i < raws.size(); i++) {
            listBaiThi.add(RawBaiThi.decode(raws.get(i)));
        }
        return listBaiThi;
    }

    public static ArrayList<HocSinh> loadHocSinh() {
        return storeManager.loads(HOC_SINH, HocSinh.class);
    }

    public static ArrayList<DiemThi> loadDiemThi() {
        return storeManager.loads(DIEM_THI, DiemThi.class);
    }

    public static void update(BaiThi baiThi) {
        for (int i = 0; i < dsBaiThi.size(); i++) {
            if (dsBaiThi.get(i).maBaiThi == baiThi.maBaiThi) {
                dsBaiThi.remove(i);
            }
        }
        dsBaiThi.add(baiThi);
        RawBaiThi raw = RawBaiThi.encode(baiThi);
        storeManager.overide(BAI_THI, baiThi.maBaiThi + "", 0, raw);
    }

    public static void update(DiemThi diemThi) {
        dsDiemThi.add(diemThi);

        MessagePack msgpack = storeManager.getMessagePack();
        try {
            byte[] b = msgpack.write(diemThi);
            storeManager.overide(DIEM_THI, String.valueOf(diemThi.id), 0, b);
//            DiemThi restore = msgpack.read(b, DiemThi.class);
//            Log.e("update: DiemThi", restore.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        storeManager.overide(DIEM_THI, diemThi.id + "", 0, diemThi);
//        DiemThi restore = storeManager.load(DIEM_THI, String.valueOf(diemThi.id), DiemThi.class);
//        Log.e("update: DiemThi", restore.toString());
    }

    public static void update(HocSinh hs) {
        for (int i = 0; i < dsHocSinh.size(); i++) {
            if (dsHocSinh.get(i).sbd.equals(hs.sbd)) {
                dsHocSinh.remove(i);
            }
        }
        dsHocSinh.add(hs);
        storeManager.overide(HOC_SINH, hs.sbd, 0, hs);
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

    public static void delete(BaiThi baiThi) {
        del(baiThi);
        dsBaiThi.remove(baiThi);
    }

    public static void delete(HocSinh hocSinh) {
        del(hocSinh);
        dsHocSinh.remove(hocSinh);
    }

    public static void delete(DiemThi diemThi) {
        del(diemThi);
        storeManager.delete(HINH_ANH, diemThi.id + "");
        dsDiemThi.remove(diemThi);
    }

    public static void deleteAllBT() {
        for (int i = 0; i < dsBaiThi.size(); i++) del(dsBaiThi.get(i));
        dsBaiThi.clear();
    }

    public static void deleteAllHS() {
        for (int i = 0; i < dsHocSinh.size(); i++) del(dsHocSinh.get(i));
        dsHocSinh.clear();
    }

    public static void deleteAllDT() {
        for (int i = 0; i < dsDiemThi.size(); i++) del(dsDiemThi.get(i));
        dsDiemThi.clear();
    }

    public static BaiThi getBaiThi(int maBaiThi) {
        for (BaiThi baithi : dsBaiThi) if (baithi.maBaiThi == maBaiThi) return baithi;
        return dsBaiThi.get(0);
    }

    public static DeThi getDethi(BaiThi baiThi, String maDeThi) {
        for (int i = 0; i < baiThi.dsDeThi.size(); i++)
            if (baiThi.dsDeThi.get(i).maDeThi.equals(maDeThi)) return baiThi.dsDeThi.get(i);
        return baiThi.dsDeThi.get(0);
    }

    private static void del(BaiThi baiThi) {
        storeManager.delete(BAI_THI, baiThi.maBaiThi + "");
    }

    private static void del(DiemThi diemThi) {
        storeManager.delete(DIEM_THI, diemThi.id + "");
    }

    private static void del(HocSinh hocSinh) {
        storeManager.delete(HOC_SINH, hocSinh.sbd);
    }

    public static HocSinh getHocSinh(String sbd) {
        for (HocSinh baithi : dsHocSinh) if (baithi.sbd.equals(sbd)) return baithi;
        return dsHocSinh.get(0);
    }
}
