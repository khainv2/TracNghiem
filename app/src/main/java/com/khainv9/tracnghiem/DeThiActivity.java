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
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.models.Examination;

public class DeThiActivity extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView rv;

    DeThiAdapter deThiAdapter;
    int examId;
    Toolbar toolbar;
    Examination examination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_de_thi);
        toolbar = findViewById(R.id.ct_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Đề thi");

        //lấy vị trí của bài thi trong intent gửi đến
        examId = getIntent().getIntExtra(DatabaseManager.ARG_P_BAI_THI, 0);
        examination = DatabaseManager.getExamination(examId);

        //init
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(deThiAdapter = new DeThiAdapter(examId));

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(
                new Intent(v.getContext(), MaDeActivity.class)
                        .putExtra(DatabaseManager.ARG_P_BAI_THI, examId)
                        .putExtra(DatabaseManager.ARG_P_DE_THI, MaDeActivity.THEM_MOI)
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        deThiAdapter.notifyDataSetChanged();
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
