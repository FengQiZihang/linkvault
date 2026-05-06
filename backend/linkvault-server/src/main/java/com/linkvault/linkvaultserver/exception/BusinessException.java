package com.linkvault.linkvaultserver.exception;

public class BusinessException extends RuntimeException {
    private final int httpStatus;
    private final int code;
    private final String msg;


    public BusinessException(int httpStatus, int code, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
        this.code = code;
        this.msg = msg;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
