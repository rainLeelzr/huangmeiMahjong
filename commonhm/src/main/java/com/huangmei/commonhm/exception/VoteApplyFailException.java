package com.huangmei.commonhm.exception;

/**
 * 距离上一次投票时间不够两分钟,不能再次发起投票
 *
 * @author Administrator
 */
public class VoteApplyFailException extends RuntimeException {

    public VoteApplyFailException() {
        super();
    }

    public VoteApplyFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public VoteApplyFailException(String message) {
        super(message);
    }

    public VoteApplyFailException(Throwable cause) {
        super(cause);
    }


}
