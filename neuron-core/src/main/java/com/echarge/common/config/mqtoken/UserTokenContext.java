package com.echarge.common.config.mqtoken;

public class UserTokenContext {
    private static ThreadLocal<String> userToken = new ThreadLocal();

    public static String getToken() {
        return userToken.get();
    }

    public static void setToken(String token) {
        userToken.set(token);
    }

    public static void remove() {
        userToken.remove();
    }
}

