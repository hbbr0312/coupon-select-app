package com.example.user.proj4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean login=false;
    public static boolean ismanager = false;
    public static String userid;
    public static String name;
    public static String phone;
    private ImageView qrcode;

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        ListView lv = (ListView) findViewById(R.id.ListView);



    }

    @Override
    protected void onStart(){
        super.onStart();
        invalidateOptionsMenu();
        Log.e("main","onstart");
        Log.e("main public login",""+login);
        Log.e("main public ismanager",""+ismanager);
        //login 했을때 //TODO:user정보를 nav_header_main에 setText(), login menu사라지고 logout? , user id로 qrcode생성
        if(login){
            qrcode = (ImageView) findViewById(R.id.qrcode);
            Bitmap bitmap = generateQRCode("test1"); //userid로 바꾸기
            qrcode.setImageBitmap(bitmap);
        }
        //manager일때 //TODO:관리자 탭보이도록 일반유저면 안보이게
        if(ismanager){

        }

        //listview
    }
    public static Bitmap generateQRCode(String contents) {
        Bitmap bitmap = null;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            bitmap = toBitmap(qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, 200, 200));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static Bitmap toBitmap(BitMatrix matrix){
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
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
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu); //원래 main이었음
        MenuItem register = menu.findItem(R.id.nav_manage);
        //View b = findViewById(R.id.nav_manage);
        //b.setVisibility(View.GONE);
        if(ismanager)
        {
            register.setVisible(true);
            Log.e("register","setVisible true");
        }
        else
        {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
            Intent intent1 = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent1);
        }
        else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this,PostcouponActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
