package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;

import java.util.Random;


@Message
public class QuestionPaper {
    public static final String NOT = "_";

    public String maDeThi;

    public int soCauPhan1, soCauPhan2, soCauPhan3; // Mỗi câu ở phần 2 và 3 bao gồm 4 ý
    public String[] dapAn;

    public QuestionPaper() {
    }

    public QuestionPaper(int soCauPhan1, int soCauPhan2, int soCauPhan3) {
        this.maDeThi = "";

        this.soCauPhan1 = soCauPhan1;
        this.soCauPhan2 = soCauPhan2;
        this.soCauPhan3 = soCauPhan3;

        this.dapAn = new String[soCauPhan1 + (soCauPhan2 * 4) + (soCauPhan3 * 4)];

        for (int i = 0; i < dapAn.length; i++) this.dapAn[i] = NOT;
    }

    public String[] getDapAnP1(){
        String[] dapAn = new String[soCauPhan1];
        for (int i = 0; i < soCauPhan1; i++) dapAn[i] = this.dapAn[i];
        return dapAn;
    }

    public String[] getDapAnP2(){
        String[] dapAn = new String[soCauPhan2 * 4];
        for (int i = 0; i < soCauPhan2 * 4; i++) dapAn[i] = this.dapAn[soCauPhan1 + i];
        return dapAn;
    }

    public String[] getDapAnP3(){
        String[] dapAn = new String[soCauPhan3 * 4];
        for (int i = 0; i < soCauPhan3 * 4; i++) dapAn[i] = this.dapAn[soCauPhan1 + soCauPhan2 * 4 + i];
        return dapAn;
    }
}
