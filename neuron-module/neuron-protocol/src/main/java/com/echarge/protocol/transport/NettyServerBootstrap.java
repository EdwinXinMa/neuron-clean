package com.echarge.protocol.transport;

import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.config.ProtocolProperties;
import com.echarge.protocol.core.dispatcher.MessageDispatcher;
import com.echarge.protocol.core.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class NettyServerBootstrap implements SmartLifecycle {

    private final ProtocolProperties properties;
    private final SessionManager sessionManager;
    private final Map<String, MessageDispatcher> dispatchers;
    private final DeviceEventPublisher eventPublisher;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running = false;

    public NettyServerBootstrap(ProtocolProperties properties,
                                SessionManager sessionManager,
                                List<MessageDispatcher> dispatcherList,
                                DeviceEventPublisher eventPublisher) {
        this.properties = properties;
        this.sessionManager = sessionManager;
        this.dispatchers = dispatcherList.stream()
                .collect(Collectors.toMap(MessageDispatcher::supportedProtocol, Function.identity()));
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start() {
        if (!properties.isEnabled()) {
            log.info("OCPP WebSocket Server is disabled");
            return;
        }

        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        int workers = properties.getWorkerThreads() > 0 ? properties.getWorkerThreads() : 0;
        workerGroup = new NioEventLoopGroup(workers);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketInitializer(properties, sessionManager, dispatchers, eventPublisher))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            serverChannel = bootstrap.bind(properties.getPort()).sync().channel();
            running = true;
            log.info("OCPP WebSocket Server started on port {}", properties.getPort());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to start OCPP WebSocket Server", e);
        }
    }

    @Override
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        running = false;
        log.info("OCPP WebSocket Server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }
}
