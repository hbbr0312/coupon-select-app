package com.example.user.proj4;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class PostcouponActivity extends AppCompatActivity implements MyEventListener{
    public String store ="kaist";//TODO:매장 매니저가 로그인하면 입력한 매장이름 받아오기
    public String userid="hbbr"; //TODO:QR code 인식해서 여기에 저장
    public String change;
    public String code; //1:사용, 0:적립

    private TextView comment;
    private EditText pointnum;
    private boolean validedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button use = (Button) findViewById(R.id.use); //사용버튼
        Button point = (Button) findViewById(R.id.point); //적립버튼
        comment = (TextView) findViewById(R.id.comment1);
        pointnum = (EditText) findViewById(R.id.editpoint);

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
        }); //phone number 숫자만만

        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validedit){
                    change = pointnum.getText().toString();
                    code ="0";
                    startEvent();
                }
            }
        });

    }
    public boolean isNumber(String test){
        for(int i =0; i<test.length(); i++){
            if(test.charAt(i)-48>=10 || test.charAt(i)<0){
                return false;
            }
        }
        return true;
    }
    public void startEvent(){
        new POSTing(this).execute("http://socrip4.kaist.ac.kr:3980/postcouponinfo");
    }
    @Override
    public void onEventCompleted(){
    }

    @Override
    public void onEventFailed(){
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
                jsonObject.accumulate("id",userid);
                jsonObject.accumulate("change",change+"");
                jsonObject.accumulate("code",code+"");

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
                    Log.e("status", "6");
                    StringBuffer buffer = new StringBuffer();
                    Log.e("status", "6");
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    Log.e("status", "6");
                    reader.close();
                    Log.e("status", "6");

                    String answer = buffer.toString();
                    Log.e("answer",answer);
                    //answer = "{"+answer;

                    return "";//buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

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
}
