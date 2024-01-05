package chamdiemthi.datn.opencv_camera_1.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rantea.opencv_camera_1.R;


public class FuncView {

    private View v;
    public TextView name;
    public ImageView icon;

    public FuncView(LayoutInflater inflater) {
        v = inflater.inflate(R.layout.temp, null);
        name = v.findViewById(R.id.f_name);
        icon = v.findViewById(R.id.f_icon);
    }

    public FuncView setText(String text) {
        this.name.setText(text);
        return this;
    }

    public FuncView setIconRes(int id) {
        icon.setImageResource(id);
        return this;
    }

    public FuncView setOnClickListenter(View.OnClickListener onClickListenter, int id) {
        View item = v.findViewById(R.id.item);
        item.setId(id);
        item.setOnClickListener(onClickListenter);
        return this;
    }

    public View getView() {
        return v;
    }
}
