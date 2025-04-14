package com.example.cloudchat_user.network;

import android.os.Handler;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class PhotoWebSocketManager {
    private static final String SERVER_URL = "ws://47.94.207.38:3000";
    private static final int RECONNECT_DELAY = 5000; // 5秒重连
    private static PhotoWebSocketManager instance;
    private WebSocketClient webSocketClient;
    private final Handler handler = new Handler();
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
                    Log.d("WebSocket", " WebSocket 连接成功");
                    isReconnecting = false;
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WebSocket", " 收到消息: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.w("WebSocket", " 连接关闭: " + reason);
                    if (!isManuallyClosed) {
                        reconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e("WebSocket", " 连接错误", ex);
                    if (!isManuallyClosed) {
                        reconnect();
                    }
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e("WebSocket", " 初始化失败", e);
            reconnect(); // 如果初始化失败，也尝试重连
        }
    }

    private void reconnect() {
        if (isReconnecting) return;

        isReconnecting = true;
        Log.d("WebSocket", " 准备在 " + RECONNECT_DELAY + "ms 后重连...");

        handler.postDelayed(() -> {
            Log.d("WebSocket", " 开始重连...");
            connectWebSocket();
        }, RECONNECT_DELAY);
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
            Log.d("WebSocket", " 发送消息: " + message);
        } else {
            Log.e("WebSocket", " WebSocket 未连接，无法发送消息");
        }
    }

    public void close() {
        isManuallyClosed = true;
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
