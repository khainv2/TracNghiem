package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;

import java.util.Random;


@Message
public class DeThi {

    public static final String A = "A", B = "B", C = "C", D = "D", E = "E", NOT = "";

    public int id;
    public int maBaiThi;
    public String maDeThi;
    public String[] dapAn;

    public DeThi() {
    }

    public DeThi(int maBaiThi, int soCau) {
        this.id = new Random().nextInt();
        this.maBaiThi = maBaiThi;
        this.maDeThi = "";
        this.dapAn = new String[soCau];
        for (int i = 0; i < dapAn.length; i++) this.dapAn[i] = NOT;
    }

    public DeThi(int id, int maBaiThi, String maDeThi, int soCau) {
        this.id = id;
        this.maBaiThi = maBaiThi;
        this.maDeThi = maDeThi;
        this.dapAn = new String[soCau];
        for (int i = 0; i < dapAn.length; i++) this.dapAn[i] = NOT;
    }

    public int getSoCau() {
        return dapAn.length;
    }
}
