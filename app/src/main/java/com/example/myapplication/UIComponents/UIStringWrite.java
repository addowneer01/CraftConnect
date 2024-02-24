package com.example.myapplication.UIComponents;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.myapplication.Master;
import com.example.myapplication.R;

public class UIStringWrite extends UIContainer implements TypeUpdate{
    public UIStringWrite(Context context, int num, String headText, int[] headColor, int[] headBackgroundColor, String hint, int[] backgroundColor) {
        super(context, headText, headColor, headBackgroundColor);
        this.num = num;
        init(context);
        editText.setHint(hint);
        editText.setTextColor(Color.argb(headColor[0],headColor[1],headColor[2],headColor[3]));
        linearLayout.setBackground(new ColorDrawable(Color.argb(backgroundColor[0],backgroundColor[1],backgroundColor[2],backgroundColor[3])));
        sendButton.setTextColor(Color.argb(headColor[0],headColor[1],headColor[2],headColor[3]));
        sendButton.setBackgroundColor(Color.argb(headBackgroundColor[0],headBackgroundColor[1],headBackgroundColor[2],headBackgroundColor[3]));
    }
    public UIStringWrite(Context context){
        super(context);
        init(context);
    }
    protected int num;
    protected View container2;
    public EditText editText;
    protected Button sendButton;
    protected LinearLayout linearLayout;
    @Override
    public void setHeadColor(int[] headColor){
        super.setHeadColor(headColor);
        sendButton.setTextColor(Color.argb(headColor[0],headColor[1],headColor[2],headColor[3]));
        editText.setTextColor(Color.argb(headColor[0],headColor[1],headColor[2],headColor[3]));
    }
    @Override
    public void setHeadBackgroundColor(int[] headBackgroundColor){
        super.setHeadBackgroundColor(headBackgroundColor);
        sendButton.setBackgroundColor(Color.argb(headBackgroundColor[0],headBackgroundColor[1],headBackgroundColor[2],headBackgroundColor[3]));
    }
    public void setHint(String hint){
        editText.setHint(hint);
    }
    public void setBackgroundColor(int [] backgroundColor){
        linearLayout.setBackground(new ColorDrawable(Color.argb(backgroundColor[0],backgroundColor[1],backgroundColor[2],backgroundColor[3])));
    }
    private void init(Context context){
        container2 = LayoutInflater.from(context).inflate(R.layout.cmp_string_write, layout,true);
        editText = container2.findViewWithTag("EditText");
        editText.setText("");
        sendButton = container2.findViewWithTag("SendButton");
        linearLayout = container2.findViewWithTag("Layout");
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Master.mainActivity.sendWrite(num, editText.getText().toString());
                editText.setText("");
            }
        });
    }
}
