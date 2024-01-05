package chamdiemthi.datn.opencv_camera_1.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rantea.opencv_camera_1.R;

import chamdiemthi.datn.opencv_camera_1.list.ListNumber;
import chamdiemthi.datn.opencv_camera_1.list.ListSelectNumber;


public class MaDeFragment extends Fragment {

    public static final String ARG_MA_DE = "MaDe";

    ListSelectNumber[] lsNumbrer;

    public MaDeFragment() {
    }

    public static MaDeFragment create(String made) {
        MaDeFragment maDeFragment = new MaDeFragment();
        Bundle arg = new Bundle();
        if (made != null) arg.putString(ARG_MA_DE, made);
        maDeFragment.setArguments(arg);
        return maDeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.screen_ma_de, container, false);
        LinearLayout[] lls = {
                v.findViewById(R.id.ll2),
                v.findViewById(R.id.ll3),
                v.findViewById(R.id.ll4)
        };

        String made = getArguments().getString(ARG_MA_DE);
        int[] selected = new int[]{-1, -1, -1};
        if (made != null && made.length() == 3) {
            try {
                for (int i = 0; i < selected.length; i++)
                    selected[i] = Integer.parseInt(String.valueOf(made.charAt(i)));
            } catch (Exception e) {
            }
        }
        int number = 10;
        new ListNumber((ViewGroup) v.findViewById(R.id.ll1), number).create();
        lsNumbrer = new ListSelectNumber[3];
        for (int i = 0; i < lsNumbrer.length; i++) {
            lsNumbrer[i] = new ListSelectNumber(lls[i], number, selected[i]);
            lsNumbrer[i].create();
        }
        return v;
    }

    public String getMaDe() {
        String a = "";
        for (int i = 0; i < lsNumbrer.length; i++) {
            a += lsNumbrer[i].getSelected();
        }
        if (a.contains("-1")) return null;
        return a;
    }
}
