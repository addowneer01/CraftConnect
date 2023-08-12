package com.example.myapplication.Exceptions;

import java.io.IOException;

public class ConnectionBreakException extends IOException {
    public ConnectionBreakException(){
        super("Соединение разорвано");
    }
}
