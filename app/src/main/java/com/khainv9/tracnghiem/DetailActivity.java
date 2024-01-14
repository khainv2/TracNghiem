package com.khainv9.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.khainv9.tracnghiem.app.Utils;

import com.khainv9.tracnghiem.scan.CameraActivity;
import com.khainv9.tracnghiem.scan.ScanImageActivity;
import com.khainv9.tracnghiem.view.FuncView;


public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int F_DAP_AN = 100, F_CHAM_BAI = 101, F_XEM_LAI = 102, F_THONG_KE = 103, F_THONG_TIN = 104, F_CHAM_BAI_ANH = 105;

    int i;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Kiểm tra");

        //lấy vị trí của bài thi trong intent gửi đến
        i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);

        LinearLayout content = findViewById(R.id.content);
        LayoutInflater inflater = LayoutInflater.from(this);

        content.addView(new FuncView(inflater)
                .setText("Danh sách đề thi")
                .setIconRes(R.drawable.ic_vpn_key_black_24dp)
                .setOnClickListenter(this, F_DAP_AN)
                .getView());
        content.addView(new FuncView(inflater)
                .setText("Chấm bài qua camera")
                .setIconRes(R.drawable.ic_camera_alt_black_24dp)
                .setOnClickListenter(this, F_CHAM_BAI)
                .getView());
        content.addView(new FuncView(inflater)
                .setText("Chấm bài qua ảnh")
                .setIconRes(R.drawable.ic_content_copy_black_24dp)
                .setOnClickListenter(this, F_CHAM_BAI_ANH)
                .getView());
        content.addView(new FuncView(inflater)
                .setText("Xem lại")
                .setIconRes(R.drawable.ic_format_list_bulleted_black_24dp)
                .setOnClickListenter(this, F_XEM_LAI)
                .getView());
        content.addView(new FuncView(inflater)
                .setText("Thống kê")
                .setIconRes(R.drawable.ic_show_chart_black_24dp)
                .setOnClickListenter(this, F_THONG_KE)
                .getView());
        content.addView(new FuncView(inflater)
                .setText("Thông tin")
                .setIconRes(R.drawable.ic_error_black_24dp)
                .setOnClickListenter(this, F_THONG_TIN)
                .getView());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case F_DAP_AN: {
                Intent intent = new Intent(this, DeThiActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, i);
                startActivity(intent);
                break;
            }
            case F_CHAM_BAI: {
                if (Utils.dsStudent.isEmpty()) {
                    Toast.makeText(this, "Danh sách học sinh trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Utils.dsBaiThi.get(i).dsDeThi.isEmpty()) {
                    Toast.makeText(this, "Danh sách đề thi trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, CameraActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, i);
                startActivity(intent);
                break;
            }
            case F_CHAM_BAI_ANH: {
                if (Utils.dsStudent.isEmpty()) {
                    Toast.makeText(this, "Danh sách học sinh trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Utils.dsBaiThi.get(i).dsDeThi.isEmpty()) {
                    Toast.makeText(this, "Danh sách đề thi trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, ScanImageActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, i);
                startActivity(intent);
                break;
            }
            case F_XEM_LAI: {
                Intent intent = new Intent(this, InfoActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, i);
                startActivity(intent);
                break;
            }
            case F_THONG_KE:
                break;
            case F_THONG_TIN:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
