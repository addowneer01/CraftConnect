package com.example.myapplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Exceptions.ConnectionBreakException;
import com.example.myapplication.Exceptions.IncorrectPasswordException;
import com.example.myapplication.Exceptions.TimeOutResponseException;
import com.example.myapplication.UIComponents.UIStringView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Master {
    private static Master master;
    private static MainActivity mainActivity;
    public StartScene startScene;
    public MainScene mainScene;
    private Master(){
        startScene = new StartScene();
    }
    public void initMainScene(){
        if (mainScene == null){
            mainScene = new MainScene();
        }
    }
    public static Master getMaster() {
        if (master == null){
            master = new Master();
        }
        return master;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        Master.mainActivity = mainActivity;
        getMaster();
    }
////////////////////////////////////////////////////////////////////////////////
    private Socket socket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private BufferedReader reader;
    private OutputStream output;
    private void sendJson(String ms) throws IOException {
        output.write(ms.getBytes());
        output.write('\n');
        output.flush();
    }
    private long getTime(){
        final long[] time = new long[1];
        time[0] = System.currentTimeMillis();
        return time[0];
    }

    private class ConnectRunnable implements Runnable{
        private ConnectRunnable(String ip,int port, String password){
            this.ip = ip;
            this.port = port;
            this.password = password;
        }
        String ip;
        int port;
        String password;
        String debugMs;
        @Override
        public void run() {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                socket.setSoTimeout(5000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = socket.getOutputStream();
                Gson gson = new Gson();
                JsonObject json = new JsonObject();
                json.addProperty("type", 1);
                json.addProperty("password", password);
                String request = gson.toJson(json);
                sendJson(request);
                gson = new Gson();
                String response = reader.readLine();
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);;
//                while (jsonResponse == null && socket.isConnected()) {
//                    response = reader.readLine();
//                    jsonResponse = gson.fromJson(response, JsonObject.class);
//                    Log.d("Connect",getTime()-startTime + "");
//                    if(getTime()-startTime>5000) throw new TimeOutResponseException();
//                }
                if(!jsonResponse.get("status").getAsString().equals("success")) {
                    debugMs = jsonResponse.get("debugMs").getAsString();
                    throw new IncorrectPasswordException();
                }
                JsonArray dataScene = jsonResponse.getAsJsonArray("dataScene");
                if(socket.isClosed()) throw new ConnectionBreakException();
                Log.d("MainScene",response);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScene.resetButton();
                        mainActivity.setViewMain();
                        mainScene.setWidget(dataScene);
                    }
                });
            }
            catch (IncorrectPasswordException e){
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScene.debug.setText(debugMs);
                        startScene.resetButton();
                    }
                });
            }
            catch (ConnectException e){
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScene.debug.setText("Ошибка подключения");
                        startScene.resetButton();
                    }
                });
            }
            catch (SocketTimeoutException e){
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScene.debug.setText("Превышено время ожидания ответа");
                        startScene.resetButton();
                    }
                });
            }
            catch (Exception e){
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScene.debug.setText(e.toString());
                        startScene.resetButton();
                    }
                });
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
    protected class StartScene{
        private void onClick(){
            try {
                connectButton.setText("...");
                connectButton.setEnabled(false);
                String ip = ip1.getText().toString() + "." + ip2.getText().toString() + "." + ip3.getText().toString() + "." + ip4.getText().toString();
                String portSt = port.getText().toString();
                int portInt;
                if (!portSt.equals(""))  portInt = Integer.parseInt(portSt);
                else portInt = 8888;
                String password = this.password.getText().toString();
                if (password.equals(""))  password = "0000";
                executorService.execute(new ConnectRunnable(ip,portInt,password));
            }catch (Exception e){
                debug.setText(e.toString());
                resetButton();
            }
        }
        private void resetButton(){
            connectButton.setText("Подключиться");
            connectButton.setEnabled(true);
        }
        private StartScene(){
            ip1 = mainActivity.findViewById(R.id.ip_plain_1);
            ip2 = mainActivity.findViewById(R.id.ip_plain_2);
            ip3 = mainActivity.findViewById(R.id.ip_plain_3);
            ip4 = mainActivity.findViewById(R.id.ip_plain_4);
            port = mainActivity.findViewById(R.id.port_plain);
            password = mainActivity.findViewById(R.id.password_plain);
            debug = mainActivity.findViewById(R.id.debug_text);
            connectButton = mainActivity.findViewById(R.id.connect_button);
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startScene.onClick();
                }
            });
        }
        private final EditText ip1;
        private final EditText ip2;
        private final EditText ip3;
        private final EditText ip4;
        private final EditText port;
        private final EditText password;
        private final Button connectButton;
        private final TextView debug;
    }
    protected class MainScene {
        private final HashMap<Integer, UIStringView> stringViewHashMap = new HashMap<>();
        private void setWidget(JsonArray dataScene){
            for (int i = 0;i<dataScene.size();i++){
                try {
                    JsonObject object = dataScene.get(i).getAsJsonObject();
                    switch (object.get("type").getAsInt()){
                        case 0 -> {
                            int[] headColor = new int[4];
                            int[] headBackgroundColor = new int[4];
                            int[] textColor = new int[4];
                            int[] textBackgroundColor = new int[4];
                            for (int j = 0;j<4;j++){
                                headColor[j] = object.get("headColor").getAsJsonArray().get(j).getAsInt();
                                headBackgroundColor[j] = object.get("headBackgroundColor").getAsJsonArray().get(j).getAsInt();
                                textColor[j] = object.get("textColor").getAsJsonArray().get(j).getAsInt();
                                textBackgroundColor[j] = object.get("textBackgroundColor").getAsJsonArray().get(j).getAsInt();
                            }
                            StringBuilder head = new StringBuilder(object.get("head").toString());
                            head.deleteCharAt(0).deleteCharAt(head.length()-1);
                            StringBuilder text = new StringBuilder(object.get("text").toString());
                            text.deleteCharAt(0).deleteCharAt(text.length()-1);
                            addStringView(
                                    mainActivity,
                                    object.get("num").getAsInt(),
                                    head.toString(),
                                    headColor,
                                    headBackgroundColor,
                                    text.toString(),
                                    textColor,
                                    textBackgroundColor
                            );
                        }
                    }
                    setDebugText("Подключено");
                }catch (Exception e){
                    setDebugText("Ошибка");
                    addErrorMessage("Ошибка создания виджета",e.toString());
                }
            }
        }
        private void update(JsonArray json){
            try {
                for (int i = 0;i< json.size();i++){
                    JsonArray update = json.get(i).getAsJsonArray();
                    switch (update.get(0).getAsInt()){
                        case 0 ->{
                            UIStringView stringView = getStringView(update.get(1).getAsInt());
                            switch (update.get(2).getAsString()){
                                case "setHead"-> stringView.setHead(update.get(3).getAsString());
                                case "setHeadColor"-> {
                                    JsonArray colorJson = update.get(3).getAsJsonArray();
                                    int[] color = new int[4];
                                    for (int j = 0;j<4;i++) color[i] = colorJson.get(i).getAsInt();
                                    stringView.setHeadColor(color);
                                }
                                case "setHeadBackgroundColor"-> {
                                    JsonArray colorJson = update.get(3).getAsJsonArray();
                                    int[] color = new int[4];
                                    for (int j = 0;j<4;i++) color[i] = colorJson.get(i).getAsInt();
                                    stringView.setHeadBackgroundColor(color);
                                }
                                case "setText"-> stringView.setText(update.get(3).getAsString());
                                case "setTextColor"-> {
                                    JsonArray colorJson = update.get(3).getAsJsonArray();
                                    int[] color = new int[4];
                                    for (int j = 0;j<4;i++) color[i] = colorJson.get(i).getAsInt();
                                    stringView.setTextColor(color);
                                }
                                case "setTextBackgroundColor"-> {
                                    JsonArray colorJson = update.get(3).getAsJsonArray();
                                    int[] color = new int[4];
                                    for (int j = 0;j<4;i++) color[i] = colorJson.get(i).getAsInt();
                                    stringView.setTextBackgroundColor(color);
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                addErrorMessage("Ошибка обновления",e.toString());
            }
        }
        public void addStringView(Context context, Integer number, String headText, int[] headColor, int[] headBackgroundColor, String text, int[] textColor, int[] textBackgroundColor){
            UIStringView stringView = new UIStringView(context, headText,headColor,headBackgroundColor,text,textColor,textBackgroundColor);
            layout.addView(stringView);
            stringViewHashMap.put(number,stringView);
        }
        public void addErrorMessage(String head, String message){
            UIStringView stringView = new UIStringView(mainActivity,
                    head,
                    new int[]{255, 255, 255, 255},
                    new int[]{255,255,0,0},
                    message,
                    new int[]{255,0,0,0},
                    new int[]{255, 207, 207, 207}
                    );
            layout.addView(stringView);
        }
        public UIStringView getStringView(int id){
            return stringViewHashMap.get(id);
        }
        private MainScene(){
            layout = mainActivity.findViewById(R.id.layout_main);
            debug = mainActivity.findViewById(R.id.debug_main);
            setButton = mainActivity.findViewById(R.id.set_button_main);
        }
        public void setDebugText(String text){
            debug.setText(text);
        }
        private final LinearLayout layout;
        private final TextView debug;
        private final ImageButton setButton;
    }
}
