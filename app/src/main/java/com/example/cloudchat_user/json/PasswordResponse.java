package com.example.cloudchat_user.json;

public class PasswordResponse {
    private Message message;

    public static class Message {
        private String password;

        // Getter and Setter
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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
