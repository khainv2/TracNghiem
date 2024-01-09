package com.khainv9.tracnghiem.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khainv9.tracnghiem.R;

import static android.graphics.Color.BLACK;

import java.util.ArrayList;
import java.util.List;


public class ListNumber extends ListMini {
    List<String> titles;
    public ListNumber(ViewGroup vg, List<String> numbers) {
        super(vg); this.titles = numbers;
    }

    @Override
    public VH createItem(int i, LayoutInflater inflater) {
        return new VH(inflater.inflate(R.layout.item_circle, null));
    }

    @Override
    public int getNumber() {
        return titles.size();
    }

    @Override
    public void update(int i) {
        TextView v = (TextView) getMiniVH(i).item.findViewById(R.id.tv);
        String value = titles.get(i);
        if (value.isEmpty()){
            v.setVisibility(View.INVISIBLE);
        } else {
            v.setBackgroundResource(R.drawable.bg_circle_round);
            v.setTextColor(BLACK);
            v.setText(value);
        }
    }
}
