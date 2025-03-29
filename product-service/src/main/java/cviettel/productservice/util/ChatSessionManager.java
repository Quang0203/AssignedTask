package cviettel.productservice.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatSessionManager {
    private static final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    public static ChatSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public static void saveSession(String sessionId, ChatSession session) {
        sessions.put(sessionId, session);
    }
}
