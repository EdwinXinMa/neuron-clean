package com.echarge.common.exception;

/**
 * @Description: NeuronCloud 认证失败异常(401)
 * @author Edwin
 */
public class NeuronBoot401Exception extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NeuronBoot401Exception(String message){
		super(message);
	}

	public NeuronBoot401Exception(Throwable cause)
	{
		super(cause);
	}

	public NeuronBoot401Exception(String message, Throwable cause)
	{
		super(message,cause);
	}
}
