package org.jeecg.chatgpt.service;

import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Stub implementation - AI chat not configured
 */
@Component
public class AiChatServiceImpl implements AiChatService {
    @Override
    public String multiCompletions(List<?> messages) {
        throw new UnsupportedOperationException("AI chat service is not configured");
    }
}
