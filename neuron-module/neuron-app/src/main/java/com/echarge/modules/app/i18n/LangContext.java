package com.echarge.modules.app.i18n;

/**
 * 当前请求语言上下文（ThreadLocal）
 * 由 AppTokenFilter 从 Accept-Language Header 写入，请求结束后清除
 * @author Edwin
 */
public class LangContext {

    private static final ThreadLocal<String> LANG = new ThreadLocal<>();

    public static void set(String lang) {
        LANG.set(lang);
    }

    /** 返回当前语言，默认 zh */
    public static String get() {
        String l = LANG.get();
        return l != null ? l : "zh";
    }

    public static void clear() {
        LANG.remove();
    }
}
