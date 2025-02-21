package com.example.cloudchat_user.dao;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.cloudchat_user.json.*;
import com.google.gson.JsonSyntaxException;

public final class UserDao {

    static OkHttpClient okHttpClient = new OkHttpClient();
    static String baseURL = "http://47.94.207.38:9999/cloudchat_db_server-1.2/";
    static String tableUrl_info = "user_info";
    static String tableUrl_account = "user_account";

    private UserDao(){}


    // 验证密码
    public static String get_password(String username) throws IOException {
        String appendURL = "user_account?accountType=child&accountName="+username;
        Request request = new Request.Builder()
                .url(baseURL+appendURL)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    return errorResponse.getError();
                }else{
                    PasswordResponse passwordResponse = gson.fromJson(string, PasswordResponse.class);

                    return passwordResponse.getMessage().getPassword();
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        return "ConnectionFailed";
    }

    // 获取用户信息
    public static ArrayList<String> get_userinfo(String username)throws IOException{
        ArrayList<String> arrayList = new ArrayList<>();
        String appendURL = "user_info?accountName="+username;
        Request request = new Request.Builder()
                .url(baseURL+appendURL)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    arrayList.add(errorResponse.getError());
                    return arrayList;
                }else{
                    UserInfoResponse userInfoResponse = gson.fromJson(string, UserInfoResponse.class);
                    UserInfoResponse.Message  message = userInfoResponse.getMessage();
                    arrayList.add(message.getId());
                    arrayList.add(message.getFirst_name());
                    arrayList.add(message.getLast_name());
                    arrayList.add(message.getAge());
                    arrayList.add(message.getEmail());
                    arrayList.add(message.getGender());
                    arrayList.add(message.getDegree());
                    arrayList.add(message.getBirthday());
                    arrayList.add(message.getHobby());
                    arrayList.add(message.getGrade());

                    return arrayList;
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        arrayList.add("ConnectionFailed");
        return arrayList;

    }

    // 删除帐号
    public static String delete_account(String username) throws IOException {
        String appendURL = "user_account?accountType=child&accountName="+username;
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

    // 更新密码
    public static String update_password(String username, String new_password) throws IOException {
        String appendURL = "accountType=child&accountName=" + username + "&password=" + new_password;
        Request request = new Request.Builder()
                .url(baseURL + tableUrl_account + "?" + appendURL)
                .put(RequestBody.create("", null))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string().trim();
                if (string.isEmpty()) {
                    System.out.println("Server returned empty response");
                    return "EmptyResponse";
                }
                try {
                    Gson gson = new Gson();
                    if (string.contains("error")) {
                        ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                        return errorResponse.getError();
                    } else {
                        StatusResponse statusResponse = gson.fromJson(string, StatusResponse.class);
                        return statusResponse.getMessage().getStatus();
                    }
                } catch (JsonSyntaxException e) {
                    System.out.println("JSON Parsing Error: " + e.getMessage());
                    System.out.println("Server Response: " + string);
                    return "InvalidJSONResponse";
                }

            } else {
                System.out.println("Request failed: " + response.code());
                return "HTTP_" + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "ConnectionFailed";
        }
    }


    // 注册帐号
    public static String signup(ArrayList<String> arrayList) throws IOException {
        String appendURL = getString(arrayList);
        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
        RequestBody requestBody = RequestBody.create(appendURL.getBytes(StandardCharsets.UTF_8), mediaType);
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_info)
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
    @NonNull
    private static String getString(ArrayList<String> arrayList) {
        try {
            String name = arrayList.get(0);
            String password = arrayList.get(1);
            String f_name = arrayList.get(2);
            String l_name = arrayList.get(3);
            String age = arrayList.get(4);
            String email = arrayList.get(5);
            String gender = arrayList.get(6);
            String degree = arrayList.get(7);
            String birthday = arrayList.get(8);
            String hobby = arrayList.get(9);
            String grade = arrayList.get(10);


            return "name=" + URLEncoder.encode(name, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8") +
                    "&accountType=child" +
                    "&f=" + URLEncoder.encode(f_name, "UTF-8") +
                    "&l=" + URLEncoder.encode(l_name, "UTF-8") +
                    "&a=" + URLEncoder.encode(age, "UTF-8") +
                    "&e=" + URLEncoder.encode(email, "UTF-8") +
                    "&g=" + URLEncoder.encode(gender, "UTF-8") +
                    "&d=" + URLEncoder.encode(degree, "UTF-8") +
                    "&b=" + URLEncoder.encode(birthday, "UTF-8") +
                    "&h=" + URLEncoder.encode(hobby, "UTF-8") +
                    "&grade=" + URLEncoder.encode(grade, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("UserDao", "URL编码异常", e);
            return "";
        }
    }

    // 更新用户信息
    public static String update_userinfo(ArrayList<String> arrayList) throws IOException {
        String appendURL = getAppendURL(arrayList);
//        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
//        RequestBody requestBody = RequestBody.create(appendURL.getBytes(StandardCharsets.UTF_8), mediaType);
        Request request = new Request.Builder()
                .url(baseURL+tableUrl_info+"?"+appendURL)
                .put(RequestBody.create("", null))
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                String string = response.body().string();
                Gson gson = new Gson();
                if(string.contains("error")){
                    ErrorResponse errorResponse = gson.fromJson(string, ErrorResponse.class);
                    return errorResponse.getError();
                }else{
                    NullMessageResponse statusResponse = gson.fromJson(string, NullMessageResponse.class);

                    return statusResponse.getMessage();
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
        return "ConnectionFailed";
    }
    @NonNull
    private static String getAppendURL(ArrayList<String> arrayList) {
        String username = arrayList.get(0);
        String f_name = arrayList.get(1);
        String l_name = arrayList.get(2);
        String age = arrayList.get(3);
        String email = arrayList.get(4);
        String gender = arrayList.get(5);
        String degree = arrayList.get(6);
        String birthday = arrayList.get(7);
        String hobby = arrayList.get(8);
        String grade = arrayList.get(9);
        return "name="+username+"&type=child&f="+f_name+"&l="+l_name+"&a="+age+"&e="+email+"&g="+gender+"&d="+degree+"&b="+birthday+"&h="+hobby+"&grade="+grade;
    }

    public static void shutdown(){}

}
