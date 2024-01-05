package chamdiemthi.datn.opencv_camera_1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rantea.opencv_camera_1.R;

import chamdiemthi.datn.opencv_camera_1.adapter.DeThiAdapter;
import chamdiemthi.datn.opencv_camera_1.app.Utils;
import chamdiemthi.datn.opencv_camera_1.models.BaiThi;

public class DeThiActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fab;
    RecyclerView rv;

    DeThiAdapter deThiAdapter;
    int i;
    Toolbar toolbar;
    BaiThi baiThi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_de_thi);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Đề thi");

        //lấy vị trí của bài thi trong intent gửi đến
        i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
        baiThi = Utils.dsBaiThi.get(i);
        //init
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(deThiAdapter = new DeThiAdapter(i));

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deThiAdapter != null) deThiAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(v.getContext(), MaDeActivity.class)
                .putExtra(Utils.ARG_P_BAI_THI, i)
                .putExtra(Utils.ARG_P_DE_THI, MaDeActivity.THEM_MOI));
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
