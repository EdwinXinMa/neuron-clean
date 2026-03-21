package com.echarge.common.exception;

/**
 * @Description: jeecg-boot自定义SQL注入异常
 * @author: jeecg-boot
 */
public class NeuronSqlInjectionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NeuronSqlInjectionException(String message){
		super(message);
	}
	
	public NeuronSqlInjectionException(Throwable cause)
	{
		super(cause);
	}
	
	public NeuronSqlInjectionException(String message, Throwable cause)
	{
		super(message,cause);
	}
}
