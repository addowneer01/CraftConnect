package com.example.myapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Master;
import com.example.myapplication.TypeMsg;
import com.example.myapplication.NetService;
import com.example.myapplication.R;
import com.example.myapplication.UIComponents.TypeUpdate;
import com.example.myapplication.UIComponents.TypeWidget;
import com.example.myapplication.UIComponents.UIStringView;
import com.example.myapplication.UIComponents.UIStringWrite;
import com.example.myapplication.UIComponents.UISwitch;
import com.example.myapplication.UIComponents.UISwitchGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity  implements TypeMsg, TypeUpdate, TypeWidget {
    private Messenger toServiceMessenger;
    private NetServiceConnection netServiceConnection;
    //private Timer timer = new Timer();

    private class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case MSG_TO_MAIN_EXCEPTION -> {
                    addErrorMessage(data.getString("headDebug"),data.getString("debug"),1,20000);
                }
                case MSG_TO_MAIN_RUN -> {
                    if (!data.getBoolean("run")) setActivityStart();
                }
                case MSG_TO_START_RESULT -> {
                    Master.startActivity.resetButton();
                    if (data.getBoolean("result")){
                        Master.startActivity.destroy();
                    }else Master.startActivity.setDebug(data.getString("debug"));
                }
                case MSG_TO_MAIN_DATA_SCENE -> {
                    JsonObject json = toJsonObject(data.getString("json"));
                    setWidget(json.get("dataScene").getAsJsonArray(), true);
//                    addStringWrite(Master.mainActivity,3,"Дисплей", new int[]{255,255,123,123},new int[]{255,123,255,123},"сообщение",new int[]{255,123,123,255});
                }
                case MSG_STOP_SERVICE -> {
                    toServiceMessenger = null;
                    unbindService(netServiceConnection);
                    connectService();
                    setActivityStart("Не удалось переподключиться");
                    setDebugText("Загрузка...");
                }
                case MSG_TO_MAIN_HEAD -> setDebugText(data.getString("head"));
                case MSG_TO_MAIN_SIMPLE_UPDATE -> {
                    JsonObject json = toJsonObject(data.getString("json"));
                    setWidget(json.get("dataScene").getAsJsonArray(),false);
                }
            }
        }
    }
    public void setActivityStart(){
        setActivityStart("");
    }
    public void setActivityStart(String msg){
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("debug", msg);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Master.setMainActivity(this);
        layout = findViewById(R.id.layout_main);
        debug = findViewById(R.id.debug_main);
        setButton = findViewById(R.id.set_button_main);
//        connectService();
        UISwitchGroup uiSwitchGroup = new UISwitchGroup(this,5);
        layout.addView(uiSwitchGroup);
        switchGroupHashMap.put(-1,uiSwitchGroup);
        uiSwitchGroup.setHead("");
    }
    public void connectService(){
        if (!Master.getInstance().isServiceRunning(NetService.class)) startService(new Intent(this, NetService.class));
        netServiceConnection = new NetServiceConnection();
        bindService(new Intent(this, NetService.class),netServiceConnection,BIND_AUTO_CREATE);
    }
    private class NetServiceConnection implements ServiceConnection, TypeMsg {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("app","service connect");
            toServiceMessenger = new Messenger(service);
            Message msg = Message.obtain(null, MSG_CONNECT);
            msg.replyTo = new Messenger(new ActivityHandler());
            sendToService(msg);
            sendToService(Message.obtain(null, MSG_TO_SERVICE_REQUEST_RUN));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    public void sendToService(Message msg){
        try {
            toServiceMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e("Msg to service", e.getMessage());
        }
    }
    public void sendToService(int t, Bundle data){
        Message msg = Message.obtain(null,t);
        msg.setData(data);
        sendToService(msg);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Вызывается, когда активность уходит в фоновый режим
        // Вы можете здесь выполнить действия при сворачивании приложения
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Вызывается, когда активность становится активной
        // Вы можете здесь выполнить действия при возвращении в приложение из фонового режима
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Вызывается, когда активность полностью завершается
        // Вы можете здесь выполнить действия перед закрытием приложения
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    ////////////////////////////////////////////////////////////////
    private JsonObject toJsonObject(String response){
        Gson gson = new Gson();
        return gson.fromJson(response, JsonObject.class);
    }
    private final HashMap<Integer, UIStringView> stringViewHashMap = new HashMap<>();
    private final HashMap<Integer, UIStringWrite> stringWriteHashMap = new HashMap<>();
    private final HashMap<Integer, UISwitchGroup> switchGroupHashMap = new HashMap<>();
    private void setWidget(JsonArray dataScene, boolean refresh){
        if (refresh) clearLayout();
        for (int i = 0;i<dataScene.size();i++){
            try {
                JsonObject object = dataScene.get(i).getAsJsonObject();
                int number = object.get("num").getAsInt();
                int[] headColor = new int[4];
                int[] headBackgroundColor = new int[4];
                for (int j = 0;j<4;j++){
                    headColor[j] = object.get(String.valueOf(UPDATE_SET_HEAD_COLOR)).getAsJsonArray().get(j).getAsInt();
                    headBackgroundColor[j] = object.get(String.valueOf(UPDATE_SET_HEAD_BACKGROUND_COLOR)).getAsJsonArray().get(j).getAsInt();
                }
                StringBuilder head = new StringBuilder(object.get(String.valueOf(UPDATE_SET_HEAD)).toString());
                head.deleteCharAt(0).deleteCharAt(head.length()-1);
                switch (object.get("type").getAsInt()){
                    case STRING_VIEW -> {
                        int[] textColor = new int[4];
                        int[] textBackgroundColor = new int[4];
                        for (int j = 0;j<4;j++){
                            textColor[j] = object.get(String.valueOf(UPDATE_SV_SET_TEXT_COLOR)).getAsJsonArray().get(j).getAsInt();
                            textBackgroundColor[j] = object.get(String.valueOf(UPDATE_SV_SET_TEXT_BACKGROUND_COLOR)).getAsJsonArray().get(j).getAsInt();
                        }
                        StringBuilder text = new StringBuilder(object.get(String.valueOf(UPDATE_SV_SET_TEXT)).toString());
                        text.deleteCharAt(0).deleteCharAt(text.length()-1);
                        if (refresh) addStringView(this, number, head.toString(), headColor, headBackgroundColor, text.toString(), textColor, textBackgroundColor);
                        else setStringView(number, head.toString(), headColor, headBackgroundColor, text.toString(), textColor, textBackgroundColor);
                    }

                    case STRING_WRITE -> {
                        int[] color = new int[4];
                        for (int j = 0;j<4;j++){
                            color[j] = object.get(String.valueOf(UPDATE_SW_SET_BACKGROUND_COLOR)).getAsJsonArray().get(j).getAsInt();
                        }
                        StringBuilder hint = new StringBuilder(object.get(String.valueOf(UPDATE_SW_SET_HINT)).toString());
                        hint.deleteCharAt(0).deleteCharAt(hint.length()-1);
                        if (refresh) addStringWrite(this, number, head.toString(), headColor, headBackgroundColor, hint.toString(), color);
                        else setStringWrite(number, head.toString(), headColor, headBackgroundColor, hint.toString(), color);
                    }

                    case SWITCH_GROUP -> {
                        int num2 = object.get("num2").getAsInt();
                        int[] color1 = new int[4];
                        int[] color2 = new int[4];
                        for (int j = 0;j<4;j++){
                            color1[j] = object.get(String.valueOf(UPDATE_SG_SET_BACKGROUND_COLOR1)).getAsJsonArray().get(j).getAsInt();
                            color2[j] = object.get(String.valueOf(UPDATE_SG_SET_BACKGROUND_COLOR2)).getAsJsonArray().get(j).getAsInt();
                        }
                        JsonArray list = object.getAsJsonArray("switches");
                        if (refresh){
                            UISwitchGroup switchGroup = switchGroupHashMap.get(number);
                            UISwitch[] switches = switchGroup.getSwitches();
                            for (int j = 0;j<list.size();j++){
                                JsonObject jswitch = list.get(j).getAsJsonObject();
                                StringBuilder text = new StringBuilder(jswitch.get(String.valueOf(UPDATE_SG_SET_TEXT)).getAsString());
                                text.deleteCharAt(0).deleteCharAt(text.length()-1);
                                boolean p = jswitch.get(String.valueOf(UPDATE_SG_SET_P)).getAsBoolean();
                                switches[j].setText(text.toString());
                                switches[j].setChecked(p);
                                if (j%2==0) switches[j].setBackgroundColor(color1);
                                else switches[j].setBackgroundColor(color2);
                            }
                        }
                        else {
                            UISwitchGroup switchGroup = new UISwitchGroup(this, head.toString(), headColor, headBackgroundColor);
                            UISwitch[] switches = new UISwitch[list.size()];
                            for (int j = 0;j<list.size();j++){
                                JsonObject jswitch = list.get(j).getAsJsonObject();
                                StringBuilder text = new StringBuilder(jswitch.get(String.valueOf(UPDATE_SG_SET_TEXT)).getAsString());
                                text.deleteCharAt(0).deleteCharAt(text.length()-1);
                                boolean p = jswitch.get(String.valueOf(UPDATE_SG_SET_P)).getAsBoolean();
                                if (j%2==0) switches[j] = new UISwitch(switchGroup.getLayout(), number,num2,text.toString(),p,color1);
                                else switches[j] = new UISwitch(switchGroup.getLayout(), number,num2,text.toString(),p,color2);
                            }
                            switchGroup.init(switches);
                        }

                    }
                }
            }catch (Exception e){
                setDebugText("Ошибка");
                addErrorMessage("Ошибка создания виджета",e.toString(),i+1,60000);
            }
        }
        setDebugText("Подключено");
    }

//    private void update(JsonArray json){
//        try {
//            for (int i = 0;i< json.size();i++){
//                JsonArray update = json.get(i).getAsJsonArray();
//                switch (update.get(1).getAsInt()){
//                    case 0 ->{
//                        UIStringView stringView = getStringView(update.get(0).getAsInt());
//                        switch (update.get(2).getAsInt()){
//                            case UPDATE_SET_HEAD-> stringView.setHead(update.get(3).getAsString());
//                            case UPDATE_SET_HEAD_COLOR-> {
//                                JsonArray colorJson = update.get(3).getAsJsonArray();
//                                int[] color = new int[4];
//                                for (int j = 0;j<4;j++) color[j] = colorJson.get(j).getAsInt();
//                                stringView.setHeadColor(color);
//                            }
//                            case UPDATE_SET_HEAD_BACKGROUND_COLOR-> {
//                                JsonArray colorJson = update.get(3).getAsJsonArray();
//                                int[] color = new int[4];
//                                for (int j = 0;j<4;j++) color[j] = colorJson.get(j).getAsInt();
//                                stringView.setHeadBackgroundColor(color);
//                            }
//                            case UPDATE_SET_TEXT-> stringView.setText(update.get(3).getAsString());
//                            case UPDATE_SET_TEXT_COLOR-> {
//                                JsonArray colorJson = update.get(3).getAsJsonArray();
//                                int[] color = new int[4];
//                                for (int j = 0;j<4;j++) color[j] = colorJson.get(j).getAsInt();
//                                stringView.setTextColor(color);
//                            }
//                            case UPDATE_SET_TEXT_BACKGROUND_COLOR-> {
//                                JsonArray colorJson = update.get(3).getAsJsonArray();
//                                int[] color = new int[4];
//                                for (int j = 0;j<4;j++) color[j] = colorJson.get(j).getAsInt();
//                                stringView.setTextBackgroundColor(color);
//                            }
//                            default -> throw new RuntimeException("Неизвестный тип обновления");
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
//            addErrorMessage("Ошибка обновления",e.getMessage(),1);
//        }
//    }
    public void setStringView(Integer number, String headText, int[] headColor, int[] headBackgroundColor, String text, int[] textColor, int[] textBackgroundColor){
        UIStringView stringView = getStringView(number);
        stringView.setHead(headText);
        stringView.setHeadColor(headColor);
        stringView.setHeadBackgroundColor(headBackgroundColor);
        stringView.setText(text);
        stringView.setTextColor(textColor);
        stringView.setTextBackgroundColor(textBackgroundColor);
    }
    public void setStringWrite(int number, String headText, int[] headColor, int[] headBackgroundColor, String hint, int[] backgroundColor){
        UIStringWrite stringWrite = getStringWrite(number);
        stringWrite.setHead(headText);
        stringWrite.setHeadColor(headColor);
        stringWrite.setHeadBackgroundColor(headBackgroundColor);
        stringWrite.setHint(hint);
        stringWrite.setBackgroundColor(backgroundColor);
    }
    public void addStringView(Context context, Integer number, String headText, int[] headColor, int[] headBackgroundColor, String text, int[] textColor, int[] textBackgroundColor){
        UIStringView stringView = new UIStringView(context, headText,headColor,headBackgroundColor,text,textColor,textBackgroundColor);
        layout.addView(stringView);
        stringViewHashMap.put(number,stringView);
    }
    public void addStringWrite(Context context, int num, String headText, int[] headColor, int[] headBackgroundColor, String hint, int[] backgroundColor){
        UIStringWrite stringWrite = new UIStringWrite(context,num,headText,headColor,headBackgroundColor,hint,backgroundColor);
        layout.addView(stringWrite);
        stringWriteHashMap.put(num,stringWrite);
    }
    public void addErrorMessage(String head, String message,int index, int delay){
        UIStringView stringView = new UIStringView(this,
                head,
                new int[]{255, 255, 255, 255},
                new int[]{255,255,0,0},
                message,
                new int[]{255,0,0,0},
                new int[]{255, 207, 207, 207}
        );
        layout.addView(stringView, index);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layout.removeView(stringView);
            }
        }, delay);
    }
    private void clearLayout(){
        int childCount = layout.getChildCount();
        for (int i = childCount - 1; i > 0; i--) {
            layout.removeViewAt(i);
        }
        stringViewHashMap.clear();
        stringWriteHashMap.clear();
    }
    public UIStringView getStringView(int number){
        return stringViewHashMap.get(number);

    }
    public UIStringWrite getStringWrite(int number){
        return stringWriteHashMap.get(number);

    }
    public void sendWrite(int num, String value){
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        bundle.putString("value",value);
        sendToService(MSG_TO_SERVICE_WRITE, bundle);
    }
    public void sendSwitch(int num1, int num2, boolean p){
        Log.d("switch", num1 + " " + num2 + " " + p);
        Bundle bundle = new Bundle();
        bundle.putInt("num", num1);
        bundle.putInt("num2", num2);
        bundle.putBoolean("p",p);
        sendToService(MSG_TO_SERVICE_SWITCH, bundle);
    }
    public void setDebugText(String text){
        debug.setText(text);
    }
    private LinearLayout layout;
    private TextView debug;
    private ImageButton setButton;
}