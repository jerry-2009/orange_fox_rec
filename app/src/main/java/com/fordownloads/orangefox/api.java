package com.fordownloads.orangefox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class api {
    public static Map<String, Object> request(String reqUrl) {
        Map<String, Object> map = new HashMap<>();

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.orangefox.download/v2/" + reqUrl)
                    .build();
            Response response = client.newCall(request).execute();

            map.put("success", response.isSuccessful());
            map.put("code", response.code());
            map.put("response", response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            map.put("success", false);
        }

        return map;
    }
}
