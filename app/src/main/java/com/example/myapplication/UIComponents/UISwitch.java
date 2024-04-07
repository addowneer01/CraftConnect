package com.example.myapplication.UIComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import androidx.cardview.widget.CardView;

import com.example.myapplication.R;

public class UISwitch extends CardView {
    public UISwitch(Context context, String headText, int[] textColor, int[] headBackgroundColor) {
        super(context);
        container1 = LayoutInflater.from(context).inflate(R.layout.cmp_switch, this,true);
        aSwitch = container1.findViewWithTag("switch");
    }
    public UISwitch(Context context){
        super(context);
        container1 = LayoutInflater.from(context).inflate(R.layout.cmp_switch, this,true);
        aSwitch = container1.findViewWithTag("switch");
    }
    protected View container1;
    protected Switch aSwitch;

}
