package com.khainv9.tracnghiem;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.Toast;

import com.khainv9.tracnghiem.adapter.DiemThiAdapter;
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.app.ExcelUtil;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.ExamResult;
import com.khainv9.tracnghiem.models.Student;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoActivity extends AppCompatActivity {

    RecyclerView rv;
    DiemThiAdapter diemThiAdapter;
    int examId;
    Toolbar toolbar;
    Examination examination;

    RadioButton rb_ViewAll;
    RadioButton rb_ByABC;
    RadioButton rb_ByScore;

    void updateView(){

        ArrayList<ExamResult> list = new ArrayList<>();
        List<ExamResult> examResults = DatabaseManager.getExamResults();
        if (rb_ViewAll.isChecked()){
            for (int i = 0; i < DatabaseManager.getExamResults().size(); i++) {
                ExamResult examResult = DatabaseManager.getExamResults().get(i);
                if (examResult.examinationId == examination.id) list.add(examResult);
            }
        } else if (rb_ByABC.isChecked() || rb_ByScore.isChecked()){
            // Lấy toàn bộ danh sách, mỗi học sinh 1 điểm số cuối cùng đc scan
            Map<String, ExamResult> map = new HashMap<>();
            for (int i = 0; i < examResults.size(); i++) {
                ExamResult examResult = examResults.get(i);
                if (examResult.examinationId == examination.id){
                    // Chỉ lấy exam result có với từng studentId có id cao nhất
                    if (map.containsKey(examResult.studentId)){
                        ExamResult examResult1 = map.get(examResult.studentId);
                        if (examResult1.id < examResult.id) map.put(examResult.studentId, examResult);
                    } else map.put(examResult.studentId, examResult);
                }
            }
            list = new ArrayList<>(map.values());
            list.sort((o1, o2) -> {
                if (rb_ByABC.isChecked()){
                    Student st1 = DatabaseManager.getStudentById(o1.studentId);
                    Student st2 = DatabaseManager.getStudentById(o2.studentId);
                    if (st1 == null){
                        if (st2 == null) return 0;
                        else return -1;
                    } else {
                        if (st2 == null) return 1;
                        else {
                            String[] str1 = st1.name.split(" ");
                            String[] str2 = st2.name.split(" ");
                            String lastName1 = str1[str1.length - 1];
                            String lastName2 = str2[str2.length - 1];
                            return lastName1.compareTo(lastName2);
                        }
                    }
                }
                else return (int) (10 * ((o2.score - o1.score)));
            });
        }
        diemThiAdapter.updateList(list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Xem lại");

        rb_ViewAll = findViewById(R.id.rb_ViewAll);
        rb_ByABC = findViewById(R.id.rb_ByABC);
        rb_ByScore = findViewById(R.id.rb_ByScore);

        rb_ViewAll.setOnClickListener(v -> {
            updateView();
        });
        rb_ByABC.setOnClickListener(v -> {
            updateView();
        });
        rb_ByScore.setOnClickListener(v -> {
            updateView();
        });

        //lấy vị trí của bài thi trong intent gửi đến
        examId = getIntent().getIntExtra(DatabaseManager.ARG_P_BAI_THI, 0);
        examination = DatabaseManager.getExamination(examId);
        //init
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<ExamResult> list = new ArrayList<>();
        for (int j = 0; j < DatabaseManager.getExamResults().size(); j++) {
            ExamResult examResult = DatabaseManager.getExamResults().get(j);
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
        getMenuInflater().inflate(R.menu.export, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_export:
                String pattern = "yyMMdd_HHmmss"; // + examination.name
                String fileName = "DiemThi_"  + examination.name + "_" + android.text.format.DateFormat.format(pattern, new java.util.Date()) + ".xlsx";
//                fileName = "myfile.xlsx";
//                File folder = Environment.getExternalStorageDirectory();
//                if (!folder.exists())
//                    folder.mkdirs();
//                String totalPath = folder.getAbsolutePath() + "/" + fileName;
                boolean ret = ExcelUtil.writeExcel(this, fileName, diemThiAdapter.ds);
                if (ret){
                    Toast.makeText(this, "Xuất file thành công tại đường dẫn " + fileName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Xuất file thất bại", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
