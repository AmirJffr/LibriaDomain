package com.libria.exception;

public class UserNotFoundException extends LibriaException {
    public UserNotFoundException(String message) {
        super(message);
    }
}