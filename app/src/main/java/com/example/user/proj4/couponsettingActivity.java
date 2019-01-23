package com.example.user.proj4;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


//manager가 처음에 회원가입할때만 띄우는 액티비티임
public class couponsettingActivity extends AppCompatActivity implements MyEventListener {
    //현재값
    public static String storecolor = "#9e0000"; //지울거
    public static Bitmap storelogo;
    public String storename;// = "twosome"; //바뀌지 않는값

    public static String firststorename;

    //sample값
    private String samplecolor;
    private Bitmap samplelogo;

    private Button loadlogo;
    private Button setting;

    private ImageView logo;
    private LinearLayout linear;
    private TextView stname;

    private int PICK_IMAGE_REQUEST = 1;
    private String response;
    private boolean getting=true;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couponsetting);

        if(PostcouponActivity.store==null) storename = firststorename;
        else storename=MainActivity.storename;

        checkPermissionREAD_EXTERNAL_STORAGE(couponsettingActivity.this);
        linear = findViewById(R.id.samplecoupon);
        new GETing(this).execute("http://socrip4.kaist.ac.kr:3780/getstoreinfo?storename="+storename);


        Log.e("STORENAME","in setting..." +storename);
        stname =findViewById(R.id.storename);
        stname.setText(storename);

        /**LOAD LOGO : 갤러리에서 사진불러와서 sample로 보여주기*/
        logo = findViewById(R.id.samplelogo);
        loadlogo = findViewById(R.id.logoselect);
        loadlogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionREAD_EXTERNAL_STORAGE(couponsettingActivity.this)) {
                    Intent intent = new Intent();
                    // Show only images, no videos or anything else
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // Always show the chooser (if there are multiple options available)
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
            }
        });

        /**SETTING : sample color, logo를 store color, logo에 저장하고 서버에 posting*/
        setting = findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEvent();
            }
        });

        //update
        if(!getting)updateview();
    }

    /**update coupon view*/
    public void updateview(){
        logo.setImageBitmap(samplelogo);
        linear.setBackgroundColor(Color.parseColor(samplecolor));
    }
    /**color picker*/
    //Handle buttons
    public void Picker1Click(View arg0) {
        //no direct way to get background color as it could be a drawable
        Log.e("click","picker");
        if (linear.getBackground() instanceof ColorDrawable) {
            Log.e("enter","picker");
            ColorDrawable cd = (ColorDrawable) linear.getBackground();
            int colorCode = cd.getColor();
            //pick a color (changed in the UpdateColor listener)
            new ColorPickerDialog(couponsettingActivity.this, new UpdateColor(), colorCode).show();
        }else Log.e("enter","false");
    }
    public class UpdateColor implements ColorPickerDialog.OnColorChangedListener {
        public void colorChanged(int color) {
            //ShowColor.setBackgroundColor(color);
            //show the color value
            samplecolor = "#"+String.format("%08x", color).substring(2);
            updateview();
        }
    }

    /**logo image encoding*/
    public String encoding() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        storelogo.compress(Bitmap.CompressFormat.PNG, 40, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**pick logo image from gallery*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                samplelogo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                logo.setImageBitmap(samplelogo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**POST color, logo to server*/
    public void startEvent(){
        storecolor = samplecolor;
        storelogo = samplelogo;
        encoding();
        new POSTing(this).execute("http://socrip4.kaist.ac.kr:3780/poststoreinfo");
    }

    @Override
    public void onEventCompleted(){

        if(getting){
            try {
                JSONObject iter = new JSONObject(response);
                String logo = iter.getString("logo");
                String color = iter.getString("color");
                storecolor = color;
                storelogo = decodeBase64(logo);
                MainActivity.color = color;
                MainActivity.logo = logo;
                PostcouponActivity.color=color;
                PostcouponActivity.logo=storelogo;
                MainActivity.session.setInfo(MainActivity.userid, MainActivity.name, MainActivity.phone, MainActivity.ismanager, storename,logo,color);
            } catch (JSONException e) {
                Log.e("json", "error");
                e.printStackTrace();
            }
            samplecolor=storecolor;
            samplelogo=storelogo;
            Log.e("store color",storecolor);
            updateview();


            getting=false;
        }else{
            Toast.makeText(couponsettingActivity.this,"Coupon 설정이 변경되었습니다",Toast.LENGTH_SHORT).show();
            PostcouponActivity.color = storecolor;
            PostcouponActivity.logo = storelogo;

            MainActivity.logo=encoding();
            MainActivity.color=storecolor;
            //관리자가 회원가입했을때 쿠폰설정하고 로그인 페이지로 돌아가도록
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }



    }




    public static Bitmap decodeBase64(String input)
        {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        }


@Override
    public void onEventFailed(){
        Log.e("event","failed");
    }

    public class POSTing extends AsyncTask<String, String, String> {
        private MyEventListener callback;
        public POSTing(MyEventListener my){
            callback = my;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                JSONObject jsonObject = new JSONObject();
                Log.e("post store name"," in setting"+storename);
                jsonObject.accumulate("storename",storename);
                jsonObject.accumulate("color",storecolor);
                jsonObject.accumulate("logo",encoding());

                Log.e("enter","doInBackground");
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    Log.e("enter","trial");
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();


                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    Log.e("status","1");
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    Log.e("status","2");
                    //Log.e("status",""+con.getResponseCode());//200
                    writer.write(jsonObject.toString());
                    Log.e("status","3");
                    writer.flush();
                    Log.e("status","4");
                    writer.close();//버퍼를 받아줌

                    Log.e("status","5");
                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    Log.e("enter", "input stream");

                    reader = new BufferedReader(new InputStreamReader(stream));
                    Log.e("status", "6");
                    reader.read();
                    Log.e("status", "7");
                    StringBuffer buffer = new StringBuffer();
                    Log.e("status", "8");
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    Log.e("status", "9");
                    reader.close();
                    Log.e("status", "10");

                    String answer = buffer.toString();
                    Log.e("answer",answer);

                    return answer;

                } catch (MalformedURLException e){ //new URL() falied
                    e.printStackTrace();
                    Log.e("malformed","");
                } catch (IOException e) { //openConnection() failed
                    e.printStackTrace();
                    Log.e("IOException","");
                } finally {
                    if(con != null){
                        con.disconnect();
                        Log.e("disconnect","");
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                            Log.e("reader close","");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("IOException2","");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception","");
            }

            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(callback!=null){
                callback.onEventCompleted();
            }

        }
    }

    /**Permission*/
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);

                // other 'case' lines to check for other
                // permissions this app might request
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        Log.i("fragment2", "checkPermissionREAD_EXTERNAL_STORAGE");
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //showDialog("External storage", context,
                    //Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        Log.i("fragment2", "showDialog");
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public class GETing extends AsyncTask<String, String, String> {
        public String get;
        private MyEventListener callback;
        public GETing(MyEventListener my){
            callback = my;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("GET");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    //con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();
                    Log.e("status",""+con.getResponseCode());

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    get = buffer.toString();
                    Log.e("result",get);
                    return get;//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(callback!=null){
                response=result;
                getting=true;
                callback.onEventCompleted();
            }
        }
    }
}
