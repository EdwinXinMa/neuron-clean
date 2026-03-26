package com.echarge.protocol.ocpp.common;

import com.echarge.protocol.core.dispatcher.InboundMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Manages pending server-initiated CALL requests, matching response messageIds to CompletableFutures.
 * @author Edwin
 */
@Slf4j
public final class PendingCallManager {

    private static final Map<String, CompletableFuture<InboundMessage>> PENDING = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService TIMEOUT_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ocpp-pending-timeout");
                t.setDaemon(true);
                return t;
            });

    private PendingCallManager() {}

    /**
     * 注册待处理的OCPP请求，返回一个Future用于等待响应
     * @param messageId      消息ID
     * @param timeoutSeconds 超时秒数
     * @return 响应Future
     */
    public static CompletableFuture<InboundMessage> register(String messageId, long timeoutSeconds) {
        CompletableFuture<InboundMessage> future = new CompletableFuture<>();
        PENDING.put(messageId, future);

        TIMEOUT_EXECUTOR.schedule(() -> {
            CompletableFuture<InboundMessage> pending = PENDING.remove(messageId);
            if (pending != null && !pending.isDone()) {
                pending.completeExceptionally(new TimeoutException("OCPP call timeout: " + messageId));
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return future;
    }

    /**
     * 完成待处理的OCPP请求，将收到的响应传递给等待方
     * @param messageId 消息ID
     * @param response  响应消息
     */
    public static void complete(String messageId, InboundMessage response) {
        CompletableFuture<InboundMessage> future = PENDING.remove(messageId);
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("No pending call for messageId: {}", messageId);
        }
    }
}
