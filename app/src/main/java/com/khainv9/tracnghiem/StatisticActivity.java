package com.khainv9.tracnghiem;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.utils.MPPointF;
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
    PieChart chart;
    public static final int[] COLORFUL_COLORS = {
            Color.rgb(193, 37, 82),
            Color.rgb(255, 102, 0),
            Color.rgb(245, 199, 0),
            Color.rgb(106, 150, 31),
            Color.rgb(179, 100, 53),
            Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(254, 247, 120),
            Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)
    };

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
//        chart.setDrawGridBackground(false);

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


        // Find min, max, avr
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

        List<Range<Double>> ranges = new ArrayList<>();
        ranges.add(new Range<>(0.0, 1.0));
        ranges.add(new Range<>(1.25, 3.25));
        ranges.add(new Range<>(3.5, 4.75));
        ranges.add(new Range<>(5.0, 6.75));
        ranges.add(new Range<>(7.0, 8.0));
        ranges.add(new Range<>(8.25, 10.0));

        List<Integer> countPerRange = new ArrayList<>();
        for (int i = 0; i < ranges.size(); i++) {
            countPerRange.add(0);
        }

        for (ExamResult result: map.values()){
            for (int i = 0; i < ranges.size(); i++) {
                if (ranges.get(i).contains(result.score)){
                    countPerRange.set(i, countPerRange.get(i) + 1);
                    break;
                }
            }
        }

//        // Create fake data for all ranges
//        countPerRange.set(0, 10);
//        countPerRange.set(1, 5);
//        countPerRange.set(2, 20);
//        countPerRange.set(3, 7);
//        countPerRange.set(4, 7);
//        countPerRange.set(5, 6);



        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < ranges.size(); i++) {
            int count = countPerRange.get(i);
            if (count == 0)
                continue;
            PieEntry entry = new PieEntry(countPerRange.get(i));
            entry.setLabel("Điểm " + ranges.get(i).getLower() + " - " + ranges.get(i).getUpper());

            values.add(entry);
        }

        PieDataSet set1;

        set1 = new PieDataSet(values, "Phổ điểm");
        set1.setColors(COLORFUL_COLORS);
        set1.setDrawIcons(false);

        PieData data = new PieData(set1);
        data.setValueTextSize(10f);

        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
//                return "" + (int) value + " (" + String.format("%.2f", value / 55 * 100) + "%)";
                return "" + (int) value + " (" + String.format("%.2f", value / map.size() * 100) + "%)";
            }
        });
        chart.setData(data);
        chart.getDescription().setEnabled(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        // Add spacing between bar
//        chart.setFitBars(false);
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