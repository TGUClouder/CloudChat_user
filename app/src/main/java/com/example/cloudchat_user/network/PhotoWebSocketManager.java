package com.example.cloudchat_user.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class PhotoWebSocketManager {
    private static final String SERVER_URL = "ws://47.94.207.38:3000";
    private static final int RECONNECT_DELAY = 5000; // 5秒重连
    private static PhotoWebSocketManager instance;
    private WebSocketClient webSocketClient;
    private final Handler handler = new Handler(Looper.getMainLooper()); // 主线程 Handler
    private boolean isManuallyClosed = false;
    private boolean isReconnecting = false;

    private PhotoWebSocketManager() {
        connectWebSocket();
    }

    public static synchronized PhotoWebSocketManager getInstance() {
        if (instance == null) {
            instance = new PhotoWebSocketManager();
        }
        return instance;
    }

    private void connectWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI(SERVER_URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("WebSocket", "连接成功");
                    isReconnecting = false;
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WebSocket", "收到消息: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.w("WebSocket", "连接关闭: " + reason);
                    if (!isManuallyClosed) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e("WebSocket", "连接错误", ex);
                    if (!isManuallyClosed) {
                        scheduleReconnect();
                    }
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e("WebSocket", "初始化失败", e);
            scheduleReconnect(); // 异常也安排重连
        }
    }

    private void scheduleReconnect() {
        if (isReconnecting) return;

        isReconnecting = true;
        Log.d("WebSocket", "将在 " + RECONNECT_DELAY + "ms 后尝试重连...");

        handler.postDelayed(() -> {
            Log.d("WebSocket", "执行重连...");
            connectWebSocket();
        }, RECONNECT_DELAY);
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
            Log.d("WebSocket", "发送消息: " + message);
        } else {
            Log.e("WebSocket", "WebSocket 未连接，无法发送消息");
        }
    }

    public void close() {
        isManuallyClosed = true;
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
