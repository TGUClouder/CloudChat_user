package com.example.cloudchat_user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import org.jetbrains.annotations.NotNull;

public class PlayerActivity extends AppCompatActivity {
    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private String FLV_URL = "http://182.92.183.192:8080/live/livestream.flv";


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.player_layout);
        playerView = findViewById(R.id.player);
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if(mode.equals("replay")){
            FLV_URL = "http://182.92.183.192/live/"+intent.getStringExtra("download_url");
            Toast.makeText(this,FLV_URL,Toast.LENGTH_SHORT).show();
        }

        initializePlayer();

    }

    @OptIn(markerClass = UnstableApi.class) private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(FLV_URL);
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, () -> new FlvExtractor[]{new FlvExtractor()}).createMediaSource(mediaItem);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        // 释放播放器资源
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
