package com.khainv9.tracnghiem.adapter;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.ImageActivity;

import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.ExamResult;
import com.khainv9.tracnghiem.models.Student;

import java.util.ArrayList;
import java.util.List;


public class DiemThiAdapter extends RecyclerView.Adapter<DiemThiAdapter.DTVH>
        implements View.OnClickListener, View.OnLongClickListener {

    public List<ExamResult> ds;

    public DiemThiAdapter(List<ExamResult> dsExamResult) {
        ds = dsExamResult;
    }

    public void updateList(List<ExamResult> dsExamResult){
        ds = dsExamResult;
        notifyDataSetChanged();
    }

    @Override
    public DTVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DTVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diem_thi, parent, false));
    }

    @Override
    public void onBindViewHolder(DTVH holder, int position) {
        ExamResult d = ds.get(position);
        Student st = DatabaseManager.getStudentById(d.studentId);
        if (st == null){
            holder.tenTs.setText("Không rõ");
        } else {
            holder.tenTs.setText(st.name);
        }
        holder.sbd.setText("SBD: " + d.studentId);
        Examination examination = DatabaseManager.getExamination(d.examinationId);
        holder.tenBaiThi.setText(examination.name);
        holder.maDe.setText(d.questionPaperCode);
        holder.soDiem.setText(String.format("%.2f điểm", d.score, 93));

        holder.item.setId(position);
        holder.item.setOnLongClickListener(this);
        holder.item.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId();
        int id = ds.get(position).id;
        v.getContext().startActivity(new Intent(v.getContext(), ImageActivity.class)
                        .putExtra("i", id));
    }

    @Override
    public int getItemCount() {
        return ds.size();
    }

    @Override
    public boolean onLongClick(View v) {
        int position = v.getId();
        DatabaseManager.delete(ds.get(position));
        notifyDataSetChanged();
        return true;
    }

    public class DTVH extends RecyclerView.ViewHolder {

        View item;
        TextView tenTs, sbd, tenBaiThi, maDe, soDiem;

        public DTVH(View itemView) {
            super(itemView);
            tenTs = itemView.findViewById(R.id.tv_ten_ts);
            sbd = itemView.findViewById(R.id.tv_sbd);
            tenBaiThi = itemView.findViewById(R.id.tv_ten_bai_thi);
            maDe = itemView.findViewById(R.id.tv_ma_de);
            soDiem = itemView.findViewById(R.id.tv_diem);
            item = itemView.findViewById(R.id.item_thi_sinh);
        }
    }
}
