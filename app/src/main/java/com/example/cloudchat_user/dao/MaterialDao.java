package com.example.cloudchat_user.dao;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.cloudchat_user.json.*;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public final class MaterialDao {
    static OkHttpClient okHttpClient = new OkHttpClient();
    static String baseURL = "http://47.94.207.38:9999/cloudchat_db_server-1.2/";
    static String tableUrl_m = "material";
    private MaterialDao(){}

    // 获取所有链接
    public static HashMap<String, ArrayList<String>> get_all_res() throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(baseURL + tableUrl_m)
                .build();
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if (string.contains("error")) {
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    ArrayList<String> errorList = new ArrayList<>();
                    errorList.add(errorResponse.getError());
                    hashMap.put("error", errorList);
                    return hashMap;
                } else {
                    JSONObject jsonObject = new JSONObject(string);
                    if (jsonObject.isNull("message")) {
                        ArrayList<String> nullList = new ArrayList<>();
                        nullList.add("null");
                        hashMap.put("null", nullList);
                        return hashMap;
                    } else {
                        JSONObject message = jsonObject.getJSONObject("message");
                        for (Iterator<String> it = message.keys(); it.hasNext(); ) {
                            String id = it.next();
                            JSONObject orderData = message.getJSONObject(id);

                            // 获取 URL、remark 和 status
                            ArrayList<String> orderDetails = new ArrayList<>();
                            orderDetails.add(orderData.optString("url", "no_url"));
                            orderDetails.add(orderData.optString("remark", "no_remark"));
                            orderDetails.add(orderData.optString("status", "未接单"));

                            // 将数据存入结果哈希表
                            hashMap.put(id, orderDetails);
                        }
                        return hashMap;
                    }
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }

        ArrayList<String> connectionFailed = new ArrayList<>();
        connectionFailed.add("ConnectionFailed");
        hashMap.put("error", connectionFailed);
        return hashMap;
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

    public static String update_status(String id) throws IOException{
        String appendURL = "material?id="+id;
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
