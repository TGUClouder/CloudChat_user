package com.example.cloudchat_user.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    /**
     * 获取当前登录的用户名
     * @param context 上下文
     * @return 返回用户名，如果未登录则返回 null
     */
    public static String getCurrentUsername(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        return isLoggedIn ? username : null;
    }
}