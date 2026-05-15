package com.ccat.api.exception;

public class FreeTestAlreadyUsedException extends RuntimeException {
    public FreeTestAlreadyUsedException() {
        super("Free diagnostic test has already been used. Upgrade to premium to continue practicing.");
    }
}
