package com.echarge.common.exception;

/**
 * @Description: NeuronCloud SQL注入检测异常
 * @author Edwin
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
