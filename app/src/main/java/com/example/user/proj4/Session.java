package com.example.user.proj4;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;

public class Session {

    private SharedPreferences prefs;

    public Session(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void logout(){
        prefs.edit().putString("id", "").commit();
        prefs.edit().putString("name", "").commit();
        prefs.edit().putString("phone", "").commit();
        prefs.edit().putString("ismanager", "false").commit();
        prefs.edit().putString("storename", "").commit();
    }
    public void setInfo(String id, String name, String phone, boolean ismanager, String storename){
        prefs.edit().putString("id", id).commit();
        prefs.edit().putString("name", name).commit();
        prefs.edit().putString("phone", phone).commit();
        prefs.edit().putString("ismanager", ismanager+"").commit();
        prefs.edit().putString("storename", storename).commit();
    }

    public HashMap<String,String> getInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", prefs.getString("id", ""));
        map.put("name", prefs.getString("name", ""));
        map.put("phone", prefs.getString("phone", ""));
        map.put("ismanager", prefs.getString("ismanager", ""));
        if (prefs.getString("ismanager", "").equals("true")) {
            map.put("storename", prefs.getString("storename", ""));
        }
        return map;
    }


}
