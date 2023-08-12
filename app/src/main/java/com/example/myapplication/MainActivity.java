package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        Master.setMainActivity(this);

    }
    public void setViewMain(){
        setContentView(R.layout.activity_main);
    }
    public void setViewStart(){setContentView(R.layout.start);}
}