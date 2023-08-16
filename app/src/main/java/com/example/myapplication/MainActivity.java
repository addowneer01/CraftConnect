package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.myapplication.UIComponents.UIContainer;
import com.example.myapplication.UIComponents.UIStringView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        Master.setMainActivity(this);
        setViewMain();
    }
    public void setViewMain(){
        setContentView(R.layout.main);
        UIStringView stringView1 = new UIStringView(this);
        UIStringView stringView2 = new UIStringView(this);
        stringView1.changeText("1213");
        LinearLayout layout = findViewById(R.id.asr);
        layout.addView(stringView1);
        layout.addView(stringView2);
        for (int i = 0;i<20;i++){
            layout.addView(new UIStringView(this));
        }
    }
    public void setViewStart(){setContentView(R.layout.start);}
}