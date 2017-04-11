package com.huangmei.interfaces.monitor.schedule;

import com.huangmei.interfaces.monitor.MonitorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 广播游戏开始
 */
public class BroadcastGameStartTask implements MonitorTask {

    private static final Logger log = LoggerFactory.getLogger(BroadcastGameStartTask.class);

    /**
     * 任务名称
     */
    protected String taskName;

    /**
     * 监控任务成功时，执行的方法
     */
    protected Runnable successCallback;

    /**
     * 监控任务失败时，执行的方法
     */
    protected Runnable failCallback;

    /**
     * 监控任务结束后，不管任务成功与否，都需要执行的回调方法
     */
    protected Runnable finishCallback;

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public void run() {
        try {

            success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail();
        } finally {
            finish();
        }

    }

    @Override
    public void setSuccessCallback(Runnable success) {
        this.successCallback = success;
    }

    @Override
    public void setFailCallback(Runnable fail) {
        this.failCallback = fail;
    }

    @Override
    public void setFinishCallback(Runnable finish) {
        this.finishCallback = finish;
    }

    private void success() {
        if (successCallback != null) {
            successCallback.run();
        }
    }

    private void fail() {
        if (failCallback != null) {
            failCallback.run();
        }
    }

    private void finish() {
        if (finishCallback != null) {
            finishCallback.run();
        }
    }
}
