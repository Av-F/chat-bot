package com.chatbot;

import com.google.gson.*;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;

public class OpenAiClient implements LlmClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public OpenAiClient() {
        this.apiKey = Utils.env("OPENAI_API_KEY");
        this.apiUrl = Utils.env("OPENAI_API_URL", "https://api.openai.com/v1/chat/completions");
        this.model  = Utils.env("LLM_MODEL", "gpt-4o-mini");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set");
        }
    }

    @Override
    public String chat(List<ChatMessage> messages) throws IOException, InterruptedException {
        JsonObject root = new JsonObject();
        root.addProperty("model", model);

        JsonArray msgArr = new JsonArray();
        for (ChatMessage m : messages) {
            JsonObject o = new JsonObject();
            switch (m.getRole()) {
                case SYSTEM -> o.addProperty("role", "system");
                case USER -> o.addProperty("role", "user");
                case ASSISTANT -> o.addProperty("role", "assistant");
            }
            o.addProperty("content", m.getContent());
            msgArr.add(o);
        }
        root.add("messages", msgArr);

        String body = new Gson().toJson(root);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IOException("OpenAI API error: " + res.statusCode() + " " + res.body());
        }
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        try {
            String content = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            return content;
        } catch (Exception e) {
            throw new IOException("Failed to parse OpenAI response: " + e.getMessage() + " | body=" + res.body());
        }
    }
}
