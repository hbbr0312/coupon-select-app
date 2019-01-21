package com.example.user.proj4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Listviewitem {
    private String stname;
    private Bitmap logo;
    private String color;
    private int stpoint;
    //rgb , logo binary

    public String getName(){return stname;}
    public int getPoint(){return stpoint;}
    public Bitmap getLogo(){return logo;}
    public String getColor(){return color;}


    public Listviewitem(String name, int point,String color,Bitmap logo){
        this.stname=name;
        this.stpoint=point;
        this.color=color;
        this.logo=logo;
    }
}