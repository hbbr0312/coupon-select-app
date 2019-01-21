package com.example.user.proj4;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.HashMap;


public class managerRegister extends AppCompatActivity implements MyEventListener {
    private Button duplecheck;
    private Button register;

    private EditText editId;
    private EditText editPw;
    private EditText editName;
    private EditText editPhone;
    private EditText editCode;

    private TextView comment;

    private HashMap<String, String> codematch = new HashMap(); //TODO: code넣어주기 key:store, value:code

    private Spinner spinner;

    private String validId="";
    private String store;
    private String code;

    private int index; //1:duplication check, 2:register
    private boolean valid = false;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_register);

        duplecheck = (Button) findViewById(R.id.mduplecheck);
        register = (Button) findViewById(R.id.mregister);
        editId = (EditText) findViewById(R.id.meditId);
        editPw = (EditText) findViewById(R.id.meditPw);
        editName = (EditText) findViewById(R.id.meditName);
        editPhone = (EditText) findViewById(R.id.meditPhone);
        comment = (TextView) findViewById(R.id.mcomment);
        editCode = (EditText) findViewById(R.id.meditcode);
        spinner = (Spinner)findViewById(R.id.mspinner1);


        //store spinner
        ArrayList<String> storelist = new ArrayList<String>();
        storelist.add("twosomeplace");
        codematch.put("twosomeplace","AAA");
        /*storelist[0] = "twosomeplace";
        storelist[1] = "lotteria";
        storelist[2] = "droptop";
        storelist[3] = "ogada";
        storelist[4] = "mangosix";
        storelist[5] = "subway";
        storelist[6] = "dunkindounuts";*/

        ArrayAdapter spinnerAdapter;
        spinnerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, storelist);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                store = spinner.getItemAtPosition(position).toString();
                Toast.makeText(managerRegister.this,"선택된 아이템 : "+spinner.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //input condition
        editId.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
                if(editId.getText().toString().length()<4){
                    comment.setText("id는 4자리 이상이어야 합니다");
                }else comment.setText("");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        }); //id 4자리 이상
        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
                if(!isNumber(editPhone.getText().toString())){
                    comment.setText("형식에 맞지않는 번호입니다");
                }
                else comment.setText("");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        }); //phone number 숫자만만
        editPw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //password ***

        duplecheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index=1;
                if(editId.getText().toString().matches("")){
                    Toast.makeText(managerRegister.this,"id입력...",Toast.LENGTH_SHORT).show();
                }
                else startEvent();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index=2;
                Log.e("valid num",valid+"");
                Log.e("valid Id",validId);
                Log.e("editText",editId.getText().toString());

                if(valid && validId.equals(editId.getText().toString())){
                    if(isManager()) startEvent();
                    else Toast.makeText(managerRegister.this,"code가 맞지않습니다.",Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(managerRegister.this,"id 중복확인을 해주세요",Toast.LENGTH_SHORT).show();
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

    //store와 code가 맞는지 확인
    public boolean isManager(){
        code = editCode.getText().toString();
        if(!codematch.get(store).equals(code)) return true;
        return false;
    }

    public void startEvent(){
        if(index==1) new GETing(this).execute("http://socrip4.kaist.ac.kr:3780/getidvalidity?id="+editId.getText().toString());
        else if(index==2) {
            isManager();
            new POSTing(this).execute("http://socrip4.kaist.ac.kr:3780/postmember");
        }
    }
    @Override
    public void onEventCompleted(){
        if(index==1){
            if(valid) Toast.makeText(managerRegister.this,"사용가능한 id입니다.",Toast.LENGTH_SHORT).show();
            else Toast.makeText(managerRegister.this,"이미 사용중인 id입니다.",Toast.LENGTH_SHORT).show();
        }
        if(index==2){
            Toast.makeText(managerRegister.this,"회원가입이 완료되었습니다.",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), couponsettingActivity.class);
            intent.putExtra("register",true);
            MainActivity.ismanager=true; //TODO: manager로 로그인했을때로 옮기기
            startActivity(intent);
        }


    }
    @Override
    public void onEventFailed(){
        Toast.makeText(managerRegister.this,"falied.",Toast.LENGTH_SHORT).show();
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
                    Log.e("server send message",get);
                    if(get.equals(" 1")) {
                        valid = true;
                        validId = editId.getText().toString();
                        return "";
                    }else if(get.equals(" 0")){
                        valid = false;
                    }
                    Log.e("result",get);
                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

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
                callback.onEventCompleted();
            }
        }
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
                jsonObject.accumulate("id",editId.getText().toString());
                jsonObject.accumulate("password",editPw.getText().toString());
                jsonObject.accumulate("name",editName.getText().toString());
                jsonObject.accumulate("phone",editPhone.getText().toString());
                jsonObject.accumulate("store",store);

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
                    reader.read();

                    StringBuffer buffer = new StringBuffer();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    reader.close();

                    String answer = buffer.toString();
                    Log.e("status",answer);
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



