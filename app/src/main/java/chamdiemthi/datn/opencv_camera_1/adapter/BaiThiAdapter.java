package chamdiemthi.datn.opencv_camera_1.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.rantea.opencv_camera_1.R;

import chamdiemthi.datn.opencv_camera_1.DetailActivity;
import chamdiemthi.datn.opencv_camera_1.app.Utils;
import chamdiemthi.datn.opencv_camera_1.models.BaiThi;

import java.util.ArrayList;


public class BaiThiAdapter extends RecyclerView.Adapter<BaiThiAdapter.BTVH> implements View.OnClickListener {

    ArrayList<BaiThi> dsBaiThi;
    View.OnLongClickListener onL;

    public BaiThiAdapter(ArrayList<BaiThi> dsBaiThi, View.OnLongClickListener onL) {
        this.dsBaiThi = dsBaiThi;
        this.onL = onL;
    }

    @Override
    public BTVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BTVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bai_thi, parent, false));
    }

    @Override
    public void onBindViewHolder(BTVH holder, int position) {
        BaiThi baiThi = dsBaiThi.get(position);
        holder.ten.setText(baiThi.tenBaiThi);
        holder.ngay.setText(Utils.dateString(baiThi.ngayTao));
        holder.soCau.setText(baiThi.soCau + " câu");
        holder.item.setId(position);
        holder.item.setOnClickListener(this);
        if (onL != null) holder.item.setOnLongClickListener(onL);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId();
        v.getContext().startActivity(
                new Intent(v.getContext(), DetailActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, position)
        );
    }

    @Override
    public int getItemCount() {
        return dsBaiThi.size();
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
