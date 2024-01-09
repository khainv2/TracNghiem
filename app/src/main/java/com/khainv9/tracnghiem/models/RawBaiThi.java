package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;


@Message
public class RawBaiThi {

    public int maBaiThi;
    public DeThi[] dsDeThi;
    public long ngayTao;
    public String tenBaiThi;
    public int soCauPhan1;
    public int soCauPhan2;
    public int soCauPhan3;

    public RawBaiThi() {
    }

    public static RawBaiThi encode(BaiThi baiThi) {
        RawBaiThi raw = new RawBaiThi();
        raw.maBaiThi = baiThi.maBaiThi;
        raw.dsDeThi = baiThi.dsDeThi.toArray(new DeThi[baiThi.dsDeThi.size()]);
        raw.ngayTao = baiThi.getLNgayTao();
        raw.tenBaiThi = baiThi.tenBaiThi;
        raw.soCauPhan1 = baiThi.soCauPhan1;
        raw.soCauPhan2 = baiThi.soCauPhan2;
        raw.soCauPhan3 = baiThi.soCauPhan3;
        return raw;
    }

    public static BaiThi decode(RawBaiThi raw) {
        BaiThi baiThi = new BaiThi(raw.maBaiThi, raw.ngayTao, raw.tenBaiThi, raw.soCauPhan1, raw.soCauPhan2, raw.soCauPhan3);
        for (int i = 0; i < raw.dsDeThi.length; i++) baiThi.dsDeThi.add(raw.dsDeThi[i]);
        return baiThi;
    }

}
