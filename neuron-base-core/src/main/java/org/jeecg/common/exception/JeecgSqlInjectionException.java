package org.jeecg.common.exception;

public class JeecgSqlInjectionException extends com.echarge.common.exception.NeuronSqlInjectionException {
    public JeecgSqlInjectionException(String message) {
        super(message);
    }

    public JeecgSqlInjectionException(Throwable cause) {
        super(cause);
    }

    public JeecgSqlInjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
