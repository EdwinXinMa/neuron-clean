package com.echarge.common.exception;

import com.echarge.common.constant.CommonConstant;

/**
 * @Description: 业务提醒异常(用于操作业务提醒)
 * @date: 2026-03-22
 * @author: Edwin
 */
public class NeuronBootBizTipException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 返回给前端的错误code
	 */
	private int errCode = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;

	public NeuronBootBizTipException(String message){
		super(message);
	}

	public NeuronBootBizTipException(String message, int errCode){
		super(message);
		this.errCode = errCode;
	}

	public int getErrCode() {
		return errCode;
	}

	public NeuronBootBizTipException(Throwable cause)
	{
		super(cause);
	}
	
	public NeuronBootBizTipException(String message, Throwable cause)
	{
		super(message,cause);
	}
}
