package chamdiemthi.datn.opencv_camera_1.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.rantea.opencv_camera_1.R;

import chamdiemthi.datn.opencv_camera_1.list.ListDapAn;
import chamdiemthi.datn.opencv_camera_1.list.ListNumber;


public class DapAnFragment extends Fragment {

    public static final String ARG_DAP_AN = "DapAn";
    ListDapAn listDapAn;

    public static DapAnFragment create(String[] dapAns) {
        DapAnFragment dapAnFragment = new DapAnFragment();
        Bundle arg = new Bundle();
        if (dapAns != null) arg.putStringArray(ARG_DAP_AN, dapAns);
        dapAnFragment.setArguments(arg);
        return dapAnFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.screen_dap_an, container, false);
        String[] dapAn = getArguments().getStringArray(ARG_DAP_AN);
        new ListNumber((ViewGroup) v.findViewById(R.id.ll1), dapAn.length, 1).create();
        listDapAn = new ListDapAn((ViewGroup) v.findViewById(R.id.ll2), dapAn);
        listDapAn.create();
        return v;
    }

    public String[] getListDapAn() {
        return listDapAn.getListDapAn();
    }
}
