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

public final class DiaryDao {
    static OkHttpClient okHttpClient = new OkHttpClient();
    static String baseURL = "http://47.94.207.38:9999/cloudchat_db_server-1.2/";

    static String tableUrl_v = "diary";

    // 根据用户名获取日记内容
    public static String get_diary(String name) throws IOException, JSONException {
        String appendURL = "?name="+name;

        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v+appendURL)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    return errorResponse.getError();
                }else{
                    JSONObject jsonObject = new JSONObject(string);
                    JSONObject jsonMessage = jsonObject.getJSONObject("message");

                    return jsonMessage.getString("content");
                    }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        return "ConnectionFailed";
    }

    // 新增日记返回id
    public static String set_diary(String username, String content) throws IOException {
        String appendURL = "?accountName=" + username + "&content=" + content;
        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
        RequestBody requestBody = RequestBody.create(appendURL.getBytes(StandardCharsets.UTF_8), mediaType);

        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v)
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

    // 根据用户名删除日记,返回状态
    public static String delete_diary(String username) throws IOException{
        String appendURL = "?username="+username;
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v+appendURL)
                .delete()
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

    // 更新日记,返回状态
    public static String update_diary(String username, String content) throws IOException{
        String appendURL = "?username="+username+"&content="+content;

        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v+appendURL)
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
