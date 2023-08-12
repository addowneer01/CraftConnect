package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Exceptions.ConnectionBreakException;
import com.example.myapplication.Exceptions.IncorrectPasswordException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master {
    private static Master master;
    private static MainActivity mainActivity;
    private static StartScene startScene;
    private Master(){
        startScene = new StartScene();
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
    String ip;
    int port;
    String password;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private BufferedReader reader;
    private OutputStream output;

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
                socket = new Socket(ip,port);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = socket.getOutputStream();
                Gson gson = new Gson();
                JsonObject json = new JsonObject();
                json.addProperty("type", 0);
                json.addProperty("password", password);
                String request = gson.toJson(json);
                output.write(request.getBytes());
                output.write('\n');
                output.flush();
                gson = new Gson();
                JsonObject jsonResponse = null;
                while (jsonResponse == null && socket.isConnected()){
                    String response = reader.readLine();
                    jsonResponse = gson.fromJson(response, JsonObject.class);
                }
                if(socket.isClosed()) throw new ConnectionBreakException();
                debugMs = jsonResponse.get("debugMs").getAsString();
                if(!jsonResponse.get("status").getAsString().equals("success")) throw new IncorrectPasswordException();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScene.resetButton();
                        mainActivity.setViewMain();
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
    private class StartScene{
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
}
