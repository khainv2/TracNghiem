package com.khainv9.tracnghiem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.khainv9.tracnghiem.adapter.DiemThiAdapter;
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.models.ExamResult;
import com.khainv9.tracnghiem.models.Examination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticActivity extends AppCompatActivity {

    int examId;
    Toolbar toolbar;
    Examination examination;
    BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //cài đặt nút back home
        getSupportActionBar().setTitle("Thống kê");

        //lấy vị trí của bài thi trong intent gửi đến
        examId = getIntent().getIntExtra(DatabaseManager.ARG_P_BAI_THI, 0);
        examination = DatabaseManager.getExamination(examId);

        chart = findViewById(R.id.chart1);
        chart.setDrawGridBackground(false);

        TextView tv = findViewById(R.id.tv);

        List<ExamResult> examResults = DatabaseManager.getExamResults();

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



//        tv.setText();
//        tv.

        // Find max
        double max = 0, min = 99999;
        double average = 0;
        for (ExamResult result: map.values()){
            if (result.score > max) max = result.score;
            if (result.score < min) min = result.score;
            average += result.score;
        }
        if (map.size() == 0) max = min = average = 0;
        else average /= map.size();


        tv.setText("Điểm cao nhất: " + max + "\nĐiểm thấp nhất: " + min + "\nĐiểm trung bình: " + average + "\nSố lượng: " + map.size() + "\n");

        if (max < 10){
            max = 10;
        }

        List<ExamResult> list = new ArrayList<>(map.values());
        int[] histogram = new int[(int) Math.ceil(max) + 1];
        for (ExamResult result: list){
            histogram[(int) Math.floor(result.score)]++;
        }


        ArrayList<BarEntry> values = new ArrayList<>();

        for (float i = 0.5f; i < 10.1f; i += 1f) {
            values.add(new BarEntry(i, histogram[(int) Math.floor(i)]));
        }

        BarDataSet set1;

        set1 = new BarDataSet(values, "Phổ điểm");

        set1.setDrawIcons(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.6f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        chart.setData(data);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setLabelCount(11, true);
        chart.getDescription().setEnabled(false);

        // Add spacing between bar
        chart.setFitBars(false);
//        chart.disableScroll();
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