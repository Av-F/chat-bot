package com.chatbot;

import com.google.gson.*;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;

public class AnthropicClient implements LlmClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public AnthropicClient() {
        this.apiKey = Utils.env("ANTHROPIC_API_KEY");
        this.apiUrl = Utils.env("ANTHROPIC_API_URL", "https://api.anthropic.com/v1/messages");
        this.model  = Utils.env("ANTHROPIC_MODEL", "claude-3-5-sonnet-latest");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY is not set");
        }
    }

    @Override
    public String chat(List<ChatMessage> messages) throws IOException, InterruptedException {
        // Build Anthropic messages array (user/assistant only; prepend system separately)
        String systemPrompt = null;
        List<JsonObject> userAssistant = new ArrayList<>();
        for (ChatMessage m : messages) {
            if (m.getRole() == ChatMessage.Role.SYSTEM) {
                systemPrompt = m.getContent();
            } else {
                JsonObject entry = new JsonObject();
                entry.addProperty("role", m.getRole() == ChatMessage.Role.USER ? "user" : "assistant");
                JsonArray contentArr = new JsonArray();
                JsonObject textObj = new JsonObject();
                textObj.addProperty("type", "text");
                textObj.addProperty("text", m.getContent());
                contentArr.add(textObj);
                entry.add("content", contentArr);
                userAssistant.add(entry);
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            root.addProperty("system", systemPrompt);
        }
        JsonArray msgs = new JsonArray();
        for (JsonObject o : userAssistant) msgs.add(o);
        root.add("messages", msgs);
        root.addProperty("max_tokens", 1024);

        String body = new Gson().toJson(root);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("Anthropic API error: " + res.statusCode() + " " + res.body());
        }
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        try {
            JsonArray content = json.getAsJsonArray("content");
            if (content != null && content.size() > 0) {
                JsonObject first = content.get(0).getAsJsonObject();
                if ("text".equals(first.get("type").getAsString())) {
                    return first.get("text").getAsString();
                }
            }
            // fallback for unexpected shapes
            return json.toString();
        } catch (Exception e) {
            throw new IOException("Failed to parse Anthropic response: " + e.getMessage() + " | body=" + res.body());
        }
    }
}
