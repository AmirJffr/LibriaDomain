package com.libria.exception;

public class BookNotFoundException extends LibriaException {
    public BookNotFoundException(String message) {
        super(message);
    }
}