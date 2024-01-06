package com.khainv9.tracnghiem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.khainv9.tracnghiem.R;

import com.khainv9.tracnghiem.adapter.DiemThiAdapter;
import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;
import com.khainv9.tracnghiem.models.DiemThi;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {

    RecyclerView rv;
    DiemThiAdapter diemThiAdapter;
    int i;
    Toolbar toolbar;
    BaiThi baiThi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Xem lại");

        //lấy vị trí của bài thi trong intent gửi đến
        i = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
        baiThi = Utils.dsBaiThi.get(i);
        //init
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<DiemThi> list = new ArrayList<>();
        for (int j = 0; j < Utils.dsDiemThi.size(); j++) {
            DiemThi diemThi = Utils.dsDiemThi.get(j);
            if (diemThi.maBaiThi == baiThi.maBaiThi) list.add(diemThi);
        }
        rv.setAdapter(diemThiAdapter = new DiemThiAdapter(list));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
