package com.fordownloads.orangefox.utils;

import org.jetbrains.annotations.NotNull;

public class APIResponse {
    final public String url;
    final public String data;
    final public int code;
    final public boolean success;

    public APIResponse(String url, boolean success, int code, String data) {
        this.code = code;
        this.success = success;
        this.data = data;
        this.url = url;
    }

    public APIResponse(String url) {
        this.code = 0;
        this.success = false;
        this.data = "";
        this.url = url;
    }

    @NotNull
    @Override
    public String toString() {
        return code + " (" + url + ")";
    }
}
