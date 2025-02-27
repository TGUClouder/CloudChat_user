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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class VotesDao {
    static OkHttpClient okHttpClient = new OkHttpClient();
    static String baseURL = "http://47.94.207.38:9999/cloudchat_db_server-1.2/";

    static String tableUrl_v = "votes";

    // 获取所有投票信息
    public static HashMap<String, ArrayList<String>> get_all_votes() throws IOException, JSONException {

        HashMap<String, ArrayList<String>> map = new HashMap<>();
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ArrayList<String> arrayList = new ArrayList<>();
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    arrayList.add(errorResponse.getError());
                    map.put("error", arrayList);
                }else{
                    JSONObject jsonObject = new JSONObject(string);
                    JSONObject jsonMessage = jsonObject.getJSONObject("message");
                    for (Iterator<String> it = jsonMessage.keys(); it.hasNext(); ) {
                        ArrayList<String> arrayList = new ArrayList<>();
                        String voteID = it.next();
                        JSONObject voteDetails = jsonMessage.getJSONObject(voteID);

                        // 提取具体字段
                        String username = voteDetails.getString("username");
                        String name = voteDetails.getString("name");
                        String details = voteDetails.getString("details");
                        String pros = voteDetails.getString("pros");
                        String start = voteDetails.getString("start");
                        String end = voteDetails.getString("end");
                        arrayList.add(name);
                        arrayList.add(username);
                        arrayList.add(details);
                        arrayList.add(pros);
                        arrayList.add(start);
                        arrayList.add(end);
                        map.put(voteID, arrayList);

                    }
                }
                return map;

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("ConnectionFailed");
        map.put("connectionError", arrayList);
        return map;
    }

    // 新增投票返回id
    public static String set_vote(String username, String vote_name, String detail) throws IOException {
        String appendURL = "accountName=" + username + "&voteName=" + vote_name + "&detail=" + detail;
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

    // 根据投票id删除投票项目,返回状态
    public static String delete_vote(String id) throws IOException{
        String appendURL = "votes?id="+id;
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

    // 系统更新支持人数,返回状态
    public static String update_pros(String id) throws IOException{
        String appendURL = "id="+id+"&pro=true&detail=null&username=null";

        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v+"?"+appendURL)
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

    // 志愿者更新投票细节
    public static String update_details(String id,String details, String username) throws IOException{
        String appendURL = "id="+id+"&pro=null&detail="+details+"&username="+username;

        Request request = new Request.Builder()
                .url(baseURL+tableUrl_v+"?"+appendURL)
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