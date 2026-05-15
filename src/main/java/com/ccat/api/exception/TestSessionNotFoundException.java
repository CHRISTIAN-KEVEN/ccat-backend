package com.ccat.api.exception;

public class TestSessionNotFoundException extends RuntimeException {
    public TestSessionNotFoundException(Long id) {
        super("Test session not found: " + id);
    }
}
