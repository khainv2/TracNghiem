package com.khainv9.tracnghiem.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.list.ListDapAn;
import com.khainv9.tracnghiem.list.ListNumber;

import java.util.ArrayList;
import java.util.List;


public class DapAnFragment extends Fragment {

    public static final String ARG_DAP_AN = "DapAn";
    public static final String ARG_S1 = "S1";
    public static final String ARG_S2 = "S2";
    public static final String ARG_S3 = "S3";
    ListDapAn listDapAn;

    public static DapAnFragment create(String[] dapAn, int s1, int s2, int s3) {
        DapAnFragment dapAnFragment = new DapAnFragment();
        Bundle arg = new Bundle();
        if (dapAn != null) arg.putStringArray(ARG_DAP_AN, dapAn);
        arg.putInt(ARG_S1, s1);
        arg.putInt(ARG_S2, s2);
        arg.putInt(ARG_S3, s3);
        dapAnFragment.setArguments(arg);
        return dapAnFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.screen_dap_an, container, false);
        String[] dapAn = getArguments().getStringArray(ARG_DAP_AN);
        int s1 = getArguments().getInt(ARG_S1);
        int s2 = getArguments().getInt(ARG_S2);
        int s3 = getArguments().getInt(ARG_S3);

        List<String> numbers = new ArrayList<>();

        List<String[]> arrs = new ArrayList<>();
        for (int i = 0; i < s1; i++){
            numbers.add("" + (i + 1));
            arrs.add(new String[] { "A", "B", "C", "D" });
        }

        for (int i = 0; i < s2 * 4; i++){
            numbers.add("" + (i / 4 + 1) + "abcd".charAt(i % 4));
            arrs.add(new String[] { "Đ", "S" });
        }

        for (int i = 0; i < s3 * 4; i++){
            numbers.add("" + (i / 4 + 1) + "abcd".charAt(i % 4));
            numbers.add("");
            arrs.add(new String[] { "-", ",", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
        }

        new ListNumber((ViewGroup) v.findViewById(R.id.ll1), numbers).create();
        listDapAn = new ListDapAn((ViewGroup) v.findViewById(R.id.ll2), dapAn, arrs);
        listDapAn.create();
        return v;
    }

    public String[] getListDapAn() {
        return listDapAn.getListDapAn();
    }

}
