package com.example.user.proj4;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class LoginFragment extends Fragment implements MyEventListener {
    private Button login;
    private Button register;
    private EditText id;
    private EditText password;
    private String idinput;
    private String pwinput;

    private String name;
    private String phone;
    private String storename;
    private String userid;
    private boolean ismanager = false;

    private String response;

    private String color ="";
    private String logo="";

    private int loginsuccess = 2; //1:success , 0:not match ,-1:id does not exist

    public LoginFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login,container,false);

        login = (Button) view.findViewById(R.id.login);
        register = (Button) view.findViewById(R.id.gotoregister);
        id = (EditText) view.findViewById(R.id.userid);
        password = (EditText) view.findViewById(R.id.userpw);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idinput = id.getText().toString();
                pwinput = password.getText().toString();
                if (idinput.matches("")) {
                    Toast.makeText(getActivity(), "id를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (pwinput.matches("")) {
                    Toast.makeText(getActivity(), "password를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else startEvent();

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), registerWhichone.class);
                startActivity(intent);
            }
        });


        return  view;
    }



    public void startEvent() {
        new POSTing(this).execute("http://socrip4.kaist.ac.kr:3780/postlogin");
    }

    @Override
    public void onEventCompleted() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        if (loginsuccess == 1) {
            Log.e("Login", "success!!");
            MainActivity.login = true;
                try {
                    JSONObject iter = new JSONObject(response);
                    name = iter.getString("name");
                    userid = iter.getString("id");
                    phone = iter.getString("phone");
                    storename = iter.getString("store");

                } catch (JSONException e) {
                    Log.e("json", "error");
                    e.printStackTrace();
                }
            if(!storename.equals("")) {
                ismanager=true;
                MainActivity.ismanager=true;
                MainActivity.storename=storename;
                MainActivity.storenamee=storename;
                MainActivity.colorr=color;
                MainActivity.logoo=logo;
                PostcouponActivity.store=storename;
                new GETing(this).execute("http://socrip4.kaist.ac.kr:3780/getstoreinfo?storename="+storename);
                return;
            }else{
                    MainActivity.firstlogin=true;
                    MainActivity.namee=name;
                    MainActivity.idd=userid;
                    MainActivity.phonee=phone;
                MainActivity.session.setInfo(userid, name, phone, ismanager, storename,logo,color);
                startActivity(intent);
            }

        } else if (loginsuccess == -1) {
            Log.e("Login", "존재하지않는 id ");
            Toast.makeText(getActivity(), "존재하지 않는 ID입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (loginsuccess == 0) {
            Log.e("Login", "incorrect password");
            Toast.makeText(getActivity(), "비밀번호가 맞지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (loginsuccess==3){
            try {
                JSONObject iter = new JSONObject(response);
                logo = iter.getString("logo");
                color = iter.getString("color");
                MainActivity.color = color;
                MainActivity.logo = logo;
            } catch (JSONException e) {
                Log.e("json", "error");
                e.printStackTrace();
            }
            MainActivity.session.setInfo(userid, name, phone, ismanager, storename,logo,color);
            startActivity(intent);
        }

    }


    @Override
    public void onEventFailed() {
        Log.e("login", "failed");
    }

    public class POSTing extends AsyncTask<String, String, String> {
        private MyEventListener callback;

        public POSTing(MyEventListener my) {
            callback = my;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("id", id.getText().toString());
                jsonObject.accumulate("password", password.getText().toString());

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    Log.e("enter", "trial");
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
                    Log.e("status", "1");
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    Log.e("status", "2");
                    //Log.e("status",""+con.getResponseCode());//200
                    writer.write(jsonObject.toString());
                    Log.e("status", "3");
                    writer.flush();
                    Log.e("status", "4");
                    writer.close();//버퍼를 받아줌

                    Log.e("status", "5");
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
                    Log.e("answer", answer);
                    Log.e("status", "11");
                    if (answer.equals("-1")) {//id does not exist
                        loginsuccess = -1;
                    } else if (answer.equals("0")) {//id password not match
                        loginsuccess = 0;
                    } else if (answer.length() >=1) { //jsonobject가 올것임
                        //login success
                        loginsuccess = 1;
                    }
                    Log.e("answer", answer);
                    //answer = "{"+answer;

                    return answer;//buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e) { //new URL() falied
                    e.printStackTrace();
                    Log.e("malformed", "");
                } catch (IOException e) { //openConnection() failed
                    e.printStackTrace();
                    Log.e("IOException", "");
                } finally {
                    if (con != null) {
                        con.disconnect();
                        Log.e("disconnect", "");
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                            Log.e("reader close", "");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("IOException2", "");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", "");
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
            if (callback != null) {
                response = result;


                Log.e("callback", "eventcomplete");
                Log.e("loginsuccess", ""+loginsuccess);
                callback.onEventCompleted();
            }
        }
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
                loginsuccess=3; // store color logo저장
                response=result;
                callback.onEventCompleted();
            }
        }
    }
}
