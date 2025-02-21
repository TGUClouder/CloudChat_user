package com.example.cloudchat_user.ui.mind_sanctuary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cloudchat_user.R;
import com.example.cloudchat_user.adapter.ChatAdapter;
import com.example.cloudchat_user.model.ChatMessage;
import com.example.cloudchat_user.network.WebSocketManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okio.ByteString;

public class ChatFragment extends Fragment implements WebSocketManager.WebSocketListenerInterface {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ImageButton imageButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private WebSocketManager webSocketManager;
    private static final String WEBSOCKET_URL = "ws://47.94.207.38:8080";
    private static final String TAG = "ChatFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.d(TAG, "Attempting to connect to: " + WEBSOCKET_URL);
        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.setListener(this);
        webSocketManager.connect(WEBSOCKET_URL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("心灵树洞");
            }
        }

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        imageButton = view.findViewById(R.id.imageButton);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                webSocketManager.sendMessage(message);
                sendMessage(message);
                messageInput.setText("");
            }
        });

        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择图片"), PICK_IMAGE_REQUEST);
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(requireView()).navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
        webSocketManager.disconnect();
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "ChatFragment received message: " + message);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                ChatMessage chatMessage = new ChatMessage(message, false);
                messages.add(chatMessage);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                chatRecyclerView.scrollToPosition(messages.size() - 1);
                Log.d(TAG, "Message added to chat UI: " + message);
            });
        }
    }

    @Override
    public void onOpen() {
        Log.d(TAG, "WebSocket connected successfully");
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "已连接到聊天服务器", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBinaryMessage(ByteString bytes) {
        Log.d(TAG, "收到二进制图片，大小: " + bytes.size());

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    byte[] imageBytes = bytes.toByteArray();
                    Bitmap bitmap =     BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    if (bitmap != null) {
                        // 添加图片消息
                        ChatMessage chatMessage = new ChatMessage("", false);
                        chatMessage.setBitmap(bitmap);  // 设置图片到消息
                        messages.add(chatMessage);
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        chatRecyclerView.scrollToPosition(messages.size() - 1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析二进制数据出错", e);
                }
            });
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Log.e(TAG, "WebSocket connection failed", t);
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onClosed() {

    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true);
        messages.add(chatMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                sendImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 压缩图片
        byte[] imageBytes = baos.toByteArray();  // 获取图片的字节数组

        // 发送二进制图片数据
        webSocketManager.sendBinaryMessage(imageBytes);

        // 本地显示图片
        ChatMessage chatMessage = new ChatMessage("", true);
        chatMessage.setBitmap(bitmap);  // 将图片设置到聊天消息
        messages.add(chatMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }
}