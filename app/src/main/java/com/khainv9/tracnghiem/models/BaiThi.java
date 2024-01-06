package com.khainv9.tracnghiem.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class BaiThi {

    public static int S20 = 20, S40 = 40, S60 = 60;

    public int maBaiThi;
    public ArrayList<DeThi> dsDeThi;
    public Date ngayTao;
    public String tenBaiThi;
    public int soCau;
    public int heDiem;

    public BaiThi(String tenBaiThi, int soCau, int heDiem) {
        Random r = new Random();
        this.maBaiThi = r.nextInt();
        this.tenBaiThi = tenBaiThi;
        this.ngayTao = new Date();
        this.dsDeThi = new ArrayList<>();
        this.soCau = soCau;
        this.heDiem = heDiem;
    }

    public BaiThi(int maBaiThi, long ngayTao, String tenBaiThi, int soCau, int heDiem) {
        this.maBaiThi = maBaiThi;
        this.ngayTao = new Date(ngayTao);
        this.tenBaiThi = tenBaiThi;
        this.soCau = soCau;
        this.heDiem = heDiem;
        this.dsDeThi = new ArrayList<>();
    }

    public DeThi addDeThi() {
        DeThi deThi =new DeThi(maBaiThi, soCau);
        this.dsDeThi.add(deThi);
        return deThi;
    }

    public int getSoCau() {
        return soCau;
    }

    public long getLNgayTao() {
        return ngayTao.getTime();
    }
}
