package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;

import java.util.Random;


@Message
public class DeThi {

    public static final String A = "A", B = "B", C = "C", D = "D", E = "E", NOT = "_";

    public int id;
    public int maBaiThi;
    public String maDeThi;

    public int soCauPhan1, soCauPhan2, soCauPhan3;
    public String[] dapAn;

    public DeThi() {
    }

    public DeThi(int maBaiThi, int soCauPhan1, int soCauPhan2, int soCauPhan3) {
        this.id = new Random().nextInt();
        this.maBaiThi = maBaiThi;
        this.maDeThi = "";

        this.soCauPhan1 = soCauPhan1;
        this.soCauPhan2 = soCauPhan2;
        this.soCauPhan3 = soCauPhan3;

        this.dapAn = new String[soCauPhan1 + (soCauPhan2 * 4) + (soCauPhan3 * 4)];

        for (int i = 0; i < dapAn.length; i++) this.dapAn[i] = NOT;
    }


}
