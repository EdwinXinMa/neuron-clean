package com.echarge.modules.app.filter;

import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.mapper.AppUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.system.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * App 端 Token 校验过滤器（独立于运维后台的 Shiro）
 * 拦截 /app/** 路径（排除 /app/auth/**）
 * @author Edwin
 */
@Slf4j
@Component
public class AppTokenFilter implements Filter {

    private static final String TOKEN_HEADER = "X-App-Token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AppUserMapper appUserMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();

        // 放行认证接口
        if (path.startsWith("/app/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        // 放行固件检查和下载接口（免登录）
        if ("/app/firmware/check".equals(path) || path.startsWith("/app/firmware/download/")) {
            chain.doFilter(request, response);
            return;
        }

        // 非 /app/ 路径不拦截
        if (!path.startsWith("/app/")) {
            chain.doFilter(request, response);
            return;
        }

        String token = req.getHeader(TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            writeError(res, 401, "未提供 Token");
            return;
        }

        // 从 Token 中解析邮箱
        String email = JwtUtil.getUsername(token);
        if (email == null) {
            writeError(res, 401, "Token 无效");
            return;
        }

        // 查用户
        AppUser user = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getEmail, email));
        if (user == null) {
            writeError(res, 401, "用户不存在");
            return;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            writeError(res, 401, "账号已禁用");
            return;
        }

        // 验证 Token 签名
        if (!JwtUtil.verify(token, email, user.getPassword())) {
            writeError(res, 401, "Token 已过期或签名无效");
            return;
        }

        // 将用户信息放入 request attribute，供 Controller 使用
        req.setAttribute("appUser", user);
        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse res, int code, String message) throws IOException {
        res.setStatus(code);
        res.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("success", false);
        body.put("message", message);
        body.put("data", null);
        body.put("timestamp", System.currentTimeMillis());
        res.getWriter().write(MAPPER.writeValueAsString(body));
    }
}
