package com.example.user.proj4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PostcouponActivity extends AppCompatActivity implements MyEventListener {
    public static String store; //="kaist";//TODO:매장 매니저가 로그인하면 입력한 매장이름 받아오기
    public String userid;
    public String change="0";
    public String code="0"; //1:사용, 0:적립

    public static String color="#af120a"; //TODO:couon setting할때 입력한 색깔 가져오기
    public static Bitmap logo; //TODO:couon setting할때 입력한 logo 가져오기

    private String userpoint;
    private LinearLayout coupon;

    private TextView comment;
    private TextView viewid;
    private TextView viewpoint;
    private TextView storename;
    private ImageView storelogo;

    private EditText pointnum;
    private EditText usenum;

    private Button useinc;
    private Button usedec;
    private Button pointdec;
    private Button pointinc;
    private Button scan;

    private boolean validedit=true; //포인트입력란에 숫자입력했는지
    private boolean usevalidedit=true; //사용입력란에 숫자입력했는지

    // QR code scanner object.
    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_coupon);

        // Initializing scan object.
        qrScan = new IntentIntegrator(this);
        if(store==null) Log.e("STORE NAME is null","...");
        else Log.e("STORE NAME",store);

        comment = (TextView) findViewById(R.id.comment1); //warning comment

        /**user coupon*/
        storename = (TextView) findViewById(R.id.storename); //storename
        storename.setText(store);
        scan = (Button) findViewById(R.id.gotoscan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.setPrompt("Scanning...");
                qrScan.initiateScan();
            }
        });
        coupon = findViewById(R.id.stcoupon);
        coupon.setBackgroundColor(Color.parseColor(color));
        //logo=BitmapFactory.decodeResource(getResources(), R.drawable.two); //TODO:logo받아서
        storelogo = findViewById(R.id.storelogo);
        storelogo.setImageBitmap(logo);


        /**user information*/
        viewid = (TextView) findViewById(R.id.viewid);
        viewpoint = (TextView) findViewById(R.id.viewpoint);

        /**point use*/
        usedec = (Button) findViewById(R.id.usedec); //use point decrease button
        useinc = (Button) findViewById(R.id.useinc); //use point increase button
        usenum = (EditText) findViewById(R.id.useedit); //사용 point 입력란
        usenum.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
                if(!isNumber(usenum.getText().toString())){
                    comment.setText("숫자만 입력해주세요");
                    usevalidedit=false;
                }
                else {
                    comment.setText("");
                    usevalidedit=true;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });
        usedec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEdit(usenum,true);
            }
        });
        useinc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEdit(usenum,false);
            }
        });
        Button use = (Button) findViewById(R.id.use); //사용버튼
        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usevalidedit){
                    int d = Integer.parseInt(usenum.getText().toString())*10; //10번적립해야 한번 사용
                    change = Integer.toString(d);
                    code="1";
                    startEvent();
                }
            }
        });

        /**point accumulate*/
        pointdec = (Button) findViewById(R.id.pointdec); //point decrease button
        pointinc = (Button) findViewById(R.id.pointinc); //point increase button
        pointnum = (EditText) findViewById(R.id.pointedit); //적립 point 입력란
        pointnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
                if(!isNumber(pointnum.getText().toString())){
                    comment.setText("숫자만 입력해주세요");
                    validedit=false;
                }
                else {
                    comment.setText("");
                    validedit=true;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });
        pointdec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEdit(pointnum,true);
            }
        });
        pointinc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEdit(pointnum,false);
            }
        });
        Button point = (Button) findViewById(R.id.point); //적립버튼
        point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click","point post");
                Log.e("validedit",""+validedit);
                if(validedit){
                    change = pointnum.getText().toString();
                    code ="0";
                    startEvent();
                }
            }
        });
    }

    // Get the scan results.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            // If there's a QR code.
            if (result.getContents() != null){
                userid = result.getContents();
                startEvent();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //+ - 버튼으로 숫자 증감
    public void changeEdit(EditText edit, boolean isDec){
        int master = Integer.parseInt(edit.getText().toString());
        if(isDec&&master>0){
            master--;
        }else if(!isDec){
            master++;
        }else return;
        edit.setText(Integer.toString(master));
    }

    public boolean isNumber(String test){
        for(int i =0; i<test.length(); i++){
            if(test.charAt(i)-48>=10 || test.charAt(i)<0){
                return false;
            }
        }
        return true;
    }

    public void updateinfo(){
        viewid.setText("user id : "+userid);
        viewpoint.setText("point : "+userpoint);
    }
    public void startEvent(){
        new POSTing(this).execute("http://socrip4.kaist.ac.kr:3780/postcouponinfo");
    }
    @Override
    public void onEventCompleted(){
        Toast.makeText(this,"complete!",Toast.LENGTH_SHORT).show();
        updateinfo();
        Log.e("event","completed");
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
                jsonObject.accumulate("store",store);
                Log.e("post store name",store);
                jsonObject.accumulate("id",userid);
                jsonObject.accumulate("change",change);
                jsonObject.accumulate("code",code);

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
                try {
                    JSONObject iter = new JSONObject(result);
                    userid=iter.getString("id");
                    userpoint = iter.getString("num_coupon");
                    Log.e("userid",userid);
                    Log.e("num_coupon",userpoint);
                    //String mobile = iter.getString("mobile");
                    //String img = iter.getString("img");

                } catch (JSONException e) {
                    Log.e("json", "error");
                    e.printStackTrace();
                }
                callback.onEventCompleted();
            }

        }
    }




}
