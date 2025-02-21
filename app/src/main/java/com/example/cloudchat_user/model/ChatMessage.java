package com.example.cloudchat_user.model;

import android.graphics.Bitmap;

public class ChatMessage {
    private String content;
    private boolean isSent;
    private String imageUrl;
    private Bitmap imageBitmap;

    public ChatMessage(String content, boolean isSent) {
        this.content = content;
        this.isSent = isSent;
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
    // 新增：获取图片 Bitmap
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

} 