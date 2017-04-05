package com.huangmei.commonhm.exception;

/**
 * 用户不存在
 *
 * @author Administrator
 */
public class AlreadyGetCoinsException extends RuntimeException {

    public AlreadyGetCoinsException() {
        super();
    }

    public AlreadyGetCoinsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyGetCoinsException(String message) {
        super(message);
    }

    public AlreadyGetCoinsException(Throwable cause) {
        super(cause);
    }


}
