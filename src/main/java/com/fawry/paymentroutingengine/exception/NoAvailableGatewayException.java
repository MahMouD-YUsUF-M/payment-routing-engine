package com.fawry.paymentroutingengine.exception;

public class NoAvailableGatewayException extends RuntimeException {
    public NoAvailableGatewayException(String message) {
        super(message);
    }
}