package com.huangmei.commonhm.exception;

/**
 * 用户不存在
 * @author Administrator
 *
 */
public class AlreadyDrawCoinsException extends RuntimeException {

	public AlreadyDrawCoinsException() {
		super();
	}

	public AlreadyDrawCoinsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyDrawCoinsException(String message) {
		super(message);
	}

	public AlreadyDrawCoinsException(Throwable cause) {
		super(cause);
	}


}
