package com.example.cloudchat_user.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cloudchat_user.R;
import com.example.cloudchat_user.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        LinearLayout rootLayout = (LinearLayout) holder.itemView;

        if (message.getImageBitmap() != null) {  // 先检查 Bitmap
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setImageBitmap(message.getImageBitmap());
        } else if (message.getImageUrl() != null) {  // 如果没有 Bitmap，检查 Base64
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            try {
                Log.d("ChatAdapter", "收到的图片数据：" + message.getImageUrl().substring(0, Math.min(50, message.getImageUrl().length())) + "...");

                if (message.getImageUrl().startsWith("data:image")) {
                    String base64Image = message.getImageUrl().split(",")[1];
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    if (bitmap != null) {
                        Log.d("ChatAdapter", "Bitmap 解析成功，大小: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        holder.messageImage.setImageBitmap(bitmap);
                    } else {
                        Log.e("ChatAdapter", "Bitmap 解析失败");
                    }
                }
            } catch (Exception e) {
                Log.e("ChatAdapter", "图片加载错误", e);
            }
        } else {  // 文字消息
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getContent());
        }

        // 处理消息的布局
        if (message.isSent()) {
            rootLayout.setGravity(Gravity.END);
            holder.messageText.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.chat_bubble_sent));
            holder.avatarImage.setVisibility(View.VISIBLE);
            holder.avatarImage.setImageResource(R.drawable.studentphoto);

            rootLayout.removeView(holder.avatarImage);
            rootLayout.addView(holder.avatarImage);
        } else {
            rootLayout.setGravity(Gravity.START);
            holder.messageText.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.chat_bubble_received));
            holder.avatarImage.setVisibility(View.VISIBLE);
            holder.avatarImage.setImageResource(R.drawable.colledgephoto);

            rootLayout.removeView(holder.avatarImage);
            rootLayout.addView(holder.avatarImage, 0);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        ImageView avatarImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            avatarImage = itemView.findViewById(R.id.avatarImage);
        }
    }
} 