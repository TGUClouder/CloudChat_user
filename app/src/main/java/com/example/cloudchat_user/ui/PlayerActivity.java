package com.example.cloudchat_user.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.extractor.flv.FlvExtractor;
import androidx.media3.ui.PlayerView;

import com.example.cloudchat_user.R;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private String FLV_URL = "http://59.110.173.159:8080/live/livestream.flv";

    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private EditText etDanmuInput;
    private Button btnSendDanmu;

    private WebSocket webSocket;
    private OkHttpClient client;
    private LinearLayout danmakuInputArea;
    private Handler hideHandler = new Handler();
    @OptIn(markerClass = UnstableApi.class)
    private final Runnable hideRunnable = () -> {
        if (playerView != null) playerView.hideController();
        if (danmakuInputArea != null) danmakuInputArea.setVisibility(View.GONE);
    };
    private static final String WS_URL = "ws://47.94.207.38:6000";
    @OptIn(markerClass = UnstableApi.class) protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.player_layout);

        playerView = findViewById(R.id.player);
        danmakuView = findViewById(R.id.danmaku_view);
        etDanmuInput = findViewById(R.id.et_danmu_input);
        btnSendDanmu = findViewById(R.id.btn_send_danmu);
        danmakuInputArea = findViewById(R.id.danmaku_input_area);

// 点击播放器，显示控制栏和弹幕输入框

        playerView.setOnClickListener(v -> {
            playerView.showController();
            danmakuInputArea.setVisibility(View.VISIBLE);
            restartHideTimer();
        });

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if (mode != null && mode.equals("replay")) {
            FLV_URL = "http://59.110.173.159/live/" + intent.getStringExtra("download_url");
            Toast.makeText(this, FLV_URL, Toast.LENGTH_SHORT).show();
        }

        initializePlayer();
        initDanmaku();
        connectWebSocket();

        btnSendDanmu.setOnClickListener(v -> {
            String text = etDanmuInput.getText().toString().trim();
            if (!text.isEmpty()) {
                if (webSocket != null) {
                    webSocket.send(text);
                }
                etDanmuInput.setText("");
                restartHideTimer(); // 刷新隐藏倒计时
            }
        });

    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(FLV_URL);
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource
                .Factory(dataSourceFactory, () -> new FlvExtractor[]{new FlvExtractor()})
                .createMediaSource(mediaItem);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }
    private void restartHideTimer() {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, 3000); // 3秒后隐藏
    }


    private void initDanmaku() {
        danmakuContext = DanmakuContext.create();
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override public void prepared() { danmakuView.start(); }
            @Override public void updateTimer(DanmakuTimer timer) {}
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
    }

    private void addDanmaku(String text, boolean isSelf) {
        if (danmakuView == null || !danmakuView.isPrepared()) return;

        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null) return;

        danmaku.text = text;
        danmaku.textSize = 50f;
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        danmaku.priority = 1;
        danmaku.borderColor = isSelf ? Color.YELLOW : Color.TRANSPARENT;

        danmakuView.addDanmaku(danmaku);
    }

    private void connectWebSocket() {
        client = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override public void onOpen(WebSocket webSocket, Response response) {}

            @Override public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> addDanmaku(text, false));
            }

            @Override public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
            }

            @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("PlayerActivity", "WebSocket error", t);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (danmakuView != null) {
            danmakuView.release();
        }
        if (webSocket != null) {
            webSocket.close(1000, "Activity stopped");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}
