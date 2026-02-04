package com.hisaab_khata.hisaab_khata.exception;


public class BusinessValidationException extends RuntimeException {

    private final String code;

    public BusinessValidationException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

