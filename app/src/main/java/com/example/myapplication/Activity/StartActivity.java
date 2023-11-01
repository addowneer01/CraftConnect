package com.example.myapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Master;
import com.example.myapplication.R;
import com.example.myapplication.TypeMsg;

public class StartActivity extends AppCompatActivity implements TypeMsg {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        Master.setStartActivity(this);
        ip1 = findViewById(R.id.ip_plain_1);
        ip2 = findViewById(R.id.ip_plain_2);
        ip3 = findViewById(R.id.ip_plain_3);
        ip4 = findViewById(R.id.ip_plain_4);
        port = findViewById(R.id.port_plain);
        password = findViewById(R.id.password_plain);
        debug = findViewById(R.id.debug_text);
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    connectButton.setText("...");
                    connectButton.setEnabled(false);
                    String ip = ip1.getText().toString() + "." + ip2.getText().toString() + "." + ip3.getText().toString() + "." + ip4.getText().toString();
                    String portSt = port.getText().toString();
                    int portInt;
                    if (!portSt.equals(""))  portInt = Integer.parseInt(portSt);
                    else portInt = 8888;
                    String passwordS = password.getText().toString();
                    if (passwordS.equals(""))  passwordS = "0000";
                    Message msg = Message.obtain(null, MSG_TO_SERVICE_START);
                    Bundle data = new Bundle();
                    data.putString("ip",ip);
                    data.putInt("port",portInt);
                    data.putString("password",passwordS);
                    msg.setData(data);
                    Master.mainActivity.sendToService(msg);
                }
                catch (Exception e){
                    debug.setText(e.toString());
                    resetButton();
                }
            }
        });
        Intent intent = getIntent();
        String message = intent.getStringExtra("debug");
        setDebug(message);
    }
    public void resetButton(){
        connectButton.setText("Подключиться");
        connectButton.setEnabled(true);
    }
    public void setDebug(String msg){
        debug.setText(msg);
    }
    public void destroy(){
        finish();
    }
    private EditText ip1;
    private EditText ip2;
    private EditText ip3;
    private EditText ip4;
    private EditText port;
    private EditText password;
    private Button connectButton;
    private TextView debug;
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}