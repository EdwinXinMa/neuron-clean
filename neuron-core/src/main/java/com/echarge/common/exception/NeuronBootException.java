package com.echarge.common.exception;

import com.echarge.common.constant.CommonConstant;

/**
 * @Description: jeecg-boot自定义异常
 * @author Edwin
 */
public class NeuronBootException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 返回给前端的错误code
	 */
	private int errCode = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;

	public NeuronBootException(String message){
		super(message);
	}

	public NeuronBootException(String message, int errCode){
		super(message);
		this.errCode = errCode;
	}

	public int getErrCode() {
		return errCode;
	}

	public NeuronBootException(Throwable cause)
	{
		super(cause);
	}
	
	public NeuronBootException(String message,Throwable cause)
	{
		super(message,cause);
	}
}
