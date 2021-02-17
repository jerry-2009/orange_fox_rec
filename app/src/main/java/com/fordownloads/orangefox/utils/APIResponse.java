package com.fordownloads.orangefox.utils;

public class APIResponse {
    final public String response;
    final public int code;
    final public boolean success;

    public APIResponse(boolean success, int code, String response) {
        this.code = code;
        this.success = success;
        this.response = response;
    }

    public APIResponse() {
        this.code = 0;
        this.success = false;
        this.response = "";
    }
}
