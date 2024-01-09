package com.khainv9.tracnghiem.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class BaiThi {

    public int maBaiThi;
    public ArrayList<DeThi> dsDeThi;
    public Date ngayTao;
    public String tenBaiThi;
    public int soCauPhan1;
    public int soCauPhan2;
    public int soCauPhan3;

    public BaiThi(String tenBaiThi, int soCauPhan1, int soCauPhan2, int soCauPhan3) {
        Random r = new Random();
        this.maBaiThi = r.nextInt();
        this.tenBaiThi = tenBaiThi;
        this.ngayTao = new Date();
        this.dsDeThi = new ArrayList<>();

        this.soCauPhan1 = soCauPhan1;
        this.soCauPhan2 = soCauPhan2;
        this.soCauPhan3 = soCauPhan3;
    }

    public BaiThi(int maBaiThi, long ngayTao, String tenBaiThi, int soCauPhan1, int soCauPhan2, int soCauPhan3) {
        this.maBaiThi = maBaiThi;
        this.ngayTao = new Date(ngayTao);
        this.tenBaiThi = tenBaiThi;
        this.dsDeThi = new ArrayList<>();

        this.soCauPhan1 = soCauPhan1;
        this.soCauPhan2 = soCauPhan2;
        this.soCauPhan3 = soCauPhan3;
    }

    public DeThi addDeThi() {
        DeThi deThi = new DeThi(maBaiThi, soCauPhan1, soCauPhan2, soCauPhan3);
        this.dsDeThi.add(deThi);
        return deThi;
    }

    public long getLNgayTao() {
        return ngayTao.getTime();
    }
}
