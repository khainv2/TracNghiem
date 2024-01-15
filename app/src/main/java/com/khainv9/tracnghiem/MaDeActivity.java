package com.khainv9.tracnghiem;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.fragment.DapAnFragment;
import com.khainv9.tracnghiem.fragment.MaDeFragment;
import com.khainv9.tracnghiem.fragment.SSPAdapter;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.QuestionPaper;


public class MaDeActivity extends AppCompatActivity implements View.OnClickListener, TabLayout.OnTabSelectedListener {

    public static final int THEM_MOI = -1;

    int iBT;
    int iDT;
    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    MaDeFragment sMaDe;
    DapAnFragment sDapAn;

    public Examination examination;
    public QuestionPaper questionPaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma_de);
        toolbar = (Toolbar) findViewById(R.id.ct_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mã đề");

        //lấy vị trí của bài thi trong intent gửi đến
        iBT = getIntent().getIntExtra(Utils.ARG_P_BAI_THI, 0);
        examination = Utils.dsExamination.get(iBT);
        if (examination == null){
            Log.e("MaDeActivity", "onCreate: baiThi null");
            finish();
        } else {
            Log.d("MaDeActivity", "onCreate: baiThi " + examination.questionPapers.size());
        }

        iDT = getIntent().getIntExtra(Utils.ARG_P_DE_THI, THEM_MOI);
        if (iDT == THEM_MOI) {
            iDT = examination.questionPapers.size();
            questionPaper = new QuestionPaper(examination.chapterACount, examination.chapterBCount, examination.chapterCCount);
            examination.questionPapers.add(questionPaper);
        } else {
            questionPaper = examination.questionPapers.get(iDT);
        }

        Log.d("MaDeActivity", "onCreate: iBT = " + iBT + ", iDT = " + iDT + ", deThi " + questionPaper);
        for (int i = 0; i < examination.questionPapers.size(); i++) {
            Log.d("MaDeActivity", "onCreate: " + examination.questionPapers.get(i));
        }

        viewPager = findViewById(R.id.vp);
        tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.addTab(tabLayout.newTab().setText("MÃ ĐỀ"));
        tabLayout.addTab(tabLayout.newTab().setText("ĐÁP ÁN"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        Fragment[] fragments = new Fragment[]{
                sMaDe = MaDeFragment.create(questionPaper.maDeThi),
                sDapAn = DapAnFragment.create(questionPaper.dapAn, questionPaper.soCauPhan1, questionPaper.soCauPhan2, questionPaper.soCauPhan3 )
        };

        SSPAdapter adapter = new SSPAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        questionPaper.maDeThi = sMaDe.getMaDe();
        questionPaper.dapAn = sDapAn.getListDapAn();
        Utils.update(examination);
        Log.d("MaDeActivity", "onBackPressed: " + questionPaper.maDeThi + ", " + questionPaper.dapAn);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
