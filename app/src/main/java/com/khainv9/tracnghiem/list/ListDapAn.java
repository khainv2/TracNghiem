package com.khainv9.tracnghiem.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.khainv9.tracnghiem.R;


public class ListDapAn extends ListMini {

    String[] dapAn;
    DapAn1[] dapAn1s;

    public ListDapAn(ViewGroup vg, String[] dapAn) {
        super(vg);
        this.dapAn = dapAn;
        dapAn1s = new DapAn1[dapAn.length];
    }

    @Override
    public VH createItem(int i, LayoutInflater inflater) {
        return new VH(inflater.inflate(R.layout.item_list_dap_an, null));
    }

    @Override
    public int getNumber() {
        return dapAn.length;
    }

    @Override
    public void update(int i) {
        LinearLayout ll = (LinearLayout) getMiniVH(i).item;
        dapAn1s[i] = new DapAn1(ll, dapAn[i]);
        dapAn1s[i].create();
    }

    public String[] getListDapAn() {
        String[] all = new String[dapAn1s.length];
        for (int i = 0; i < dapAn1s.length; i++) all[i] = dapAn1s[i].getDapAn();
        return all;
    }
}
