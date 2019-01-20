package com.example.user.proj4;

import android.graphics.Bitmap;

public class Listviewitem {
    private String stname;
    private String stpoint;

    public String getName(){return stname;}
    public String getPoint(){return stpoint;}

    public Listviewitem(String name, String point){
        this.stname=name;
        this.stpoint=point;
    }
}