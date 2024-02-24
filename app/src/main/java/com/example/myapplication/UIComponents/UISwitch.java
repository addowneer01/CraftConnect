package com.example.myapplication.UIComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.myapplication.R;

public class UISwitch extends UIContainer{
    public UISwitch(Context context, String headText, int[] textColor, int[] headBackgroundColor) {
        super(context, headText, textColor, headBackgroundColor);
        container2 = LayoutInflater.from(context).inflate(R.layout.cmp_switch, layout,true);
    }
    public UISwitch(Context context){
        super(context);
        container2 = LayoutInflater.from(context).inflate(R.layout.cmp_switch, layout,true);
    }
    protected View container2;

}
