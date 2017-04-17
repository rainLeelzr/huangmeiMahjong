package com.huangmei.commonhm.exception;

/**
 * 已经绑定推广码
 *
 * @author Administrator
 */
public class AlreadyBindPromoteCodeException extends RuntimeException {

    public AlreadyBindPromoteCodeException() {
        super();
    }

    public AlreadyBindPromoteCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyBindPromoteCodeException(String message) {
        super(message);
    }

    public AlreadyBindPromoteCodeException(Throwable cause) {
        super(cause);
    }


}
