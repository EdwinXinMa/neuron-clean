package com.echarge.common.enums;

public enum SentinelErrorInfoEnum {
    FlowException("\u8bbf\u95ee\u9891\u7e41\uff0c\u8bf7\u7a0d\u5019\u518d\u8bd5"),
    ParamFlowException("\u70ed\u70b9\u53c2\u6570\u9650\u6d41"),
    SystemBlockException("\u7cfb\u7edf\u89c4\u5219\u9650\u6d41\u6216\u964d\u7ea7"),
    AuthorityException("\u6388\u6743\u89c4\u5219\u4e0d\u901a\u8fc7"),
    UnknownError("\u672a\u77e5\u5f02\u5e38"),
    DegradeException("\u670d\u52a1\u964d\u7ea7");

    String error;
    Integer code;

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    private SentinelErrorInfoEnum(String error, Integer code) {
        this.error = error;
        this.code = code;
    }

    private SentinelErrorInfoEnum(String error) {
        this.error = error;
        this.code = 500;
    }

    public static SentinelErrorInfoEnum getErrorByException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String exceptionClass = throwable.getClass().getSimpleName();
        for (SentinelErrorInfoEnum e : SentinelErrorInfoEnum.values()) {
            if (!exceptionClass.equals(e.name())) continue;
            return e;
        }
        return null;
    }
}

