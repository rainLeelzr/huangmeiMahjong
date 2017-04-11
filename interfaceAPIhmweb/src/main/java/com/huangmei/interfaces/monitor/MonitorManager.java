package com.huangmei.interfaces.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 任务监控管理器
 */
@Component
public class MonitorManager {

    private static final Logger log = LoggerFactory.getLogger(MonitorManager.class);

    //private static final ExecutorService executor = Executors.newFixedThreadPool(20);

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);

    public void watch(MonitorTask monitorTask) {
        if (monitorTask == null) {
            throw new IllegalArgumentException("monitorTask不能为null");
        }

        executor.submit(monitorTask);
    }

    /**
     * 在delay毫秒后，执行一次任务
     */
    public void schedule(MonitorTask monitorTask, long delay) {
        if (monitorTask == null) {
            throw new IllegalArgumentException("monitorTask不能为null");
        }

        executor.schedule(monitorTask, delay, TimeUnit.MILLISECONDS);
    }
}
