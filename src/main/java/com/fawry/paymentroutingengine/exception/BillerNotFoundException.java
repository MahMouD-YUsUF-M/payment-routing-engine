package com.fawry.paymentroutingengine.exception;

public class BillerNotFoundException extends RuntimeException {
    public BillerNotFoundException(String message) {
        super(message);
    }
}