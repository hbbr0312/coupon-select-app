package com.example.user.proj4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
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

/**view coupon list!*/
public class couponsActivity extends AppCompatActivity implements MyEventListener {
    private ArrayList<Listviewitem> data;
    private ListView lv;
    private ImageView qrcode;
    private String id ="test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupons);

        data = new ArrayList<>();
        lv = (ListView) findViewById(R.id.ListView);
        id = MainActivity.userid;

    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(couponsActivity.this, "login "+MainActivity.login, Toast.LENGTH_SHORT).show();
        if(MainActivity.login){
            //listview
            new GETing(this).execute("http://socrip4.kaist.ac.kr:3780/getcouponinfo?id="+id);

            //qr code생성
            qrcode = (ImageView) findViewById(R.id.qrcode);
            Bitmap bitmap = generateQRCode(id);
            qrcode.setImageBitmap(bitmap);
        }

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
    public void onEventCompleted(){
        Log.e("on completed data size",""+data.size());
        ListviewAdapter adapter1 = new ListviewAdapter(this,R.layout.list_item, data);
        Log.e("adpater get count",""+adapter1.getCount());
        lv.setAdapter(adapter1);
        Log.e("get coupon information","success");

    }
    @Override
    public void onEventFailed(){
        Log.e("get coupon information","failed");
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
                    Log.e("server send message","'"+get+"'");

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
                if(result==null){
                    callback.onEventCompleted();
                    return;
                }
                try {
                    JSONArray json = new JSONArray(result);
                    for (int i = 0; i < json.length(); i++) {
                        Log.e("jason", ""+i);
                        JSONObject iter = json.getJSONObject(i);
                        String storename = iter.getString("store");
                        String stpoint = iter.getString("num_coupon");
                        String color = iter.getString("color");
                        String logo = iter.getString("logo");

                        /**convert to proper shape*/
                        int point = Integer.parseInt(stpoint);
                        Bitmap logobit = decodeBase64(logo);
                        /*
                        byte[] bytedata1 = Base64.decode(logo,0);
                        ByteArrayInputStream inStream1 = new ByteArrayInputStream(bytedata1);
                        Bitmap logobit = BitmapFactory.decodeStream(inStream1);
                        */

                        Listviewitem item = new Listviewitem(storename,point,color,logobit);
                        data.add(item);
                    }

                } catch (JSONException e) {
                    Log.e("json", "error");
                    e.printStackTrace();
                }

                callback.onEventCompleted();
            }
        }
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
