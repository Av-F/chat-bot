package com.chatbot;

import java.util.*;
import java.io.*;
import static com.chatbot.ChatMessage.Role.*;

public class ChatbotApp {
    public static void main(String[] args) {
        // Load provider
        String provider = Utils.env("LLM_PROVIDER", "OPENAI").trim().toUpperCase(Locale.ROOT);
        String systemPrompt = Utils.env("SYSTEM_PROMPT", "You are a helpful Java console chatbot.");

        LlmClient client = switch (provider) {
            case "ANTHROPIC" -> new AnthropicClient();
            case "OPENAI" -> new OpenAiClient();
            default -> throw new IllegalArgumentException("Unsupported LLM_PROVIDER: " + provider);
        };

        List<ChatMessage> history = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            history.add(new ChatMessage(SYSTEM, systemPrompt));
        }

        System.out.println("🤖 Chatbot ready (" + provider + "). Type 'exit' to quit.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("You: ");
                if (!scanner.hasNextLine()) break;
                String user = scanner.nextLine();
                if ("exit".equalsIgnoreCase(user.trim())) break;
                if (user.isBlank()) continue;

                history.add(new ChatMessage(USER, user));
                try {
                    String reply = client.chat(history);
                    System.out.println("Bot: " + reply);
                    history.add(new ChatMessage(ASSISTANT, reply));
                } catch (Exception e) {
                    System.err.println("⚠️  Error: " + e.getMessage());
                    // Allow continuing the session after errors.
                }
            }
        }
        System.out.println("👋 Bye!");
    }
}
