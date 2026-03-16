package org.jeecg.common.exception;

public class JeecgBootBizTipException extends com.echarge.common.exception.NeuronBootBizTipException {
    public JeecgBootBizTipException(String message) {
        super(message);
    }

    public JeecgBootBizTipException(String message, int errCode) {
        super(message, errCode);
    }

    public JeecgBootBizTipException(Throwable cause) {
        super(cause);
    }

    public JeecgBootBizTipException(String message, Throwable cause) {
        super(message, cause);
    }
}
