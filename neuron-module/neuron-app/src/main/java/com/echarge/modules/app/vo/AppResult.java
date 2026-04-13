package com.echarge.modules.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * App 端统一返回对象（字段名 data，区别于运维后台的 result）
 * @author Edwin
 */
@Data
public class AppResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success = true;
    private String message = "";
    private Integer code = 0;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static AppResult<?> ok(String msg) {
        AppResult<?> r = new AppResult<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage(msg);
        return r;
    }

    public static <T> AppResult<T> ok(T data) {
        AppResult<T> r = new AppResult<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setData(data);
        return r;
    }

    public static <T> AppResult<T> ok(String msg, T data) {
        AppResult<T> r = new AppResult<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }

    public static <T> AppResult<T> error(String msg) {
        AppResult<T> r = new AppResult<>();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage(msg);
        return r;
    }

    public static <T> AppResult<T> error(int code, String msg) {
        AppResult<T> r = new AppResult<>();
        r.setSuccess(false);
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }
}
