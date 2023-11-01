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
import com.example.myapplication.UIComponents.UIStringView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity  implements TypeMsg, TypeUpdate {
    private Messenger toServiceMessenger;
    private NetServiceConnection netServiceConnection;

    private class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case MSG_TO_MAIN_EXCEPTION -> {
                    addErrorMessage(data.getString("headDebug"),data.getString("debug"),1);
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
                    clearLayout();
                    setWidget(json.get("dataScene").getAsJsonArray(), false);
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
                    setWidget(json.get("dataScene").getAsJsonArray(),true);
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
        connectService();
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
    private void setWidget(JsonArray dataScene, boolean simpleUpdate){
        for (int i = 0;i<dataScene.size();i++){
            try {
                JsonObject object = dataScene.get(i).getAsJsonObject();
                switch (object.get("type").getAsInt()){
                    case 0 -> {
                        int number = object.get("num").getAsInt();
                        int[] headColor = new int[4];
                        int[] headBackgroundColor = new int[4];
                        int[] textColor = new int[4];
                        int[] textBackgroundColor = new int[4];
                        for (int j = 0;j<4;j++){
                            headColor[j] = object.get(String.valueOf(UPDATE_SET_HEAD_COLOR)).getAsJsonArray().get(j).getAsInt();
                            headBackgroundColor[j] = object.get(String.valueOf(UPDATE_SET_HEAD_BACKGROUND_COLOR)).getAsJsonArray().get(j).getAsInt();
                            textColor[j] = object.get(String.valueOf(UPDATE_SET_TEXT_COLOR)).getAsJsonArray().get(j).getAsInt();
                            textBackgroundColor[j] = object.get(String.valueOf(UPDATE_SET_TEXT_BACKGROUND_COLOR)).getAsJsonArray().get(j).getAsInt();
                        }
                        StringBuilder head = new StringBuilder(object.get(String.valueOf(UPDATE_SET_HEAD)).toString());
                        head.deleteCharAt(0).deleteCharAt(head.length()-1);
                        StringBuilder text = new StringBuilder(object.get(String.valueOf(UPDATE_SET_TEXT)).toString());
                        text.deleteCharAt(0).deleteCharAt(text.length()-1);
                        if (!simpleUpdate){
                            addStringView(
                                    this,
                                    number,
                                    head.toString(),
                                    headColor,
                                    headBackgroundColor,
                                    text.toString(),
                                    textColor,
                                    textBackgroundColor
                            );
                        }else {
                            setStringView(
                                    number,
                                    head.toString(),
                                    headColor,
                                    headBackgroundColor,
                                    text.toString(),
                                    textColor,
                                    textBackgroundColor
                            );
                        }
                    }
                }
            }catch (Exception e){
                setDebugText("Ошибка");
                addErrorMessage("Ошибка создания виджета",e.toString(),i+1);
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
    public void addStringView(Context context, Integer number, String headText, int[] headColor, int[] headBackgroundColor, String text, int[] textColor, int[] textBackgroundColor){
        UIStringView stringView = new UIStringView(context, headText,headColor,headBackgroundColor,text,textColor,textBackgroundColor);
        layout.addView(stringView);
        stringViewHashMap.put(number,stringView);
    }
    public void addErrorMessage(String head, String message,int index){
        UIStringView stringView = new UIStringView(this,
                head,
                new int[]{255, 255, 255, 255},
                new int[]{255,255,0,0},
                message,
                new int[]{255,0,0,0},
                new int[]{255, 207, 207, 207}
        );
        layout.addView(stringView, index);
    }
    private void clearLayout(){
        int childCount = layout.getChildCount();
        for (int i = childCount - 1; i > 0; i--) {
            layout.removeViewAt(i);
        }
    }
    public UIStringView getStringView(int number){
        return stringViewHashMap.get(number);

    }
    public void setDebugText(String text){
        debug.setText(text);
    }
    private LinearLayout layout;
    private TextView debug;
    private ImageButton setButton;
}