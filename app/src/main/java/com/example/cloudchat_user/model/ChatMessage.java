package com.example.cloudchat_user.model;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage {
    private String content;
    private boolean isSent;
    private String imageUrl;
    private Bitmap imageBitmap;
    private String time;  // 新增字段用于存储时间

    public ChatMessage(String content, boolean isSent) {
        this.content = content;
        this.isSent = isSent;
        this.time = getCurrentTime();  // 初始化时自动设置时间
    }

    // 获取当前时间，精确到分钟
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date());
    }

    public String getContent() {
        return content;
    }

    public boolean isSent() {
        return isSent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    // 获取图片 Bitmap
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    // 获取时间
    public String getTime() {
        return time;
    }

    // 设置时间（如果需要手动更改时间）
    public void setTime(String time) {
        this.time = time;
    }
}
