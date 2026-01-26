package com.fawry.paymentroutingengine.exception;

public class NoViableGatewayException extends RuntimeException {
    public NoViableGatewayException(String message) {
        super(message);
    }
}