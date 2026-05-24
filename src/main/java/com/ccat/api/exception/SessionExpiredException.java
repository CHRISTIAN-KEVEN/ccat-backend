package com.ccat.api.exception;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(Long sessionId) {
        super("Session " + sessionId + " expired and was auto-submitted");
    }
}
