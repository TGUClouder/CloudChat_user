package com.example.cloudchat_user.dao;

import com.example.cloudchat_user.json.ErrorResponse;
import com.example.cloudchat_user.json.IdResponse;
import com.example.cloudchat_user.json.StatusResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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
    public static String get_my_diary(String name) throws IOException, JSONException {
        String appendURL = "?option=mine&name="+name;

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

    // 获取所有日记
    public static ArrayList<HashMap<String, String>>  get_all_diary() throws IOException, JSONException {
        String appendURL = "?option=all&name=null";

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v+appendURL)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("error", errorResponse.getError());
                    arrayList.add(hashMap);
                }else{
                    JSONObject jsonObject = new JSONObject(string);
                    JSONObject jsonMessage = jsonObject.getJSONObject("message");
                    for (Iterator<String> it = jsonMessage.keys(); it.hasNext(); ) {
                        HashMap<String, String> hashMap = new HashMap<>();
                        String id = it.next();
                        JSONObject userDetails = jsonMessage.getJSONObject(id);
                        hashMap.put("diary", userDetails.getString("diary"));
                        hashMap.put("first_name", userDetails.getString("first_name"));
                        hashMap.put("last_name", userDetails.getString("last_name"));
                        hashMap.put("hobby", userDetails.getString("hobby"));
                        hashMap.put("gender", userDetails.getString("gender"));
                        hashMap.put("degree", userDetails.getString("degree"));
                        hashMap.put("grade", userDetails.getString("grade"));
                        hashMap.put("birthday", userDetails.getString("birthday"));
                        arrayList.add(hashMap);
                    }

                }
                return arrayList;

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("error", "ConnectionFailed");
        arrayList.add(hashMap);
        return arrayList;
    }

    // 新增日记返回id
    public static String set_diary(String username, String content) throws IOException {
        String appendURL = "accountName=" + username + "&content=" + content;
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
