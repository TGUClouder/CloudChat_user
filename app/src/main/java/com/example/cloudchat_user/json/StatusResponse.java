package com.example.cloudchat_user.json;

public class StatusResponse {
    private Message message;

    public static class Message {
        private String status;

        // Getter and Setter
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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