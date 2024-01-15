package com.khainv9.tracnghiem.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.MaDeActivity;
import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.QuestionPaper;


public class DeThiAdapter extends RecyclerView.Adapter<DeThiAdapter.BTVH> implements View.OnClickListener {

    int iBT;
    Examination examination;

    public DeThiAdapter(int iBT) {
        this.iBT = iBT;
        examination = Utils.dsExamination.get(iBT);
    }

    @Override
    public BTVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BTVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_de_thi, parent, false));
    }

    @Override
    public void onBindViewHolder(BTVH holder, int position) {
        QuestionPaper questionPaper = examination.questionPapers.get(position);
        holder.ma.setText("" + questionPaper.maDeThi);
        holder.item.setId(position);
        holder.item.setOnClickListener(this);
        holder.item.setOnLongClickListener(v -> {
            // Tạo alert dialog confirm
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Xóa đề thi");
            builder.setMessage("Bạn có chắc chắn muốn xóa đề thi này?");
            builder.setPositiveButton("Xóa", (dialog, which) -> {
                examination.questionPapers.remove(position);
                Utils.update(examination);
                notifyDataSetChanged();
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        int position = v.getId();
        v.getContext().startActivity(
                new Intent(v.getContext(), MaDeActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, iBT)
                        .putExtra(Utils.ARG_P_DE_THI, position)
        );
    }

    @Override
    public int getItemCount() {
        return examination.questionPapers.size();
    }


    public class BTVH extends RecyclerView.ViewHolder {

        View item;
        TextView ma;

        public BTVH(View itemView) {
            super(itemView);
            ma = itemView.findViewById(R.id.tv_ma_de);
            item = itemView.findViewById(R.id.item_bai_thi);
        }
    }
}
