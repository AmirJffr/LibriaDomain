package com.libria.exception;


public class UserAlreadyExistException extends LibriaException {
    public UserAlreadyExistException(String message) {
        super(message);
    }
}