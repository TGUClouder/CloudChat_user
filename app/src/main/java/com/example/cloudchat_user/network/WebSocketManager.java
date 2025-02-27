package com.example.cloudchat_user.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager {

    private static WebSocketManager instance;
    private WebSocket webSocket;
    private OkHttpClient client;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String TAG = "WebSocketManager";
    private boolean isConnected = false;

    // 接收消息的回调接口
    public interface WebSocketListenerInterface {
        void onMessage(String text);
        void onOpen();
        void onBinaryMessage(ByteString bytes);
        void onFailure(Throwable t);
        void onClosed();  // 添加关闭回调
    }
    private WebSocketListenerInterface listenerInterface;

    public static WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    private WebSocketManager() {
        client = new OkHttpClient();
    }

    // 设置回调监听
    public void setListener(WebSocketListenerInterface listenerInterface) {
        this.listenerInterface = listenerInterface;
    }

    // 连接 WebSocket 服务器
    public void connect(String url) {
        if (isConnected) {
            return;  // 如果已经连接，则不重复连接
        }

        android.util.Log.d(TAG, "Attempting to connect to: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                isConnected = true;
                android.util.Log.d(TAG, "WebSocket connection opened");
                mainHandler.post(() -> {
                    if (listenerInterface != null) {
                        listenerInterface.onOpen();
                    }
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                mainHandler.post(() -> {
                    if (listenerInterface != null) {
                        listenerInterface.onMessage(text);
                    }
                });
            }
            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                mainHandler.post(() -> {
                    if (listenerInterface != null) {
                        listenerInterface.onBinaryMessage(bytes);
                    }
                });
            }


            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                android.util.Log.d(TAG, "WebSocket closing: " + reason);
                webSocket.close(code, reason);
                mainHandler.post(() -> {
                    if (listenerInterface != null) {
                        listenerInterface.onClosed();
                    }
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable okhttp3.Response response) {
                isConnected = false;
                android.util.Log.e(TAG, "WebSocket failure: " + t.getMessage());
                mainHandler.post(() -> {
                    if (listenerInterface != null) {
                        listenerInterface.onFailure(t);
                    }
                });
            }
        });
    }
    public void sendBinaryMessage(byte[] data) {
        if (webSocket != null && webSocket.send(ByteString.of(data))) {
            Log.d("WebSocketManager", "二进制图片已发送，大小: " + data.length);
        } else {
            Log.e("WebSocketManager", "WebSocket 未连接，无法发送二进制数据");
        }
    }
    // 发送消息
    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    // 关闭连接
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing");
        }
    }
    //二进制图片
    public void sendBinary(ByteString bytes) {
        if (webSocket != null) {
            webSocket.send(bytes);
        }
    }
    // 添加获取连接状态的方法
    public boolean isConnected() {
        return isConnected;
    }
}