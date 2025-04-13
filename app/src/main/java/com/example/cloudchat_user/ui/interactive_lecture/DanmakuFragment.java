package com.example.cloudchat_user.ui.interactive_lecture;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cloudchat_user.R;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.*;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

public class DanmakuFragment extends Fragment {

    private static final String TAG = "DanmakuFragment";

    private Switch switchDanmu;
    private EditText etDanmuInput;
    private Button btnSendDanmu;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;

    private WebSocket webSocket;
    private OkHttpClient client;

    private static final String WS_URL = "ws://47.94.207.38:6000";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danmaku, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchDanmu = view.findViewById(R.id.switch_danmu);
        etDanmuInput = view.findViewById(R.id.et_danmu_input);
        btnSendDanmu = view.findViewById(R.id.btn_send_danmu);
        danmakuView = view.findViewById(R.id.danmaku_view);

        setupDanmaku();
        connectWebSocket();

        btnSendDanmu.setOnClickListener(v -> sendDanmu());
    }

    private void setupDanmaku() {
        Log.d(TAG, "Initializing Danmaku...");
        danmakuContext = DanmakuContext.create();
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                danmakuView.start();
                Log.d(TAG, "DanmakuView started.");
            }

            @Override public void updateTimer(DanmakuTimer danmakuTimer) {}
            @Override public void drawingFinished() {}
            @Override public void danmakuShown(BaseDanmaku danmaku) {}
        });

        danmakuView.prepare(new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        }, danmakuContext);

        danmakuView.enableDanmakuDrawingCache(true);
        Log.d(TAG, "Danmaku initialized.");
    }

    private void connectWebSocket() {
        client = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(WS_URL).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connected.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 只有当弹幕开关开启时，才接收和显示弹幕
                if (switchDanmu.isChecked()) {
                    Log.d(TAG, "Received danmaku: " + text);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> addDanmaku(text, false));
                    }
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d(TAG, "WebSocket closing: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket error: ", t);
            }
        });
    }

    private void sendDanmu() {
        String danmuText = etDanmuInput.getText().toString().trim();

        // 如果弹幕开关关闭或文本为空，则不发送弹幕
        if (!switchDanmu.isChecked() || TextUtils.isEmpty(danmuText)) {
            Log.d(TAG, "Danmu not sent - switch off or text empty.");
            return;
        }

        // 发送弹幕到服务器
        if (webSocket != null) {
            webSocket.send(danmuText);
            etDanmuInput.setText("");  // 发送后清空输入框
        }
    }

    private void addDanmaku(String text, boolean isSelf) {
        // 如果弹幕开关关闭，则不显示弹幕
        if (!switchDanmu.isChecked()) {
            return;
        }

        if (danmakuView == null || !danmakuView.isPrepared()) {
            Log.d(TAG, "DanmakuView is not prepared.");
            return;
        }

        // 创建弹幕
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null) return;

        // 设置弹幕属性
        danmaku.text = text;
        danmaku.textSize = 60f;
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        danmaku.priority = 1;
        danmaku.borderColor = isSelf ? Color.YELLOW : Color.TRANSPARENT;

        danmakuView.addDanmaku(danmaku);
        Log.d(TAG, "Danmaku added: " + text);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (danmakuView != null) {
            danmakuView.release();
        }
        if (webSocket != null) {
            webSocket.close(1000, "Fragment destroyed");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}

