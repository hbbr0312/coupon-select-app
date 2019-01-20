package com.example.user.proj4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class registerWhichone extends AppCompatActivity {

    private Button gotom;
    private Button gotou;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_whichone);

        gotou = findViewById(R.id.gotouser);
        gotom = findViewById(R.id.gotomanager);

        //user register page
        gotou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registerWhichone.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        //manager register page
        gotom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(registerWhichone.this,managerRegister.class);
                startActivity(intent1);
            }
        });
    }
}
