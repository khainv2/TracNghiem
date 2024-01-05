package chamdiemthi.datn.opencv_camera_1.models;

import android.graphics.Bitmap;

import chamdiemthi.datn.opencv_camera_1.app.Utils;

import org.msgpack.annotation.Message;

import java.util.Random;


@Message
public class  DiemThi {

    public int id;
    public String sbd;
    public int maBaiThi;
    public String maDeThi;
    public String[] baiLam;
    public double diemSo;

    public DiemThi() {
    }

    public DiemThi(String sbd, int maBaiThi, String maDeThi, Bitmap anhBaiThi, String[] baiLam) {
        this.id = new Random().nextInt();
        this.sbd = sbd;
        this.maBaiThi = maBaiThi;
        this.maDeThi = maDeThi;
        Utils.saveImage(id + "", anhBaiThi);
        this.baiLam = baiLam;
        this.diemSo = chamBai();
    }

    public double chamBai() {
        BaiThi baiThi = Utils.getBaiThi(maBaiThi);
        DeThi deThi = Utils.getDethi(baiThi, maDeThi);
        int soCauDung = 0;
        String[] dapAn = deThi.dapAn;

        for (int i = 0; i < dapAn.length; i++) if (baiLam[i].equals(dapAn[i])) soCauDung++;
        diemSo = ((double) soCauDung / (double) dapAn.length) * baiThi.heDiem;
        return diemSo;
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
