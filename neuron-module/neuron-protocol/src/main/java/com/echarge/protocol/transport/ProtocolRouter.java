package com.echarge.protocol.transport;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.echarge.protocol.core.dispatcher.InboundMessage;
import com.echarge.protocol.core.dispatcher.MessageDispatcher;
import com.echarge.protocol.core.dispatcher.OutboundMessage;
import com.echarge.protocol.core.session.Session;
import com.echarge.protocol.core.session.SessionManager;
import com.echarge.protocol.ocpp.common.OcppErrorCode;
import com.echarge.protocol.ocpp.common.PendingCallManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ProtocolRouter extends SimpleChannelInboundHandler<InboundMessage> {

    private final SessionManager sessionManager;
    private final Map<String, MessageDispatcher> dispatchers;
    private final DeviceEventPublisher eventPublisher;

    public ProtocolRouter(SessionManager sessionManager, Map<String, MessageDispatcher> dispatchers, DeviceEventPublisher eventPublisher) {
        this.sessionManager = sessionManager;
        this.dispatchers = dispatchers;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 心跳超时检测：180秒未收到任何消息，关闭连接
        if (evt instanceof IdleStateEvent) {
            Session session = sessionManager.getByChannel(ctx.channel());
            String cpId = session != null ? session.getChargePointId() : "unknown";
            log.warn("Device heartbeat timeout, closing connection: chargePointId={}", cpId);
            ctx.close();
            return;
        }
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshake) {
            String uri = handshake.requestUri();
            String subProtocol = handshake.selectedSubprotocol();

            // Extract chargePointId from URI: /ocpp/{chargePointId}
            String chargePointId = extractChargePointId(uri);
            if (chargePointId == null || chargePointId.isEmpty()) {
                log.error("No chargePointId in URI: {}", uri);
                ctx.close();
                return;
            }

            if (subProtocol == null || subProtocol.isEmpty()) {
                subProtocol = "ocpp1.6"; // default fallback
            }

            if (!dispatchers.containsKey(subProtocol)) {
                log.error("Unsupported sub-protocol: {}", subProtocol);
                ctx.close();
                return;
            }

            sessionManager.register(chargePointId, ctx.channel(), subProtocol);
            log.info("WebSocket handshake complete: chargePointId={}, subProtocol={}", chargePointId, subProtocol);
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InboundMessage msg) {
        Session session = sessionManager.getByChannel(ctx.channel());
        if (session == null) {
            log.warn("Received message from unregistered channel");
            ctx.close();
            return;
        }

        // Handle CALLRESULT/CALLERROR responses to server-initiated requests
        if (msg.getMessageType() == 3 || msg.getMessageType() == 4) {
            PendingCallManager.complete(msg.getMessageId(), msg);
            return;
        }

        // Handle CALL requests
        MessageDispatcher dispatcher = dispatchers.get(session.getProtocolVersion());
        if (dispatcher == null) {
            log.error("No dispatcher for protocol: {}", session.getProtocolVersion());
            ctx.writeAndFlush(OutboundMessage.callError(msg.getMessageId(),
                    OcppErrorCode.NOT_SUPPORTED.getCode(), "Protocol not supported"));
            return;
        }

        OutboundMessage response = dispatcher.dispatch(session, msg);
        if (response != null) {
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        handleDisconnect(ctx);
        ctx.fireChannelInactive();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        handleDisconnect(ctx);
    }

    private void handleDisconnect(ChannelHandlerContext ctx) {
        Session session = sessionManager.getByChannel(ctx.channel());
        if (session != null) {
            log.info("Device disconnected: chargePointId={}", session.getChargePointId());
            DeviceEvent event = new DeviceEvent(
                    DeviceEvent.DEVICE_OFFLINE,
                    session.getChargePointId(),
                    null
            );
            eventPublisher.publish(event);
            sessionManager.unregister(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel exception: {}", cause.getMessage(), cause);
        ctx.close();
    }

    private String extractChargePointId(String uri) {
        if (uri == null) return null;
        // Remove query string if present
        int queryIdx = uri.indexOf('?');
        if (queryIdx > 0) {
            uri = uri.substring(0, queryIdx);
        }
        // Get the last path segment
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < uri.length() - 1) {
            return uri.substring(lastSlash + 1);
        }
        return null;
    }
}
