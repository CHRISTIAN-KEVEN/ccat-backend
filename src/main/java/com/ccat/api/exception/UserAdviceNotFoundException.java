package com.ccat.api.exception;

public class UserAdviceNotFoundException extends RuntimeException {
    public UserAdviceNotFoundException(Long id) {
        super("Advice not found: " + id);
    }
}
