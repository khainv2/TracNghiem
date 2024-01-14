package com.khainv9.tracnghiem.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.models.Student;

import java.util.ArrayList;


public class HocSinhAdapter extends RecyclerView.Adapter<HocSinhAdapter.HSVH> implements View.OnClickListener {

    ArrayList<Student> ds;
    View.OnLongClickListener onL;

    public HocSinhAdapter(ArrayList<Student> dsStudent, View.OnLongClickListener onL) {
        ds = dsStudent;
        this.onL = onL;
    }

    @Override
    public HSVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HSVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thi_sinh, parent, false));
    }

    @Override
    public void onBindViewHolder(HSVH holder, int position) {
        Student student = ds.get(position);
        holder.ten.setText(student.name);
        holder.sbd.setText(student.id);
        holder.lop.setText(student.class1);
        holder.item.setId(position);
        holder.item.setOnClickListener(this);
        if (onL != null) holder.item.setOnLongClickListener(onL);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId();
    }

    @Override
    public int getItemCount() {
        return ds.size();
    }

    public class HSVH extends RecyclerView.ViewHolder {

        View item;
        TextView ten, sbd, lop;

        public HSVH(View itemView) {
            super(itemView);
            ten = itemView.findViewById(R.id.tv_ten_ts);
            sbd = itemView.findViewById(R.id.tv_sbd);
            lop = itemView.findViewById(R.id.tv_lop);
            item = itemView.findViewById(R.id.item_thi_sinh);
        }
    }
}
