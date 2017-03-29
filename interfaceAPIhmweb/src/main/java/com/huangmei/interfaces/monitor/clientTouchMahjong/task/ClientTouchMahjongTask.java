package com.huangmei.interfaces.monitor.clientTouchMahjong.task;

import com.huangmei.interfaces.monitor.MonitorTask;

/**
 * 客户端摸牌操作
 * 从剩下的牌中抽出最后一张牌，并判断玩家可以的操作，返回给给客户端
 */
public class ClientTouchMahjongTask implements MonitorTask {

    private static final String taskName = "客户端摸牌";

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public void run() {

    }

    @Override
    public void setSuccessCallback(Runnable success) {

    }

    @Override
    public void setFailCallback(Runnable fail) {

    }

    @Override
    public void setFinishCallback(Runnable finish) {

    }
}
