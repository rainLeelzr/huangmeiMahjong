package com.huangmei.commonhm.exception;

/**
 * 推广码不存在
 *
 * @author Administrator
 */
public class PromoteCodeNotExistException extends RuntimeException {

    public PromoteCodeNotExistException() {
        super();
    }

    public PromoteCodeNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromoteCodeNotExistException(String message) {
        super(message);
    }

    public PromoteCodeNotExistException(Throwable cause) {
        super(cause);
    }


}
