package com.example.user.proj4;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class fragment1 extends Fragment {

    TextView textView;
    public static String what;

    public fragment1(){

    }
    public static void test(String status){
        what = status;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment,container,false);


        textView = (TextView)view.findViewById(R.id.test);
        textView.setText(what);
        return  view;
    }

}
