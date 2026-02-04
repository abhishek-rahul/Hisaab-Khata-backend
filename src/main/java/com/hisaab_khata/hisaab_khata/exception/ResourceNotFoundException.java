package com.hisaab_khata.hisaab_khata.exception;


public class ResourceNotFoundException extends RuntimeException {

    private final String code;

    public ResourceNotFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

