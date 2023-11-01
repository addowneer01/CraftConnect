package com.example.myapplication;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.Activity.StartActivity;
import com.example.myapplication.Exceptions.ConnectionBreakException;
import com.example.myapplication.Exceptions.IncorrectPasswordException;
import com.example.myapplication.UIComponents.UIStringView;
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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master{
    private static Master master;
    public static MainActivity mainActivity;
    public static StartActivity startActivity;

    public static void setStartActivity(StartActivity startActivity) {
        Master.startActivity = startActivity;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        Master.mainActivity = mainActivity;
    }

    private Master(){

    }
    public static Master getInstance() {
        if (master == null){
            master = new Master();
        }
        return master;
    }
////////////////////////////////////////////////////////////////////////////////
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mainActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private long getTime(){
        final long[] time = new long[1];
        time[0] = System.currentTimeMillis();
        return time[0];
    }




////////////////////////////////////////////////////////////////////////////////
}
