package com.echarge.protocol.transport;

import com.echarge.protocol.config.ProtocolProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * OCPP WebSocket 连接认证拦截器
 * 在 WebSocket 握手前校验密码，支持两种方式：
 * 1. HTTP Basic Auth header（设备标准方式）
 * 2. URL 参数 ?auth=password（测试页方便用）
 * @author Edwin
 */
@Slf4j
public class HttpAuthHandler extends ChannelInboundHandlerAdapter {

    private final ProtocolProperties properties;

    public HttpAuthHandler(ProtocolProperties properties) {
        this.properties = properties;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest request) {
            // 认证未开启，直接放行
            if (!properties.isAuthEnabled()) {
                ctx.fireChannelRead(msg);
                return;
            }

            String password = extractPassword(request);
            if (properties.getAuthKey().equals(password)) {
                // 认证通过，放行
                log.debug("OCPP auth passed: uri={}", request.uri());
                ctx.fireChannelRead(msg);
            } else {
                // 认证失败，返回 401
                log.warn("OCPP auth failed: uri={}, remote={}", request.uri(), ctx.channel().remoteAddress());
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
                response.headers().set("WWW-Authenticate", "Basic realm=\"OCPP\"");
                response.headers().set("Content-Length", 0);
                ctx.writeAndFlush(response);
                ctx.close();
                request.release();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 从请求中提取密码，优先 Basic Auth header，其次 URL 参数
     * @param request HTTP 请求
     * @return 密码，提取失败返回 null
     */
    private String extractPassword(FullHttpRequest request) {
        // 方式1: Basic Auth header
        String authHeader = request.headers().get("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
                // 格式: username:password
                int colonIndex = decoded.indexOf(':');
                if (colonIndex >= 0) {
                    return decoded.substring(colonIndex + 1);
                }
            } catch (Exception e) {
                log.debug("Failed to decode Basic Auth header: {}", e.getMessage());
            }
        }

        // 方式2: URL 参数 ?auth=password
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        List<String> authParams = decoder.parameters().get("auth");
        if (authParams != null && !authParams.isEmpty()) {
            return authParams.get(0);
        }

        return null;
    }
}
