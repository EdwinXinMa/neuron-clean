package org.jeecg.common.exception;

public class JeecgBootException extends com.echarge.common.exception.NeuronBootException {
    public JeecgBootException(String message) {
        super(message);
    }

    public JeecgBootException(String message, int errCode) {
        super(message, errCode);
    }

    public JeecgBootException(Throwable cause) {
        super(cause);
    }

    public JeecgBootException(String message, Throwable cause) {
        super(message, cause);
    }
}
