package com.example.myapplication.UIComponents;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;

public abstract class UIContainer extends FrameLayout {
    public UIContainer(Context context, String headText,int[] headColor, int[] headBackgroundColor) {
        super(context);
        init(context);
        head.setText(headText);
        head.setTextColor(Color.argb(headColor[0],headColor[1],headColor[2],headColor[3]));
        head.setBackgroundColor(Color.argb(headBackgroundColor[0],headBackgroundColor[1],headBackgroundColor[2],headBackgroundColor[3]));
    }
    public UIContainer(Context context){
        super(context);
        init(context);
    }
    protected View container1;
    protected TextView head;
    protected LinearLayout layout;

    private void init(Context context){
        container1 = LayoutInflater.from(context).inflate(R.layout.cmp_ui_container, this,true);
        head = container1.findViewWithTag("headText");
        layout = container1.findViewWithTag("layout");
    }
}
