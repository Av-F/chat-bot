# Java Console LLM Chatbot

A minimal Java 17 console chatbot that can call **OpenAI-compatible** chat APIs or **Anthropic** using pluggable clients.

## Quick Start

```bash
# 1) Ensure you have Java 17+ and Maven
java -version
mvn -v

# 2) Copy and edit environment
cp .env.example .env
# Edit .env and set LLM_PROVIDER (OPENAI or ANTHROPIC) and the API keys

# 3) Build & run
mvn -q -DskipTests clean package
mvn -q exec:java
```

### Switching Providers
- **OPENAI**: uses `OPENAI_API_URL`, `OPENAI_API_KEY`, `LLM_MODEL`.
- **ANTHROPIC**: uses `ANTHROPIC_API_URL`, `ANTHROPIC_API_KEY`, `ANTHROPIC_MODEL`.

### Notes
- This app stores a running chat history in memory and sends it on each call.
- Responses are streamed logically (simple non-streaming HTTP call; you can extend to server-sent events later).
- JSON parsing is done with Gson.
- Env loading is via `java-dotenv`. System env vars override `.env` when both are present.

## Files
- `src/main/java/com/chatbot/ChatbotApp.java` – main loop & wiring
- `src/main/java/com/chatbot/LlmClient.java` – client interface
- `src/main/java/com/chatbot/OpenAiClient.java` – OpenAI-compatible implementation
- `src/main/java/com/chatbot/AnthropicClient.java` – Anthropic implementation
- `src/main/java/com/chatbot/ChatMessage.java` – POJO for messages
- `src/main/java/com/chatbot/Utils.java` – helpers

## License
MIT
