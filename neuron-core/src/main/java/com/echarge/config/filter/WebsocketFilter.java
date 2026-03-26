package com.echarge.config.filter;

import lombok.extern.slf4j.Slf4j;
import com.echarge.common.api.CommonApi;
import com.echarge.common.util.RedisUtil;
import com.echarge.common.util.SpringContextUtils;
import com.echarge.common.util.TokenUtils;
import com.echarge.common.util.OConvertUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * websocket 前端将token放到子协议里传入 与后端建立连接时需要用到http协议，此处用于校验token的有效性
 * @Author Edwin
 * @Date2026-03-22
 * @author Edwin
 **/
@Slf4j
public class WebsocketFilter implements Filter {

    private static final String TOKEN_KEY = "Sec-WebSocket-Protocol";

    private static CommonApi commonApi;

    private static RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (commonApi == null) {
            commonApi = SpringContextUtils.getBean(CommonApi.class);
        }
        if (redisUtil == null) {
            redisUtil = SpringContextUtils.getBean(RedisUtil.class);
        }
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String token = request.getHeader(TOKEN_KEY);

        log.debug("Websocket连接 Token安全校验，Path = {}，token:{}", request.getRequestURI(), token);

        try {
            TokenUtils.verifyToken(token, commonApi, redisUtil);
        } catch (Exception exception) {
            log.debug("Websocket连接 Token安全校验失败，IP:{}, Token:{}, Path = {}，异常：{}", OConvertUtils.getIpAddrByRequest(request), token, request.getRequestURI(), exception.getMessage());
            return;
        }
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.setHeader(TOKEN_KEY, sanitizeHeader(token));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 清洗 HTTP header 值，防止 HTTP Response Splitting
     * @param value 原始值
     * @return 去掉 \r \n 的安全值
     */
    static String sanitizeHeader(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\r\\n]", "");
    }

}
