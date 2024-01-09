package com.khainv9.tracnghiem.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khainv9.tracnghiem.R;

import static com.khainv9.tracnghiem.models.DeThi.NOT;


public class ItemRowDapAn extends ListMini implements View.OnClickListener {

    public String[] listDapAn;
    int selected;

    public ItemRowDapAn(ViewGroup vg, String dapAn, String[] listDapAn) {
        super(vg);
        this.listDapAn = listDapAn;
        selected = -1;
        for (int i = 0; i < this.listDapAn.length; i++) {
            if (dapAn.equals(this.listDapAn[i])) selected = i;
        }
    }

    @Override
    public VH createItem(int i, LayoutInflater inflater) {
        return new SDapAnVH(inflater.inflate(R.layout.item_circle, null));
    }

    @Override
    public int getNumber() {
        return listDapAn.length;
    }

    @Override
    public void update(int i) {
        if (i < 0 || i >= listDapAn.length) return;
        SDapAnVH vh = (SDapAnVH) getMiniVH(i);
        TextView tv = vh.tv;
        tv.setId(i);
        tv.setText(listDapAn[i]);
        if (selected == i) tv.setBackgroundResource(R.drawable.bg_circle_1);
        else tv.setBackgroundResource(R.drawable.bg_circle_2);
        tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int oldSelected = selected;
        update(selected = v.getId());
        update(oldSelected);
    }

    public String getDapAn() {
        if (selected == -1) return NOT;
        return listDapAn[selected];
    }

    public class SDapAnVH extends VH {
        TextView tv;

        public SDapAnVH(View v) {
            super(v);
            tv = v.findViewById(R.id.tv);
        }
    }
}
