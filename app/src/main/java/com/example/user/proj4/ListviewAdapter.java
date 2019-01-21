package com.example.user.proj4;

import android.content.Context;
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
        /**point*/
        TextView point=(TextView)convertView.findViewById(R.id.stpoint);
        point.setText("point : "+listviewitem.getPoint());
        /**logo*/
        ImageView logo=(ImageView)convertView.findViewById(R.id.stlogo);
        logo.setImageBitmap(listviewitem.getLogo()); //기본 카카오이미지로 아이콘
        /**color*/
        LinearLayout item = (LinearLayout) convertView.findViewById(R.id.item);
        item.setBackgroundColor(Color.parseColor(listviewitem.getColor()));

        return convertView;
    }
}