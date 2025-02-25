package com.example.cloudchat_user.dao;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.cloudchat_user.json.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class MaterialDao {
    static OkHttpClient okHttpClient = new OkHttpClient();
    static String baseURL = "http://47.94.207.38:9999/cloudchat_db_server-1.2/";

    static String tableUrl_m = "material";

    private MaterialDao(){}

    public static String[] get_url_remark(String id) throws IOException {
        String appendURL = "material?id="+id;
        Request request = new Request.Builder()
                .url(baseURL+appendURL)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    return new String[]{errorResponse.getError()};
                }else{
                    UrlRemarkResponse remarkResponse = gson.fromJson(string, UrlRemarkResponse.class);

                    return new String[]{remarkResponse.getMessage().getUrl(),remarkResponse.getMessage().getRemark()};
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        return new String[]{"ConnectionFailed"};
    }

    public static String delete_material(String id) throws IOException{
        String appendURL = "material?id="+id;
        Request request = new Request.Builder()
                .url(baseURL+appendURL)
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

    public static String set_url_remark(String url, String remark) throws IOException{
        String appendURL = "url="+url+"&remark="+remark;
        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
        RequestBody requestBody = RequestBody.create(appendURL.getBytes(StandardCharsets.UTF_8), mediaType);
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_m)
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
}
