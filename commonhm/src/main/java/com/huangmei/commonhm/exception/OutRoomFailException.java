package com.huangmei.commonhm.exception;

/**
 * 游戏已开始,退出房间失败
 *
 * @author Administrator
 */
public class OutRoomFailException extends RuntimeException {

    public OutRoomFailException() {
        super();
    }

    public OutRoomFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutRoomFailException(String message) {
        super(message);
    }

    public OutRoomFailException(Throwable cause) {
        super(cause);
    }


}
