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
        if (loginsuccess == 1) {
            Log.e("Login", "success!!");
            Intent intent = new Intent(getContext(), MainActivity.class);
            MainActivity.login = true;
            if (loginsuccess == 1) {

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
            }
            if(!storename.equals("")) {
                ismanager=true;
                MainActivity.ismanager=true;
                PostcouponActivity.store=storename;
            }
            MainActivity.session.setInfo(userid, name, phone, ismanager, storename); //TODO: Main에서 id,name,phone,ismanager,(storename)지정
            startActivity(intent);
        } else if (loginsuccess == -1) Log.e("Login", "존재하지않는 id ");
        else if (loginsuccess == 0) Log.e("Login", "incorrect password");
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
                    } else if (answer.length() >=1) { //TODO:jsonobject가 올것임
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
}
