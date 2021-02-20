package com.fordownloads.orangefox.utils;

public class APIResponse {
    final public String data;
    final public int code;
    final public boolean success;

    public APIResponse(boolean success, int code, String data) {
        this.code = code;
        this.success = success;
        this.data = data;
    }

    public APIResponse() {
        this.code = 0;
        this.success = false;
        this.data = "";
    }
}
