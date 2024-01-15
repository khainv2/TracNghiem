package com.khainv9.tracnghiem;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.khainv9.tracnghiem.adapter.DiemThiAdapter;
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.ExamResult;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {

    RecyclerView rv;
    DiemThiAdapter diemThiAdapter;
    int i;
    Toolbar toolbar;
    Examination examination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Xem lại");

        //lấy vị trí của bài thi trong intent gửi đến
        i = getIntent().getIntExtra(DatabaseManager.ARG_P_BAI_THI, 0);
        examination = DatabaseManager.getExamination(i);
        //init
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<ExamResult> list = new ArrayList<>();
        for (int j = 0; j < DatabaseManager.examResults.size(); j++) {
            ExamResult examResult = DatabaseManager.examResults.get(j);
            if (examResult.examinationId == examination.id) list.add(examResult);
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
