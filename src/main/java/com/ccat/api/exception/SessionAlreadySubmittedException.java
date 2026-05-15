package com.ccat.api.exception;

public class SessionAlreadySubmittedException extends RuntimeException {
    public SessionAlreadySubmittedException(Long sessionId) {
        super("Session " + sessionId + " is already closed");
    }
}
