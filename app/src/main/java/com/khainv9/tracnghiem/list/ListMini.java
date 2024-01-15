package com.khainv9.tracnghiem.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.ArrayList;

public abstract class ListMini {

    ArrayList<VH> vhs;
    ViewGroup vg;

    public ListMini(ViewGroup vg) {
        this.vg = vg;
        this.vhs = new ArrayList<>();
        init(vg);
    }

    public void init(ViewGroup ll) {
    }

    public void create() {
        clear();
        LayoutInflater inflater = LayoutInflater.from(vg.getContext());
        for (int i = 0; i < getNumber(); i++) {
            VH vh = createItem(i, inflater);
            vg.addView(vh.item);
            vhs.add(vh);
            update(i);
        }
    }

    public abstract VH createItem(int i, LayoutInflater inflater);

    public abstract int getNumber();

    public abstract void update(int i);

    public VH getMiniVH(int i) {
        return getVhs().get(i);
    }

    public ArrayList<VH> getVhs() {
        return vhs;
    }

    public void clear() {
        vg.removeAllViews();
        vhs.clear();
    }

    public class VH {
        public View item;

        public VH(View v) {
            item = v;
        }
    }

}