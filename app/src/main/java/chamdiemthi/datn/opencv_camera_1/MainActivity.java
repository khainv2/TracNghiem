package chamdiemthi.datn.opencv_camera_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rantea.opencv_camera_1.R;

import chamdiemthi.datn.opencv_camera_1.adapter.BaiThiAdapter;
import chamdiemthi.datn.opencv_camera_1.adapter.DiemThiAdapter;
import chamdiemthi.datn.opencv_camera_1.adapter.HocSinhAdapter;
import chamdiemthi.datn.opencv_camera_1.app.Utils;
import chamdiemthi.datn.opencv_camera_1.models.BaiThi;
import chamdiemthi.datn.opencv_camera_1.models.HocSinh;

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
                tvSoCau = v.findViewById(R.id.ed_so_cau),
                tvHeDiem = v.findViewById(R.id.ed_he_diem);
        tvTenBai.setText(baiThi.tenBaiThi);
        tvHeDiem.setText(baiThi.heDiem + "");
        tvSoCau.setText(baiThi.soCau + "");
        new AlertDialog.Builder(this)
                .setTitle("Sửa bài thi")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sTen = tvTenBai.getText().toString();
                        int soCau = Integer.parseInt(tvSoCau.getText().toString());
                        int heDiem = Integer.parseInt(tvHeDiem.getText().toString());
                        if (soCau != 20 && soCau != 40 && soCau != 60 && soCau != 80 && soCau != 100) {
                            Toast.makeText(MainActivity.this, "Số câu phải bằng 20/40/60/80/100", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        baiThi.tenBaiThi = sTen;
                        baiThi.heDiem = heDiem;
                        baiThi.soCau = soCau;
                        Utils.update(baiThi);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.delete(baiThi);
                        adapter.notifyDataSetChanged();
                    }
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tenHS = tvTenHS.getText().toString();
                        String sbd = tvSBD.getText().toString();
                        String lop = tvLop.getText().toString();
                        hocSinh.sbd = sbd;
                        hocSinh.name = tenHS;
                        hocSinh.class1 = lop;
                        Utils.update(hocSinh);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.delete(hocSinh);
                        adapter.notifyDataSetChanged();
                    }
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

    private void createWindowThemBaiThi() {
        View v = getLayoutInflater().inflate(R.layout.screen_bai_moi, null);
        final EditText tvTenBai = v.findViewById(R.id.ed_bai),
                tvSoCau = v.findViewById(R.id.ed_so_cau),
                tvHeDiem = v.findViewById(R.id.ed_he_diem);
        taoBaiThi = new AlertDialog.Builder(this)
                .setTitle("Thêm bài mới")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sTen = tvTenBai.getText().toString();
                        int soCau = Integer.parseInt(tvSoCau.getText().toString());
                        int heDiem = Integer.parseInt(tvHeDiem.getText().toString());
                        if (soCau != 20 && soCau != 40 && soCau != 60 && soCau != 80 && soCau != 100) {
                            Toast.makeText(MainActivity.this, "Số câu phải bằng 20/40/60/80/100", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        BaiThi baiThi = new BaiThi(sTen, soCau, heDiem);
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
                adapter = new BaiThiAdapter(Utils.dsBaiThi, new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        createWindowSuaBaiThi(Utils.dsBaiThi.get(v.getId()));
                        return true;
                    }
                });
                rv.setAdapter(adapter);
                break;
            case R.id.nav_hoc_sinh:
                adapter = new HocSinhAdapter(Utils.dsHocSinh, new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        createWindowSuaHocSinh(Utils.dsHocSinh.get(v.getId()));
                        return true;
                    }
                });
                rv.setAdapter(adapter);
                break;
            case R.id.nav_xem_diem:
                adapter = new DiemThiAdapter(Utils.dsDiemThi);
                rv.setAdapter(adapter);
                break;
            case R.id.nav_giay_thi:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tnmaker.wordpress.com/"));
                startActivity(browserIntent);
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
