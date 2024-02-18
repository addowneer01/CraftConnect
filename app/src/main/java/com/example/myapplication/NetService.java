package com.example.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.Exceptions.ConnectionBreakException;
import com.example.myapplication.Exceptions.IncorrectPasswordException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetService extends Service implements TypeMsg {
    private Socket socket;
    private boolean run = false;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private BufferedReader reader;
    private OutputStream output;
    private String ip;
    private int port;
    private String password;
    Messenger toActivityMessenger;
    SocketRequestRunnable socketRequestRunnable;
    private synchronized long getTime(){
        final long[] time = new long[1];
        time[0] = System.currentTimeMillis();
        return time[0];
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //startForeground(1, notification);
        return START_NOT_STICKY;
    }
    private class ServiceHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case MSG_CONNECT -> {
                    toActivityMessenger = msg.replyTo;
                }
                case MSG_TO_SERVICE_REQUEST_RUN -> {
                    Message rMsg = Message.obtain(null, MSG_TO_MAIN_RUN);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("run", run);
                    rMsg.setData(bundle);
                    sendToActivity(rMsg);
                }
                case MSG_TO_SERVICE_START -> {
                    executorService.execute(new ConnectRunnable(data.getString("ip"),data.getInt("port"),data.getString("password")));
                }
                case MSG_TO_SERVICE_WRITE -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", 4);
                    json.addProperty("num", data.getInt("num"));
                    json.addProperty("value", data.getString("value"));
                    socketRequestRunnable.sendOnThread(json);
                }
            }
        }
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
            Bundle bundle = new Bundle();
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
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                if (jsonResponse.get("type").getAsInt() != 1) throw new RuntimeException("Неправильный тип пакета");
                if(!jsonResponse.get("status").getAsString().equals("success")) {
                    debugMs = jsonResponse.get("debugMs").getAsString();
                    throw new IncorrectPasswordException();
                }
                if(socket.isClosed() || !socket.isConnected()) throw new ConnectionBreakException();
                bundle.putBoolean("result",true);
                NetService.this.ip = ip;
                NetService.this.port = port;
                NetService.this.password = password;
                executorService.execute(new SocketReaderRunnable());
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type",2);
                sendJson(jsonObject);
            }catch (IncorrectPasswordException e){
                bundle.putString("debug", debugMs);
                bundle.putBoolean("result",false);
            }
            catch (ConnectException e){
                bundle.putString("debug", e.getMessage());
                bundle.putBoolean("result",false);
            }
            catch (SocketTimeoutException e){
                bundle.putString("debug", "Превышено время ожидания ответа");
                bundle.putBoolean("result",false);
            }
            catch (Exception e) {
                bundle.putString("debug", e.getMessage());
                bundle.putBoolean("result",false);
            }
            finally {
                Message msg = Message.obtain(null,MSG_TO_START_RESULT);
                msg.setData(bundle);
                sendToActivity(msg);
            }
        }
    }
    private class SocketReaderRunnable implements Runnable{

        int reconnectionAttempts = 0;
        static int pintTimeOut = 5000;
        static void setPingTimeOut(int time){
            pintTimeOut = time;
        }
        Future<?> request;
        @Override
        public void run() {
            socketRequestRunnable = new SocketRequestRunnable(1000,100);
            request = executorService.submit(socketRequestRunnable);
            long lastPing = getTime();
            while (socket.isConnected() && !socket.isClosed()){
                try {
                    if (reader.ready()) {
                        lastPing = getTime();
                        Gson gson = new Gson();
                        String response = reader.readLine();
                        JsonObject jsonPackage = gson.fromJson(response, JsonObject.class);
                        Bundle bundle = new Bundle();
                        switch (jsonPackage.get("type").getAsInt()){
                            case 0 -> {
                                Log.d("P0", "ping");
                            }
                            case 1 -> {}
                            case 2 -> {
                                Message msg = Message.obtain(null,MSG_TO_MAIN_DATA_SCENE);
                                bundle.putString("json",response);
                                msg.setData(bundle);
                                //Log.d("P2",response);
                                sendToActivity(msg);
                            }
                            case 3 -> {
                                if(jsonPackage.get("simple").getAsBoolean()){
                                    bundle.putString("json",response);
                                    sendToActivity(MSG_TO_MAIN_SIMPLE_UPDATE,bundle);
                                }else{
                                    Log.d("P3", "not simple");
                                }
                                //Log.d("P3",response);
                            }
                        }
                    }
                    if (lastPing>getTime()) lastPing = getTime();
                    if (getTime() - lastPing > pintTimeOut) socket.close();
                }catch (Exception e){
                    Message msg = Message.obtain(null,MSG_TO_MAIN_EXCEPTION);
                    Bundle bundle = new Bundle();
                    bundle.putString("headDebug","Неизвестная ошибка");
                    bundle.putString("debug",e.getMessage());
                    msg.setData(bundle);
                    sendToActivity(msg);
                }
            }
            Message msgH = Message.obtain(null,MSG_TO_MAIN_HEAD);
            Bundle bundle = new Bundle();
            bundle.putString("head", "Переподключение...");
            msgH.setData(bundle);
            sendToActivity(msgH);
            if (reconnect()) run();
            else {
                Message msg = Message.obtain(null,MSG_STOP_SERVICE);
                sendToActivity(msg);
                stopSelf();
            }
        }
        private boolean reconnect(){
            try {
                socket.close();
                request.cancel(true);
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                socket.setSoTimeout(5000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = socket.getOutputStream();
                reconnectionAttempts = 0;
                Gson gson = new Gson();
                JsonObject json = new JsonObject();
                json.addProperty("type", 1);
                json.addProperty("password", password);
                String request = gson.toJson(json);
                sendJson(request);
                Message msgH = Message.obtain(null,MSG_TO_MAIN_HEAD);
                Bundle bundle = new Bundle();
                bundle.putString("head", "Подключено");
                msgH.setData(bundle);
                sendToActivity(msgH);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type",2);
                sendJson(jsonObject);
                return true;
            } catch (Exception e) {
                reconnectionAttempts++;
                Log.d("reconnect","attempt " + reconnectionAttempts);
                if (reconnectionAttempts>=10) {
                    return false;
                }
            }
            return reconnect();
        }
    }
    private class SocketRequestRunnable implements Runnable{
        private Timer timer = new Timer();
        Queue<JsonObject> out = new LinkedList<>();

        SocketRequestRunnable
        (long timePing, long timeUpdate){
            timer.scheduleAtFixedRate(new PingTask(),0, timePing);
            timer.scheduleAtFixedRate(new UpdateTask(),10, timeUpdate);
        }
        @Override
        public void run() {

        }
        public void stop(){

        }

        public void sendOnThread(JsonObject json) {
            out.add(json);
        }
        private class PingTask extends TimerTask {
            @Override
            public void run() {
                JsonObject json = new JsonObject();
                json.addProperty("type", 0);
                try {
                    sendJson(json);
                } catch (IOException e) {
                    Log.e("Request", e.getMessage());
                }
            }
        }
        private class UpdateTask extends TimerTask {
            @Override
            public void run() {
                JsonObject json = new JsonObject();
                json.addProperty("type", 3);
                //Log.d("Request", "Запрос");
                try {
                    sendJson(json);
                } catch (IOException e) {
                    Log.e("Request", e.getMessage());
                }
                while (!out.isEmpty() && socket.isConnected()){
                    try {
                        sendJson(out.poll());
                    } catch (IOException e) {
                        Log.e("sendWrite",e.getMessage());
                    }
                }
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("app", "NetService onCreate");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopForeground(false);
        Log.d("app", "NetService destroy");
    }
    private void sendJson(JsonObject json) throws IOException {
        Gson gson = new Gson();
        sendJson(gson.toJson(json));
    }
    private void sendJson(String ms) throws IOException {
        //Log.d("out", ms);
        output.write(ms.getBytes());
        output.write('\n');
        output.flush();
    }
    private void sendToActivity(int t, Bundle data){
        Message msg = Message.obtain(null,t);
        msg.setData(data);
        sendToActivity(msg);
    }
    private void sendToActivity(Message msg){
        try {
            toActivityMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e("Msg to activity", e.getMessage());
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        Messenger messenger = new Messenger(new ServiceHandler());
        return messenger.getBinder();
    }

}
