package com.echarge.modules.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 平台间对接统一响应对象
 *
 * @author Edwin
 */
@Data
public class PlatformResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> PlatformResult<T> ok(T data) {
        PlatformResult<T> r = new PlatformResult<>();
        r.setCode(200);
        r.setMessage("ok");
        r.setData(data);
        return r;
    }

    public static PlatformResult<?> ok() {
        return ok(null);
    }

    public static <T> PlatformResult<T> error(int code, String message) {
        PlatformResult<T> r = new PlatformResult<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> PlatformResult<T> error(String message) {
        return error(500, message);
    }
}
