package com.khainv9.tracnghiem.models;

import android.graphics.Bitmap;
import android.util.Log;

import com.khainv9.tracnghiem.app.Utils;

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
        if (baiThi == null || deThi == null)
            return 0;
        String[] dapAn = deThi.dapAn;
        String p1 = baiLam[0];
        String p2 = baiLam[1];
        String p3 = baiLam[2];

        String dapAnAll = "";
        for (int i = 0; i < dapAn.length; i++) dapAnAll += dapAn[i];
        Log.d("MyLog", "Start cham bai, bai lam: " + p1 + p2 + p3 + ", dap an" + dapAnAll);
//
//        double diem = 0;
//        for (int i = 0; i < dapAn.length; i++) {
//            if (baiLam[i].equals(dapAn[i])) {
//                diem += baiThi.heDiem;
//            }
//        }
//
        int soCauDung = 0;

//        String[] dapAn = deThi.dapAn;
//
//        for (int i = 0; i < dapAn.length; i++) if (baiLam[i].equals(dapAn[i])) soCauDung++;
//        diemSo = ((double) soCauDung / (double) dapAn.length) * baiThi.heDiem;
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
