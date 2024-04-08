package com.example.myapplication.UIComponents;

import android.content.Context;

public class UISwitchGroup extends UIContainer{
    public UISwitchGroup(Context context, String headText, int[] headColor, int[] headBackgroundColor) {
        super(context, headText, headColor, headBackgroundColor);
    }
    public void init(UISwitch[] switches){
        this.switches = switches;
        add();
    }
    public Context getLayout(){
        return layout.getContext();
    }
    public UISwitchGroup(Context context, int countSwitch){
        super(context);
        switches = new UISwitch[countSwitch];
        for (int i = 0;i<countSwitch;i++){
            UISwitch uiSwitch = new UISwitch(context, -1,i);
            if (i%2==1) uiSwitch.setBackgroundColor(new int[]{255,150,150,150});
            switches[i] = uiSwitch;
        }
        add();
    }
    public void setBackgroundColor1(int[] color){
        for (int i = 0;i<switches.length;i+=2){
            switches[i].setBackgroundColor(color);
        }
    }
    public void setBackgroundColor2(int[] color){
        for (int i = 1;i<switches.length;i+=2){
            switches[i].setBackgroundColor(color);
        }
    }
    private void add(){
        for (UISwitch i:switches){
            layout.addView(i);
        }
    }
    public UISwitch[] getSwitches(){
        return switches;
    }
    private UISwitch[] switches;
}