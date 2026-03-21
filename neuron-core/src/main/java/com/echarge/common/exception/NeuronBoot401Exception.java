package com.echarge.common.exception;

/**
 * @Description: jeecg-boot自定义401异常
 * @author: jeecg-boot
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
