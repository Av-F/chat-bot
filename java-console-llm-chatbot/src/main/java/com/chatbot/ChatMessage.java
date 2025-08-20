package com.chatbot;

public class ChatMessage {
    public enum Role { SYSTEM, USER, ASSISTANT }

    private Role role;
    private String content;

    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public Role getRole() { return role; }
    public String getContent() { return content; }
}
