package com.lanbitou.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lanbitou.R;
import com.lanbitou.fragments.AllNotesFragment;
import com.lanbitou.fragments.BillFragment;
import com.lanbitou.fragments.NewestNotesFragment;
import com.lanbitou.fragments.PaintDisplayFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    //这是右下的小按钮
    FloatingActionButton fab;

    private SharedPreferences preferences;
    public final static int ADD_PAINT_REQUEST_CODE = 10;
    private final static int ADD_NOTE_REQUEST_CODE = 20;

    private Context context = this;
    private int nowAt = 0;          //现在在哪一个Fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //如果没uid就进入登陆
        preferences = getSharedPreferences("lanbitou", MODE_PRIVATE);
        int uid = preferences.getInt("uid", 0);
        if (uid == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragments = new Fragment[4];

        fragments[0] = new NewestNotesFragment();
        fragments[1] = new AllNotesFragment();
        fragments[2] = new BillFragment();
        fragments[3] = new PaintDisplayFragment();

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frag_content,fragments[0]).show(fragments[0]);
        fragmentTransaction.commit();


        //添加按钮
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowAt == 3){
                    Intent i = new Intent(context,PaintActivity.class);
                    startActivityForResult(i,ADD_PAINT_REQUEST_CODE);
                } else {
                    String[] items = { "添加笔记", "添加账单" };
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    switch (which) {
                                        case 0:
                                            Intent intent = new Intent(context, NoteShowActivity.class);
                                            intent.putExtra("isNew", true);
                                            startActivityForResult(intent, 1);
                                            break;
                                        case 1:

                                            break;
                                        default:
                                            break;
                                    }
                                }

                            });

                    builder.create().show();
                }
            }
        });

        //控制滑动动作
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //得到侧边栏的头部
        View navHeaderView = navigationView.getHeaderView(0);

        View navHeader = navHeaderView.findViewById(R.id.nav_header);

        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,AccountActivity.class);
                startActivity(i);
            }
        });

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 右边三个点的菜单点击时间
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity_show_signal_paint in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 侧边栏的点击事件
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.frag_content,fragments[0]).show(fragments[0]);


        if (id == R.id.nav_newest_notes) {
            fragmentTransaction.replace(R.id.frag_content,fragments[0]);
            nowAt = 0;
            // Handle the camera action
        } else if (id == R.id.nav_all_notes) {
            fragmentTransaction.replace(R.id.frag_content,fragments[1]);
            nowAt = 1;
        } else if (id == R.id.nav_bill) {
            fragmentTransaction.replace(R.id.frag_content,fragments[2]);
            nowAt = 2;
        } else if (id == R.id.nav_paint) {
            fragmentTransaction.replace(R.id.frag_content,fragments[3]);
            nowAt = 3;
        } else if (id == R.id.nav_setting) {
            Intent i = new Intent(this,SettingActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_last_synch_date) {

        }

        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        fragments[0].onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_PAINT_REQUEST_CODE){
            fragments[3].onActivityResult(requestCode,resultCode,data);
        }


    }

}
