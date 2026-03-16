package org.jeecg.chatgpt.service;

import java.util.List;

public interface AiChatService {
    String multiCompletions(List<?> messages);
}
