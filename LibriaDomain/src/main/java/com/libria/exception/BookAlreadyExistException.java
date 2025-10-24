package com.libria.exception;

public class BookAlreadyExistException extends LibriaException {
    public BookAlreadyExistException(String message) {
        super(message);
    }
}