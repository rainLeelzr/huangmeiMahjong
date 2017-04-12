package com.huangmei.commonhm.exception;

/**
 * 喇叭不足
 *
 * @author Administrator
 */
public class UserLackHornsException extends RuntimeException {

    public UserLackHornsException() {
        super();
    }

    public UserLackHornsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserLackHornsException(String message) {
        super(message);
    }

    public UserLackHornsException(Throwable cause) {
        super(cause);
    }


}
