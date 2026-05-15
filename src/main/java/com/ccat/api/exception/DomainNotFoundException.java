package com.ccat.api.exception;

public class DomainNotFoundException extends RuntimeException {
    public DomainNotFoundException(String code) {
        super("Domain not found: " + code);
    }
}
