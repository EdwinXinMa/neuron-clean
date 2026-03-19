package com.echarge.protocol.transport;

import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.config.ProtocolProperties;
import com.echarge.protocol.core.dispatcher.MessageDispatcher;
import com.echarge.protocol.core.session.SessionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private final ProtocolProperties properties;
    private final SessionManager sessionManager;
    private final Map<String, MessageDispatcher> dispatchers;
    private final DeviceEventPublisher eventPublisher;

    public WebSocketInitializer(ProtocolProperties properties,
                                SessionManager sessionManager,
                                Map<String, MessageDispatcher> dispatchers,
                                DeviceEventPublisher eventPublisher) {
        this.properties = properties;
        this.sessionManager = sessionManager;
        this.dispatchers = dispatchers;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(properties.getMaxFrameSize()));
        pipeline.addLast(new WebSocketServerProtocolHandler(
                properties.getPath(),
                "ocpp1.6,ocpp2.0.1",
                true,
                properties.getMaxFrameSize(),
                false,
                true  // checkStartsWith: /ocpp/TEST001 匹配 /ocpp 前缀
        ));
        // 180秒未收到任何消息则判定设备超时，触发关闭连接
        pipeline.addLast(new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new OcppFrameCodec());
        pipeline.addLast(new ProtocolRouter(sessionManager, dispatchers, eventPublisher));
    }
}
