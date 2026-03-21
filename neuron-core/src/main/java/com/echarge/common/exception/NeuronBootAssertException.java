package com.echarge.common.exception;

/**
 * jeecgboot断言异常
 * for [QQYUN-10990]AIRAG
 * @author chenrui
 * @date 2025/2/14 14:31
 */
public class NeuronBootAssertException extends NeuronBootException {
	private static final long serialVersionUID = 1L;


	public NeuronBootAssertException(String message) {
		super(message);
	}

	public NeuronBootAssertException(String message, int errCode) {
		super(message, errCode);
	}

}
