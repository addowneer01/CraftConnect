package com.example.myapplication.UIComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.cardview.widget.CardView;

import com.example.myapplication.Master;
import com.example.myapplication.R;

public class UISwitch extends CardView {
    public UISwitch(Context context,int num1, int num2, String text, boolean p, int[] backgroundColor) {
        super(context);
        init(context,num1,num2);
        aSwitch.setText("  "+text);
        aSwitch.setChecked(p);
        aSwitch.setBackgroundColor(Color.argb(backgroundColor[0],backgroundColor[1],backgroundColor[2],backgroundColor[3]));
    }
    public UISwitch(Context context, int num1, int num2){
        super(context);
        init(context,num1,num2);
    }
    public void init(Context context, int num1, int num2){
        this.num1 =num1;
        this.num2 =num2;
        container1 = LayoutInflater.from(context).inflate(R.layout.cmp_switch, this,true);
        aSwitch = container1.findViewWithTag("switch");
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Master.mainActivity.sendSwitch(num1,num2,b);
            }
        });
    }
    public void setChecked(boolean p){
        aSwitch.setChecked(p);
    }
    public void setText(String text){
        aSwitch.setText("  "+text);
    }
    public void setBackgroundColor(int[] backgroundColor){
        aSwitch.setBackgroundColor(Color.argb(backgroundColor[0],backgroundColor[1],backgroundColor[2],backgroundColor[3]));
    }
    protected View container1;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    protected Switch aSwitch;
    static int num1;
    static int num2;
}
