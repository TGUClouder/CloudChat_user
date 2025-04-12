package com.example.cloudchat_user.dao;

import com.example.cloudchat_user.json.ErrorResponse;
import com.example.cloudchat_user.json.IdResponse;
import com.example.cloudchat_user.json.StatusResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class ReplayDao {
    static OkHttpClient okHttpClient = new OkHttpClient();
    static String baseURL = "http://47.94.207.38:9999/cloudchat_db_server-1.2/";

    static String tableUrl_l = "replay";
    private ReplayDao(){}

    // 获取所有链接
    public static long get_mil_sec(String section) throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_l+"?a="+section)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                if(string.contains("error")){ // 说明没有
                    return -1;
                }else{
                    JSONObject jsonObject = new JSONObject(string);
                    return jsonObject.getLong("message");

                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }

        return -2;
    }

    public static String set_replay(String section, String mil_sec) throws IOException{
        String appendURL = "a="+section+"&b="+mil_sec;
        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
        RequestBody requestBody = RequestBody.create(appendURL.getBytes(StandardCharsets.UTF_8), mediaType);
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_l)
                .post(requestBody)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    return errorResponse.getError();
                }else{
                    IdResponse idResponse = gson.fromJson(string, IdResponse.class);

                    return idResponse.getMessage().getId();
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        return "ConnectionFailed";
    }

    public static String update_replay(String section, String mil_sec) throws IOException{
        String appendURL = tableUrl_l+"?a="+section+"&b="+mil_sec;
        Request request = new Request.Builder()
                .url(baseURL+appendURL)
                .put(RequestBody.create("",null))
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    return errorResponse.getError();
                }else{
                    StatusResponse statusResponse = gson.fromJson(string, StatusResponse.class);

                    return statusResponse.getMessage().getStatus();
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        return "ConnectionFailed";
    }
}
