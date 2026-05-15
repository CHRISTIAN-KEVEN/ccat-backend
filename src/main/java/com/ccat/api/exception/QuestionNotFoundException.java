package com.ccat.api.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String uuid) {
        super("Question not found: " + uuid);
    }
}
