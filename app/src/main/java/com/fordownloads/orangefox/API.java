package com.fordownloads.orangefox;

import android.app.Activity;

import com.fordownloads.orangefox.ui.Tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class API {
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
            map.put("code", 0);
            map.put("success", false);
        }

        return map;
    }

    public static void errorHandler(Activity context, Map<String, Object> response, int customErr){
        if (!(boolean) response.get("success")) {
            int code = (int) response.get("code");
            switch (code) {
                case 404:
                case 500:
                    Tools.dialogFinish(context, customErr);
                    break;
                case 0:
                    Tools.dialogFinish(context, R.string.err_no_internet);
                    break;
                default:
                    Tools.dialogFinish(context, context.getString(R.string.err_response, code));
                    break;
            }
        }
    }
}
