package com.khainv9.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.khainv9.tracnghiem.adapter.DeThiAdapter;
import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;

public class DeThiActivity extends AppCompatActivity {

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
        fab.setOnClickListener(v -> startActivity(
                new Intent(v.getContext(), MaDeActivity.class)
                        .putExtra(Utils.ARG_P_BAI_THI, i)
                        .putExtra(Utils.ARG_P_DE_THI, MaDeActivity.THEM_MOI)
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deThiAdapter != null) deThiAdapter.notifyDataSetChanged();
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
