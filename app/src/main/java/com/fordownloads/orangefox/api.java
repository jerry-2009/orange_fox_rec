package com.fordownloads.orangefox;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class api {
    public static String request(String reqUrl) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.orangefox.download/v2/" + reqUrl)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "[]";
    }
}
