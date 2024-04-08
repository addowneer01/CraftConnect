package com.example.myapplication.UIComponents;

import android.content.Context;

public class UISwitchGroup extends UIContainer{
    public UISwitchGroup(Context context, String headText, int[] headColor, int[] headBackgroundColor, UISwitch[] switches) {
        super(context, headText, headColor, headBackgroundColor);
        this.switches = switches;
        add();
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
    private void add(){
        for (UISwitch i:switches){
            layout.addView(i);
        }
    }
    public UISwitch getSwitch(int index){
        return switches[index];
    }
    private UISwitch[] switches;
}