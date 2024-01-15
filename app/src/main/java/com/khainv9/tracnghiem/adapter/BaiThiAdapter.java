package com.khainv9.tracnghiem.adapter;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khainv9.tracnghiem.DetailActivity;
import com.khainv9.tracnghiem.R;
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.models.Examination;

import java.util.Date;
import java.util.List;


public class BaiThiAdapter extends RecyclerView.Adapter<BaiThiAdapter.BTVH> implements View.OnClickListener {

    List<Examination> dsExamination;
    View.OnLongClickListener onL;

    public BaiThiAdapter(List<Examination> dsExamination, View.OnLongClickListener onL) {
        this.dsExamination = dsExamination;
        this.onL = onL;
    }

    @Override
    public BTVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BTVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bai_thi, parent, false));
    }

    @Override
    public void onBindViewHolder(BTVH holder, int position) {
        Examination examination = dsExamination.get(position);
        holder.ten.setText(examination.name);

        holder.ngay.setText(DatabaseManager.dateString(new Date(examination.createdTime)));
        holder.soCau.setText("Số câu: " + examination.chapterACount + "/" + examination.chapterBCount + "/" + examination.chapterCCount);
        holder.item.setId(examination.id);
        holder.item.setOnClickListener(this);
        if (onL != null) holder.item.setOnLongClickListener(onL);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId();
        v.getContext().startActivity(
                new Intent(v.getContext(), DetailActivity.class)
                        .putExtra(DatabaseManager.ARG_P_BAI_THI, position)
        );
    }

    @Override
    public int getItemCount() {
        return dsExamination.size();
    }


    public class BTVH extends RecyclerView.ViewHolder {

        View item;
        TextView ten, ngay, soCau;

        public BTVH(View itemView) {
            super(itemView);
            ten = itemView.findViewById(R.id.tv_ten_bai);
            ngay = itemView.findViewById(R.id.tv_date);
            soCau = itemView.findViewById(R.id.tv_so_cau);
            item = itemView.findViewById(R.id.item_bai_thi);
        }
    }
}
