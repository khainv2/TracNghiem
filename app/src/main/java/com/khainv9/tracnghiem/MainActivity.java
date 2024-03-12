package com.khainv9.tracnghiem;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.khainv9.tracnghiem.adapter.BaiThiAdapter;
import com.khainv9.tracnghiem.adapter.DiemThiAdapter;
import com.khainv9.tracnghiem.adapter.HocSinhAdapter;
import com.khainv9.tracnghiem.app.DatabaseManager;
import com.khainv9.tracnghiem.app.ExcelUtil;
import com.khainv9.tracnghiem.app.FileUtil;
import com.khainv9.tracnghiem.models.Examination;
import com.khainv9.tracnghiem.models.Student;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    RecyclerView rv;
    RecyclerView.Adapter adapter;
    FloatingActionButton fab;
    NavigationView navigationView;
    AlertDialog taoBaiThi, themHocSinh;
    Toolbar toolbar;
    int idSelected;

    private static final int FILE_SELECT_CODE = 1331;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Bài thi");
        setSupportActionBar(toolbar);

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);// Đặt item chọn mặc định
        onNavigationItemSelected(navigationView.getMenu().getItem(0));
        createWindowThemBaiThi();
        createWindowThemHocSinh();
    }

    private void createWindowSuaBaiThi(final Examination examination) {
        View v = getLayoutInflater().inflate(R.layout.screen_bai_moi, null);
        final EditText tvTenBai = v.findViewById(R.id.ed_bai),
                edPhan1 = v.findViewById(R.id.ed_p1),
                edPhan2 = v.findViewById(R.id.ed_p2),
                edPhan3 = v.findViewById(R.id.ed_p3),
                edTotal = v.findViewById(R.id.ed_number_total);
        final CheckBox cbShortMath = v.findViewById(R.id.cb_SortMath);
        edPhan1.setText(examination.chapterACount + "");
        edPhan2.setText(examination.chapterBCount + "");
        edPhan3.setText(examination.chapterCCount + "");
        cbShortMath.setChecked(examination.isMathSubject);
        syncDiemBaiThi(edPhan1, edPhan2, edPhan3, edTotal, cbShortMath);
        tvTenBai.setText(examination.name);
        new AlertDialog.Builder(this)
                .setTitle("Sửa bài thi")
                .setView(v)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (!verifyBaiThiInfo(tvTenBai.getText().toString(), edPhan1.getText().toString(),
                            edPhan2.getText().toString(), edPhan3.getText().toString())){
                        return;
                    }
                    String sTen = tvTenBai.getText().toString();
                    int p1 = Integer.parseInt(edPhan1.getText().toString());
                    int p2 = Integer.parseInt(edPhan2.getText().toString());
                    int p3 = Integer.parseInt(edPhan3.getText().toString());
                    examination.name = sTen;
                    examination.chapterACount = p1;
                    examination.chapterBCount = p2;
                    examination.chapterCCount = p3;
                    examination.isMathSubject = cbShortMath.isChecked();
                    DatabaseManager.update(examination);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Xóa", (dialog, which) -> {
                    DatabaseManager.delete(examination);
                    adapter.notifyDataSetChanged();
                })
                .create().show();
    }

    private void createWindowSuaHocSinh(final Student student) {
        View v2 = getLayoutInflater().inflate(R.layout.screen_hoc_sinh_moi, null);
        final EditText tvTenHS = v2.findViewById(R.id.ed_bai),
                tvSBD = v2.findViewById(R.id.ed_he_diem),
                tvLop = v2.findViewById(R.id.ed_lop);
        tvTenHS.setText(student.name);
        tvSBD.setText(student.id);
        tvLop.setText(student.iclass);
        new AlertDialog.Builder(this)
                .setTitle("Sửa học sinh")
                .setView(v2)
                .setPositiveButton("OK", (dialog, which) -> {
                    String tenHS = tvTenHS.getText().toString();
                    String sbd = tvSBD.getText().toString();
                    String lop = tvLop.getText().toString();
                    student.id = sbd;
                    student.name = tenHS;
                    student.iclass = lop;
                    if (tenHS.isEmpty()){
                        Toast.makeText(MainActivity.this, "Tên học sinh không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sbd.isEmpty()){
                        Toast.makeText(MainActivity.this, "SBD không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DatabaseManager.update(student);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Xóa", (dialog, which) -> {
                    DatabaseManager.delete(student);
                    adapter.notifyDataSetChanged();
                })
                .create().show();
    }

    private void createWindowThemHocSinh() {
        View v2 = getLayoutInflater().inflate(R.layout.screen_hoc_sinh_moi, null);
        final EditText tvTenHS = v2.findViewById(R.id.ed_bai),
                tvSBD = v2.findViewById(R.id.ed_he_diem),
                tvLop = v2.findViewById(R.id.ed_lop);

        themHocSinh = new AlertDialog.Builder(this)
                .setTitle("Thêm học sinh")
                .setView(v2)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tenHS = tvTenHS.getText().toString();
                        String sbd = tvSBD.getText().toString();
                        String lop = tvLop.getText().toString();
                        if (tenHS.isEmpty()){
                            Toast.makeText(MainActivity.this, "Tên học sinh không được để trống", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (sbd.isEmpty()){
                            Toast.makeText(MainActivity.this, "SBD không được để trống", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Student student = new Student(sbd, tenHS, lop);
                        DatabaseManager.update(student);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }

    private String getTotal(String p1, String p2, String p3, boolean isShortMath) {
        try {
            int s1 = Integer.parseInt(p1);
            int s2 = Integer.parseInt(p2);
            int s3 = Integer.parseInt(p3);
            if (isShortMath){
                return (s1 * 0.25) + s2 + (s3 * 0.5) + "";
            } else {
                return (s1 * 0.25) + s2 + (s3 * 0.25) + "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private boolean verifyBaiThiInfo(String tenBai, String p1, String p2, String p3){
        if (tenBai.isEmpty()) {
            Toast.makeText(MainActivity.this, "Tên bài không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int s1 = Integer.parseInt(p1);
            int s2 = Integer.parseInt(p2);
            int s3 = Integer.parseInt(p3);
            if (s1 > 40) {
                Toast.makeText(MainActivity.this, "Số câu phần 1 không được lớn hơn 40", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (s2 > 8){
                Toast.makeText(MainActivity.this, "Số câu phần 2 không được lớn hơn 8", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (s3 > 6){
                Toast.makeText(MainActivity.this, "Số câu phần 3 không được lớn hơn 6", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Số điểm không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    void syncDiemBaiThi(EditText edPhan1, EditText edPhan2, EditText edPhan3, EditText edTotal, CheckBox cbShortMath){

        // On edit text change share for 3 edit text
        edPhan1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                edTotal.setText(getTotal(edPhan1.getText().toString(),
                        edPhan2.getText().toString(), edPhan3.getText().toString(), cbShortMath.isChecked()));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
        edPhan2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                edTotal.setText(getTotal(edPhan1.getText().toString(),
                        edPhan2.getText().toString(), edPhan3.getText().toString(), cbShortMath.isChecked()));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
        edPhan3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                edTotal.setText(getTotal(edPhan1.getText().toString(),
                        edPhan2.getText().toString(), edPhan3.getText().toString(), cbShortMath.isChecked()));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
        cbShortMath.setOnClickListener(v1 -> {
            edTotal.setText(getTotal(edPhan1.getText().toString(),
                    edPhan2.getText().toString(), edPhan3.getText().toString(), cbShortMath.isChecked()));
        });
    }

    private void createWindowThemBaiThi() {
        View v = getLayoutInflater().inflate(R.layout.screen_bai_moi, null);
        final EditText tvTenBai = v.findViewById(R.id.ed_bai),
                edPhan1 = v.findViewById(R.id.ed_p1),
                edPhan2 = v.findViewById(R.id.ed_p2),
                edPhan3 = v.findViewById(R.id.ed_p3),
                edTotal = v.findViewById(R.id.ed_number_total);
        final CheckBox cbShortMath = v.findViewById(R.id.cb_SortMath);

        syncDiemBaiThi(edPhan1, edPhan2, edPhan3, edTotal, cbShortMath);
        taoBaiThi = new AlertDialog.Builder(this)
                .setTitle("Thêm bài mới")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!verifyBaiThiInfo(tvTenBai.getText().toString(), edPhan1.getText().toString(),
                                edPhan2.getText().toString(), edPhan3.getText().toString())){
                            return;
                        }
                        int p1 = Integer.parseInt(edPhan1.getText().toString());
                        int p2 = Integer.parseInt(edPhan2.getText().toString());
                        int p3 = Integer.parseInt(edPhan3.getText().toString());
                        boolean isShortMath = cbShortMath.isChecked();
                        String sTen = tvTenBai.getText().toString();
                        Examination examination = new Examination(sTen, p1, p2, p3, isShortMath);
                        DatabaseManager.update(examination);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_detete) {
            switch (idSelected) {
                case R.id.nav_bai_thi:
                    DatabaseManager.deleteAllExaminations();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.nav_hoc_sinh:
                    DatabaseManager.deleteAllStudents();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.nav_xem_diem:
                    break;
                case R.id.nav_send:
                    break;
                case R.id.nav_share:
                    break;
            }
            return true;
        }
        if (id == R.id.action_import){
            switch (idSelected) {
                case R.id.nav_hoc_sinh:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try {
                        startActivityForResult(
                                Intent.createChooser(intent, "Select a File to Upload"),
                                FILE_SELECT_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        // Potentially direct the user to the Market with a Dialog
                        Toast.makeText(this, "Please install a File Manager.",
                                Toast.LENGTH_SHORT).show();
                    }


                    adapter.notifyDataSetChanged();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("MyLog", "File Uri: " + uri.toString());
                    // Get the path
                    String path = FileUtil.getFileAbsolutePath(this, uri);
                    Log.d("MyLog", "File Path: " + path);
                    // Read excel using apache POI
                    ExcelUtil.Result ret = ExcelUtil.readExcel(path);
                    if (ret.success){
                        for (Student student : ret.students){
                            DatabaseManager.update(student);
                        }
                        Toast.makeText(this, "Nhập dữ liệu học sinh thành công", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Lỗi đọc file", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem itemImport = menu.findItem(R.id.action_import);
        itemImport.setVisible(idSelected == R.id.nav_hoc_sinh);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        idSelected = item.getItemId();
        if (idSelected == R.id.nav_bai_thi || idSelected == R.id.nav_hoc_sinh) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        switch (idSelected) {
            case R.id.nav_bai_thi:
                adapter = new BaiThiAdapter(DatabaseManager.getExaminations(), v -> {
                    createWindowSuaBaiThi(DatabaseManager.getExamination(v.getId()));
                    return true;
                });
                rv.setAdapter(adapter);
                toolbar.setTitle("Bài thi");
                break;
            case R.id.nav_hoc_sinh:
                adapter = new HocSinhAdapter(DatabaseManager.students, v -> {
                    createWindowSuaHocSinh(DatabaseManager.students.get(v.getId()));
                    return true;
                });
                rv.setAdapter(adapter);
                toolbar.setTitle("Học sinh");
                break;
            case R.id.nav_xem_diem:
                adapter = new DiemThiAdapter(DatabaseManager.getExamResults());
                rv.setAdapter(adapter);
                toolbar.setTitle("Xem điểm");
                break;
            case R.id.nav_send:
                break;
            case R.id.nav_share:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (idSelected) {
            case R.id.nav_bai_thi:
                taoBaiThi.show();
                break;
            case R.id.nav_hoc_sinh:
                themHocSinh.show();
                break;
        }
    }
}
