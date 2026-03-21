package com.echarge.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * 浏览器/客户端检测工具
 */
public class BrowserUtils {

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "(phone|pad|pod|iphone|ipod|ios|ipad|android|mobile|blackberry|iemobile|mqqbrowser|juc|fennec|wosbrowser|browserng|webos|symbian|windows phone)");

    /** 判断请求是否来自电脑端 */
    public static boolean isDesktop(HttpServletRequest request) {
        return !isMobile(request);
    }

    /** 判断请求是否来自移动端 */
    public static boolean isMobile(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            return false;
        }
        return MOBILE_PATTERN.matcher(ua.toLowerCase()).find();
    }
}
