package com.example.user.proj4;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;

public class Session {

    private SharedPreferences prefs;

    public Session(Context cntx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void logout(){
        prefs.edit().putString("id", "").apply();
        prefs.edit().putString("name", "").apply();
        prefs.edit().putString("phone", "").apply();
        prefs.edit().putString("ismanager", "false").apply();
        prefs.edit().putString("storename", "").apply();
    }
    public void setInfo(String id, String name, String phone, boolean ismanager, String storename, String logo, String color){
        prefs.edit().putString("id", id).apply();
        prefs.edit().putString("name", name).apply();
        prefs.edit().putString("phone", phone).apply();
        prefs.edit().putString("storename", storename).apply();
        prefs.edit().putString("color",color).apply();
        prefs.edit().putString("logo",logo).apply();
        if(ismanager) prefs.edit().putString("ismanager", "true").apply();
        else prefs.edit().putString("ismanager", "false").apply();

    }

    public HashMap<String,String> getInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", prefs.getString("id", ""));
        map.put("name", prefs.getString("name", ""));
        map.put("phone", prefs.getString("phone", ""));
        map.put("ismanager", prefs.getString("ismanager", ""));
        if (prefs.getString("ismanager", "").equals("true")) {
            map.put("storename", prefs.getString("storename", ""));
            map.put("color", prefs.getString("color", ""));
            map.put("logo", prefs.getString("logo", ""));
        }
        return map;
    }


}
