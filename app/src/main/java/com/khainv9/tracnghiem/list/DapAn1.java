package com.khainv9.tracnghiem.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khainv9.tracnghiem.R;

import static com.khainv9.tracnghiem.models.DeThi.A;
import static com.khainv9.tracnghiem.models.DeThi.B;
import static com.khainv9.tracnghiem.models.DeThi.C;
import static com.khainv9.tracnghiem.models.DeThi.D;
import static com.khainv9.tracnghiem.models.DeThi.E;
import static com.khainv9.tracnghiem.models.DeThi.NOT;


public class DapAn1 extends ListMini implements View.OnClickListener {

    public static String[] DAP_AN = {A, B, C, D, E};
    int selected;

    public DapAn1(ViewGroup vg, String dapAn) {
        super(vg);
        selected = -1;
        for (int i = 0; i < DAP_AN.length; i++) {
            if (dapAn.equals(DAP_AN[i])) selected = i;
        }
    }

    @Override
    public VH createItem(int i, LayoutInflater inflater) {
        return new SDapAnVH(inflater.inflate(R.layout.item_circle, null));
    }

    @Override
    public int getNumber() {
        return DAP_AN.length;
    }

    @Override
    public void update(int i) {
        if (i < 0 || i >= getNumber()) return;
        SDapAnVH vh = (SDapAnVH) getMiniVH(i);
        TextView tv = vh.tv;
        tv.setId(i);
        tv.setText(DAP_AN[i]);
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
        return DAP_AN[selected];
    }

    public class SDapAnVH extends VH {
        TextView tv;

        public SDapAnVH(View v) {
            super(v);
            tv = v.findViewById(R.id.tv);
        }
    }
}