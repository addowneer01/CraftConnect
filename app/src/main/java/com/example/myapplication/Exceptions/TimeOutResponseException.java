package com.example.myapplication.Exceptions;

import java.net.SocketTimeoutException;

public class TimeOutResponseException extends SocketTimeoutException {
    public TimeOutResponseException(){
        super("Превышено время ожидания пакета");
    }
}
