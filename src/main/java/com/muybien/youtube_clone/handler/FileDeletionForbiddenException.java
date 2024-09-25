package com.muybien.youtube_clone.handler;

public class FileDeletionForbiddenException extends RuntimeException {
    public FileDeletionForbiddenException(String message) {
        super(message);
    }
}
