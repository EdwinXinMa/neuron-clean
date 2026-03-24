package com.echarge.common.websocket;

import java.util.function.Consumer;

/**
 * 前端推送通道 — core 层接口，device 模块注册实现，其他模块通过此接口广播
 */
public final class FrontendPushChannel {

    private static volatile Consumer<String> broadcaster;

    private FrontendPushChannel() {}

    /**
     * 注册广播实现（DeviceEventWebSocket 启动时调用）
     */
    public static void register(Consumer<String> impl) {
        broadcaster = impl;
    }

    /**
     * 广播消息到所有前端
     */
    public static void broadcast(String message) {
        Consumer<String> b = broadcaster;
        if (b != null) {
            b.accept(message);
        }
    }
}
