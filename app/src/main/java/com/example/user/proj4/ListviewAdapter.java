package com.example.user.proj4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListviewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<Listviewitem> data;
    private int layout;
    private Context c;


    public ListviewAdapter(Context context, int layout, ArrayList<Listviewitem> data){
        Log.e("listviewadapter","constructor");
        this.inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data=data;
        this.layout=layout;
        this.c = context;
    }
    @Override
    public int getCount(){return data.size();}
    @Override
    public String getItem(int position){return data.get(position).getName();}
    @Override
    public long getItemId(int position){return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView=inflater.inflate(layout,parent,false);
        }
        Log.e("enter","listviewadapter");
        Listviewitem listviewitem=data.get(position);
        /**store name*/
        TextView name= (TextView)convertView.findViewById(R.id.stname);
        name.setText(listviewitem.getName());
        /**logo*/
        ImageView logo=(ImageView)convertView.findViewById(R.id.stlogo);
        logo.setImageBitmap(listviewitem.getLogo()); //기본 카카오이미지로 아이콘
        /**color*/
        LinearLayout item = (LinearLayout) convertView.findViewById(R.id.item);
        item.setBackgroundColor(Color.parseColor(listviewitem.getColor()));
        /**stamp*/
        int stamp = (int) listviewitem.getPoint();
        int coupon = stamp / 10; //완성된 쿠폰 개수
        int remain = stamp - (coupon * 10); //미완성 쿠폰의 스탬프 개수

        Bitmap check = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.check);
        Bitmap blank = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.blank);

        int[] btns = {R.id.stamp1, R.id.stamp2, R.id.stamp3, R.id.stamp4,R.id.stamp5,R.id.stamp6,R.id.stamp7,R.id.stamp8,R.id.stamp9,R.id.stamp10};

        int j = 0;
        // Check image.
        for(int i = 0; i < remain; i++) {
            ImageView imageView = convertView.findViewById(btns[i]);
            imageView.setImageBitmap(check);
            j = i;
        }
        // Blank image.

        for(int i = j+1; i < 10; i++){

        for(int i = j; i < 10; i++){
            ImageView imageView = convertView.findViewById(btns[i]);
            imageView.setImageBitmap(blank);
        }
        /**Full Coupons*/
        TextView coupon_full = (TextView)convertView.findViewById(R.id.stcoupon_full);
        coupon_full.setText("사용 가능한 쿠폰 : "+ String.valueOf(coupon));

        return convertView;
    }
}