package com.echarge.protocol.transport;

import com.echarge.protocol.config.ProtocolProperties;
import com.echarge.protocol.core.dispatcher.MessageDispatcher;
import com.echarge.protocol.core.session.SessionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.Map;

public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private final ProtocolProperties properties;
    private final SessionManager sessionManager;
    private final Map<String, MessageDispatcher> dispatchers;

    public WebSocketInitializer(ProtocolProperties properties,
                                SessionManager sessionManager,
                                Map<String, MessageDispatcher> dispatchers) {
        this.properties = properties;
        this.sessionManager = sessionManager;
        this.dispatchers = dispatchers;
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
        pipeline.addLast(new OcppFrameCodec());
        pipeline.addLast(new ProtocolRouter(sessionManager, dispatchers));
    }
}
