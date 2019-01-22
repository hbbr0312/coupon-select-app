package com.example.user.proj4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean login=false;
    public static boolean ismanager = false;

    public static String storename="";
    public static String userid;
    public static String name;
    public static String phone;

    public static Session session;
    private HashMap<String,String> info;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private View headerView;
    TextView tname;
    TextView tid;
    TextView tphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:coupon정보 다시 로드
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //왼쪽 메뉴 탭
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        tname = headerView.findViewById(R.id.name);
        tid = headerView.findViewById(R.id.userid);
        tphone = headerView.findViewById(R.id.phone);

        session = new Session(MainActivity.this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        invalidateOptionsMenu();
        Log.e("main","onstart");
        Log.e("main public login",""+login);
        Log.e("main public ismanager",""+ismanager);

        //처음 login 했을때 //TODO:user정보를 nav_header_main에 setText(), login menu사라지고 logout?
        info = session.getInfo();
        updateuserinfo();
        if(login){
            if(ismanager){
                //manager일때 //TODO:관리자 탭보이도록 일반유저면 안보이게
            }
        }


    }

    //login하거나 logout할때 userid,phone,name 재설정해주고, 왼쪽 메뉴탭에도 적용
    public void updateuserinfo(){
        getsession();
        Log.e("updating","...");
        if(login){
            Log.e("updating","현재상태 로그인");
            tname.setText(name+" 고객님");
            tid.setText("ID : "+userid);
            tphone.setText("Phone number : "+phone);
            couponsFragment cf = new couponsFragment();
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentA,cf);
            fragmentTransaction.commit();
        }else{
            Log.e("updating","현재상태 로그아웃");
            tname.setText("로그인이 필요합니다.");
            tid.setText("");
            tphone.setText("");
            LoginFragment lf = new LoginFragment();
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentA,lf);
            fragmentTransaction.commit();
        }
    }

    //session에서 준 정보를 가지고 login했는지 보고 login값 재설정,유저정보 업데이트
    public void getsession(){
        userid = info.get("id");
        if(userid.length()>0){ //login session maintain
            login=true;
        }
        phone = info.get("phone");
        name = info.get("name");
        if(info.get("ismanager").equals("true")) {
            ismanager=true;
            storename = info.get("storename");
        }

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
        MenuItem register = menu.findItem(R.id.action_settings);
        if(!login){
            register.setVisible(false);
            Log.e("register","setVisible false");
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        /*
        MenuItem register = menu.findItem(R.id.nav_manage);
        if(isManager)
        {
            register.setVisible(true);
            Log.e("register","setVisible false");
        }
        else
        {
            register.setVisible(false);
            Log.e("register","setVisible false");
        }
        */
        return true;
    }

    public boolean permission(boolean needmanage){
        Log.e("permission","login : "+login);
        if(login) {
            if(needmanage) {
                if(!ismanager) Toast.makeText(MainActivity.this,"가게 매니저 권한이 필요합니다.",Toast.LENGTH_SHORT).show();
                return ismanager;
            }
            else return true;

        }
        else {
            Toast.makeText(MainActivity.this,"login이 필요합니다.",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(login){ //logout할거임
                Toast.makeText(MainActivity.this,"logout되었습니다",Toast.LENGTH_SHORT).show();
                session.logout();
                updateuserinfo();
            }
            login=!login;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_login){
            /*
            Intent intent1 = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent1);*/
        }
        else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            if(permission(false)){

                /*
                Intent intent0 = new Intent(MainActivity.this,couponsActivity.class);
                startActivity(intent0);*/
            }
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            if(permission(true)){
                PostcouponFragment lf = new PostcouponFragment();
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentA,lf);
                fragmentTransaction.commit();
                /*
                Intent intent = new Intent(MainActivity.this,PostcouponActivity.class);
                startActivity(intent);
                */
            }
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            if(true){ //permission(true)
                Intent intent2 = new Intent(MainActivity.this,couponsettingActivity.class);
                startActivity(intent2);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

