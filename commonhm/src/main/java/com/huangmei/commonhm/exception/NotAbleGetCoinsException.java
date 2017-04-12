package com.huangmei.commonhm.exception;

/**
 * 距离上次领取金币还没超过两小时
 *
 * @author Administrator
 */
public class NotAbleGetCoinsException extends RuntimeException {

    public NotAbleGetCoinsException() {
        super();
    }

    public NotAbleGetCoinsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAbleGetCoinsException(String message) {
        super(message);
    }

    public NotAbleGetCoinsException(Throwable cause) {
        super(cause);
    }


}
