package com.fawry.paymentroutingengine.exception;

public class InsufficientQuotaException extends RuntimeException {
    public InsufficientQuotaException(String message) {
        super(message);
    }
}