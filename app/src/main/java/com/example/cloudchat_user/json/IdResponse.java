package com.example.cloudchat_user.json;

public class IdResponse {
    private Message message;

    public static class Message {
        private String id;

        // Getter and Setter
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    // Getter and Setter
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}