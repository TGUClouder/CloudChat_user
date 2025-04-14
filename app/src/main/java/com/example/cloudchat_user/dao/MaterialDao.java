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
                .url(baseURL+tableUrl_m)
                .build();
        HashMap<String, ArrayList<String>> hashMap =new HashMap<>();
        ArrayList<String> arrayList = new ArrayList<>();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    arrayList.add(errorResponse.getError());
                    hashMap.put("error", arrayList);
                    return hashMap;
                }else{

                    JSONObject jsonObject = new JSONObject(string);
                    if(jsonObject.isNull("message")){
                        arrayList.add("null");
                        hashMap.put("null",arrayList);
                        return hashMap;
                    }
                    else{
                        JSONObject jsonObject1 = jsonObject.getJSONObject("message");
                        for (Iterator<String> it = jsonObject1.keys(); it.hasNext(); ) {
                            ArrayList<String> arrayList_ = new ArrayList<>();
                            String id = it.next();
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("id");
                            arrayList_.add(jsonObject2.getString("url"));
                            arrayList_.add(jsonObject2.getString("remark"));
                            arrayList_.add(jsonObject2.getString("status"));
                            hashMap.put(id, arrayList_);
                        }
                        return hashMap;
                    }
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }

        arrayList.add("ConnectionFailed");
        hashMap.put("error",arrayList);
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
