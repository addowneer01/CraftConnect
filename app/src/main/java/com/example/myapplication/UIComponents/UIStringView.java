package com.example.myapplication.UIComponents;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;

public class UIStringView extends UIContainer{
    public UIStringView(Context context, String headText, int[] headColor, int[] headBackgroundColor, String text, int[] textColor, int[] textBackgroundColor) {
        super(context, headText, headColor, headBackgroundColor);
        init(context);
        textView.setText(text);
        textView.setTextColor(Color.argb(textColor[0],textColor[1],textColor[2],textColor[3]));
        textView.setBackgroundColor(Color.argb(textBackgroundColor[0],textBackgroundColor[1],textBackgroundColor[2],textBackgroundColor[3]));
    }
    public UIStringView(Context context){
        super(context);
        init(context);
    }
    public void setText(String text) {
        textView.setText(text);
    }
    public void setTextColor(int[] textColor){
        textView.setTextColor(Color.argb(textColor[0],textColor[1],textColor[2],textColor[3]));
    }
    public void setTextBackgroundColor(int[] textBackgroundColor){
        textView.setBackgroundColor(Color.argb(textBackgroundColor[0],textBackgroundColor[1],textBackgroundColor[2],textBackgroundColor[3]));
    }
    protected View container2;
    protected TextView textView;
    private void init(Context context){
        container2 = LayoutInflater.from(context).inflate(R.layout.cmp_string_view, layout,true);
        textView = container2.findViewWithTag("textView");
    }
}
