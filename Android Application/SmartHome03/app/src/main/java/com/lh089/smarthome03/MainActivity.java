package com.lh089.smarthome03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private final int Fragment1 = 1;
    private final int Fragment2 = 2;
    private final int Fragment3 = 3;
    private final int Fragment4 = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentView(Fragment1);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        // 위치권한 요청
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list,  1);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                /*switch (item.getItemId()) {
                    case R.id.item1:
                        //Toast.makeText(getApplicationContext(), "메뉴아이템 1 선택", Toast.LENGTH_SHORT).show();
                        fragmentTransaction.replace(R.id.main_frame, fragment1);
                    case R.id.item2:
                        fragmentTransaction.replace(R.id.main_frame, fragment2);
                    case R.id.item3:
                        fragmentTransaction.replace(R.id.main_frame, fragment3);
                }
                fragmentTransaction.commit();*/

                if (item.getItemId() == R.id.item1) {
                    fragmentView(Fragment1);
                }else if (item.getItemId() == R.id.item2) {
                    fragmentView(Fragment2);
                } else if (item.getItemId() == R.id.item3) {
                    fragmentView(Fragment3);
                } else if (item.getItemId() == R.id.item4) {
                    fragmentView(Fragment4);
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void fragmentView(int fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (fragment) {
            case 1:
                Fragment1 fragment1 = new Fragment1();
                transaction.replace(R.id.main_frame, fragment1);
                transaction.commit();
                break;
            case 2:
                Fragment2 fragment2 = new Fragment2();
                transaction.replace(R.id.main_frame, fragment2);
                transaction.commit();
                break;
            case 3:
                Fragment3 fragment3 = new Fragment3();
                transaction.replace(R.id.main_frame, fragment3);
                transaction.commit();
                break;
            case 4:
                Fragment4 fragment4 = new Fragment4();
                transaction.replace(R.id.main_frame, fragment4);
                transaction.commit();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}