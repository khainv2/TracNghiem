package com.khainv9.tracnghiem;

import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.khainv9.tracnghiem.adapter.BaiThiAdapter;
import com.khainv9.tracnghiem.adapter.DiemThiAdapter;
import com.khainv9.tracnghiem.adapter.HocSinhAdapter;
import com.khainv9.tracnghiem.app.Utils;
import com.khainv9.tracnghiem.models.BaiThi;
import com.khainv9.tracnghiem.models.HocSinh;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    RecyclerView rv;
    RecyclerView.Adapter adapter;
    FloatingActionButton fab;
    NavigationView navigationView;
    AlertDialog taoBaiThi, themHocSinh;
    int idSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chấm trắc nghiệm");
        Utils.init(this);

        //init
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


    private void createWindowSuaBaiThi(final BaiThi baiThi) {
        View v = getLayoutInflater().inflate(R.layout.screen_bai_moi, null);
        final EditText tvTenBai = v.findViewById(R.id.ed_bai),
                edPhan1 = v.findViewById(R.id.ed_p1),
                edPhan2 = v.findViewById(R.id.ed_p2),
                edPhan3 = v.findViewById(R.id.ed_p3),
                edTotal = v.findViewById(R.id.ed_number_total);
        syncDiemBaiThi(edPhan1, edPhan2, edPhan3, edTotal);
        tvTenBai.setText(baiThi.tenBaiThi);
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
                    baiThi.tenBaiThi = sTen;
                    baiThi.soCauPhan1 = p1;
                    baiThi.soCauPhan2 = p2;
                    baiThi.soCauPhan3 = p3;
                    Utils.update(baiThi);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Xóa", (dialog, which) -> {
                    Utils.delete(baiThi);
                    adapter.notifyDataSetChanged();
                })
                .create().show();
    }

    private void createWindowSuaHocSinh(final HocSinh hocSinh) {
        View v2 = getLayoutInflater().inflate(R.layout.screen_hoc_sinh_moi, null);
        final EditText tvTenHS = v2.findViewById(R.id.ed_bai),
                tvSBD = v2.findViewById(R.id.ed_he_diem),
                tvLop = v2.findViewById(R.id.ed_lop);
        tvTenHS.setText(hocSinh.name);
        tvSBD.setText(hocSinh.sbd);
        tvLop.setText(hocSinh.class1);
        new AlertDialog.Builder(this)
                .setTitle("Sửa học sinh")
                .setView(v2)
                .setPositiveButton("OK", (dialog, which) -> {
                    String tenHS = tvTenHS.getText().toString();
                    String sbd = tvSBD.getText().toString();
                    String lop = tvLop.getText().toString();
                    hocSinh.sbd = sbd;
                    hocSinh.name = tenHS;
                    hocSinh.class1 = lop;
                    if (tenHS.isEmpty()){
                        Toast.makeText(MainActivity.this, "Tên học sinh không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sbd.isEmpty()){
                        Toast.makeText(MainActivity.this, "SBD không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Utils.update(hocSinh);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Xóa", (dialog, which) -> {
                    Utils.delete(hocSinh);
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
                        HocSinh hocSinh = new HocSinh(sbd, tenHS, lop);
                        Utils.update(hocSinh);
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

    private String getTotal(String p1, String p2, String p3) {
        try {
            int s1 = Integer.parseInt(p1);
            int s2 = Integer.parseInt(p2);
            int s3 = Integer.parseInt(p3);
            return (s1 * 0.25) + s2 + (s3 * 0.25) + "";
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

    void syncDiemBaiThi(EditText edPhan1, EditText edPhan2, EditText edPhan3, EditText edTotal){

        // On edit text change share for 3 edit text
        edPhan1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                edTotal.setText(getTotal(edPhan1.getText().toString(),
                        edPhan2.getText().toString(), edPhan3.getText().toString()));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
        edPhan2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                edTotal.setText(getTotal(edPhan1.getText().toString(),
                        edPhan2.getText().toString(), edPhan3.getText().toString()));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
        edPhan3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                edTotal.setText(getTotal(edPhan1.getText().toString(),
                        edPhan2.getText().toString(), edPhan3.getText().toString()));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
    }

    private void createWindowThemBaiThi() {

        View v = getLayoutInflater().inflate(R.layout.screen_bai_moi, null);
        final EditText tvTenBai = v.findViewById(R.id.ed_bai),
                edPhan1 = v.findViewById(R.id.ed_p1),
                edPhan2 = v.findViewById(R.id.ed_p2),
                edPhan3 = v.findViewById(R.id.ed_p3),
                edTotal = v.findViewById(R.id.ed_number_total);
        syncDiemBaiThi(edPhan1, edPhan2, edPhan3, edTotal);
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
                        String sTen = tvTenBai.getText().toString();
                        BaiThi baiThi = new BaiThi(sTen, p1, p2, p3);
                        Utils.update(baiThi);
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
                    Utils.deleteAllBT();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.nav_hoc_sinh:
                    Utils.deleteAllHS();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.nav_xem_diem:
                    break;
                case R.id.nav_giay_thi:
                    break;
                case R.id.nav_send:
                    break;
                case R.id.nav_share:
                    break;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
                adapter = new BaiThiAdapter(Utils.dsBaiThi, v -> {
                    createWindowSuaBaiThi(Utils.dsBaiThi.get(v.getId()));
                    return true;
                });
                rv.setAdapter(adapter);
                break;
            case R.id.nav_hoc_sinh:
                adapter = new HocSinhAdapter(Utils.dsHocSinh, v -> {
                    createWindowSuaHocSinh(Utils.dsHocSinh.get(v.getId()));
                    return true;
                });
                rv.setAdapter(adapter);
                break;
            case R.id.nav_xem_diem:
                adapter = new DiemThiAdapter(Utils.dsDiemThi);
                rv.setAdapter(adapter);
                break;
            case R.id.nav_giay_thi:
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tnmaker.wordpress.com/"));
//                startActivity(browserIntent);
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
