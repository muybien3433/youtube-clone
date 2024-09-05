package com.muybien.youtube_clone.handler;

public class InvalidFileUrlException extends RuntimeException {
    public InvalidFileUrlException(String message) {
        super(message);
    }
}
