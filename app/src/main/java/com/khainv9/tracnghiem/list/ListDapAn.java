package com.khainv9.tracnghiem.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayout;
import com.khainv9.tracnghiem.R;

import java.util.List;


public class ListDapAn extends ListMini {
    List<String[]> listDapAn;
    String[] dapAn;
    ItemRowDapAn[] itemRows;

    public ListDapAn(ViewGroup vg, String[] dapAn, List<String[]> listDapAn) {
        super(vg);
        this.dapAn = dapAn;
        this.listDapAn = listDapAn;
        itemRows = new ItemRowDapAn[dapAn.length];
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
        FlexboxLayout ll = (FlexboxLayout) getMiniVH(i).item;
        itemRows[i] = new ItemRowDapAn(ll, dapAn[i], listDapAn.get(i));
        itemRows[i].create();
    }

    public String[] getListDapAn() {
        String[] all = new String[itemRows.length];
        for (int i = 0; i < itemRows.length; i++) all[i] = itemRows[i].getDapAn();
        return all;
    }

}
