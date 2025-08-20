package com.chatbot;

import java.io.IOException;
import java.util.List;

public interface LlmClient {
    /**
     * Send a chat completion request with the full message history.
     * @param messages Ordered list of messages (system, user, assistant).
     * @return Assistant reply content (plain text).
     */
    String chat(List<ChatMessage> messages) throws IOException, InterruptedException;
}
