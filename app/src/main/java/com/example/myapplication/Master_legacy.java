package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master_legacy {
    private static Master_legacy masterLegacy;
    public static Master_legacy getMaster() {
        if (masterLegacy == null){
                masterLegacy = new Master_legacy();
        }
        return masterLegacy;
    }
    public EditText ipField;
    public EditText portField;
    public EditText messageField;
    public Button connectButton;
    public Button sendButton;
    public TextView statusView;
    public TextView messageView;

    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Socket socket;
    private BufferedReader reader;
    private OutputStream output;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private Timer timer;

    public void pressConnect() {
        executorService.execute(new ConnectRunnable());
        executorService.execute(new InRunnable());
    }

    public void pressSend(){
        executorService.execute(new SendRunnable());
    }

    private class SendRunnable implements Runnable {
        @Override
        public void run() {
            String message = messageField.getText().toString();
            messageField.setText("");
            Gson gson = new Gson();
            JsonObject json = new JsonObject();
            json.addProperty("user", "root");
            json.addProperty("password", "123");
            json.addProperty("message", message);
            String request = gson.toJson(json);
            try {
                output.write(request.getBytes());
                output.write('\n');
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class InRunnable implements Runnable {
        @Override
        public void run() {
            try {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        try {
                            String response = reader.readLine();
                            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                            if (jsonResponse != null) {
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusView.setText(jsonResponse.get("status").getAsString());
                                        messageView.setText(jsonResponse.get("message").getAsString());
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer = new Timer();
                timer.scheduleAtFixedRate(task, 0, 1000);
            } catch (Exception e) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageView.setText(e.getMessage());
                    }
                });
            }
        }
    }

    private class ConnectRunnable implements Runnable {
        @Override
        public void run() {
            try {
                socket = new Socket(ipField.getText().toString(), Integer.parseInt(portField.getText().toString()));
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setText("Connected to Arduino");
                    }
                });
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = socket.getOutputStream();
            } catch (Exception e) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setText("exception");
                        messageView.setText(e.toString());
                    }
                });
            }
        }
    }
}
