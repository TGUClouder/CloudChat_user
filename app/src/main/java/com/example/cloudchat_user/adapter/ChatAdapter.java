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

        // 显示时间
        holder.messageTime.setText(message.getTime());
        holder.messageTime.setVisibility(View.VISIBLE);

        if (message.isSent()) {
            holder.receiveLayout.setVisibility(View.GONE);
            holder.sendLayout.setVisibility(View.VISIBLE);

            holder.senderAvatar.setImageResource(R.drawable.studentphoto);
            holder.senderAvatar.setVisibility(View.VISIBLE);

            if (message.getImageBitmap() != null) {
                holder.senderMessageImage.setVisibility(View.VISIBLE);
                holder.senderMessageText.setVisibility(View.GONE);
                holder.senderMessageImage.setImageBitmap(message.getImageBitmap());
            } else if (message.getImageUrl() != null) {
                holder.senderMessageImage.setVisibility(View.VISIBLE);
                holder.senderMessageText.setVisibility(View.GONE);
                try {
                    if (message.getImageUrl().startsWith("data:image")) {
                        String base64Image = message.getImageUrl().split(",")[1];
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.senderMessageImage.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    Log.e("ChatAdapter", "图片加载错误", e);
                }
            } else {
                holder.senderMessageImage.setVisibility(View.GONE);
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setText(message.getContent());
            }

        } else {
            holder.sendLayout.setVisibility(View.GONE);
            holder.receiveLayout.setVisibility(View.VISIBLE);

            holder.receiverAvatar.setImageResource(R.drawable.colledgephoto);
            holder.receiverAvatar.setVisibility(View.VISIBLE);

            if (message.getImageBitmap() != null) {
                holder.receiverMessageImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.GONE);
                holder.receiverMessageImage.setImageBitmap(message.getImageBitmap());
            } else if (message.getImageUrl() != null) {
                holder.receiverMessageImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.GONE);
                try {
                    if (message.getImageUrl().startsWith("data:image")) {
                        String base64Image = message.getImageUrl().split(",")[1];
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.receiverMessageImage.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    Log.e("ChatAdapter", "图片加载错误", e);
                }
            } else {
                holder.receiverMessageImage.setVisibility(View.GONE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setText(message.getContent());
            }
        }
    }




    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMessageText, senderMessageText, messageTime;
        ImageView receiverMessageImage, senderMessageImage;
        ImageView receiverAvatar, senderAvatar;
        LinearLayout receiveLayout, sendLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // 时间
            messageTime = itemView.findViewById(R.id.messageTime);

            // 接收消息相关
            receiveLayout = itemView.findViewById(R.id.receiveLayout);
            receiverMessageText = itemView.findViewById(R.id.receiverMessageText);
            receiverMessageImage = itemView.findViewById(R.id.receiverMessageImage);
            receiverAvatar = itemView.findViewById(R.id.receiverAvatar);

            // 发送消息相关
            sendLayout = itemView.findViewById(R.id.sendLayout);
            senderMessageText = itemView.findViewById(R.id.senderMessageText);
            senderMessageImage = itemView.findViewById(R.id.senderMessageImage);
            senderAvatar = itemView.findViewById(R.id.senderAvatar);
        }
    }


} 