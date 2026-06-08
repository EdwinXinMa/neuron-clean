package com.echarge.modules.platform.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 平台间 API Key 鉴权过滤器
 * 拦截 /platform/v1/** 路径（排除 /platform/v1/health）
 * Header: X-Platform-Key
 *
 * @author Edwin
 */
@Slf4j
@Component
public class PlatformApiKeyFilter implements Filter {

    private static final String API_KEY_HEADER = "X-Platform-Key";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${neuron.platform.api-key:}")
    private String configuredApiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();

        // 放行健康检查
        if ("/platform/v1/health".equals(path)) {
            chain.doFilter(req, res);
            return;
        }

        // 非 /platform/ 路径不拦截
        if (!path.startsWith("/platform/")) {
            chain.doFilter(req, res);
            return;
        }

        // API Key 未配置时拒绝所有请求（fail-closed）
        if (!StringUtils.hasText(configuredApiKey)) {
            log.error("[Platform] API Key 未配置，拒绝请求: {}", path);
            writeError(res, 503, "Platform API Key not configured");
            return;
        }

        String key = req.getHeader(API_KEY_HEADER);
        if (!configuredApiKey.equals(key)) {
            log.warn("[Platform] API Key 校验失败, path={}, remoteAddr={}", path, req.getRemoteAddr());
            writeError(res, 401, "Invalid API Key");
            return;
        }

        chain.doFilter(req, res);
    }

    private void writeError(HttpServletResponse res, int code, String message) throws IOException {
        res.setStatus(code);
        res.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        body.put("timestamp", System.currentTimeMillis());
        res.getWriter().write(MAPPER.writeValueAsString(body));
    }
}
