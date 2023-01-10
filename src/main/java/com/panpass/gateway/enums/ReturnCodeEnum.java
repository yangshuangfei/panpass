package com.panpass.gateway.enums;

public enum ReturnCodeEnum {
    SUCCESS(200,"SUCCESS"),
    FAIL(1,"FAIL");


    private int code;

    private String msg;


    ReturnCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
