package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.myapplication.UIComponents.UIContainer;
import com.example.myapplication.UIComponents.UIStringView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        Master.setMainActivity(this);
    }    public void setViewMain(){
        setContentView(R.layout.main);
        Master.getMaster().initMainScene();
    }
    public void setViewStart(){setContentView(R.layout.start);}
}