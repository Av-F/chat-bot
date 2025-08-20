package com.chatbot;

import io.github.cdimascio.dotenv.Dotenv;

public class Utils {
    private static Dotenv dotenv;

    public static String env(String key, String def) {
        if (dotenv == null) {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }
        String sys = System.getenv(key);
        if (sys != null && !sys.isEmpty()) return sys;
        String dot = dotenv.get(key);
        return (dot != null && !dot.isEmpty()) ? dot : def;
    }

    public static String env(String key) {
        return env(key, "");
    }
}
