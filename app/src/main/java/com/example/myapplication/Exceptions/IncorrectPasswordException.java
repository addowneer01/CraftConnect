package com.example.myapplication.Exceptions;

public class IncorrectPasswordException extends RuntimeException{
    public IncorrectPasswordException(){
        super("Неверный пароль");
    }

}
