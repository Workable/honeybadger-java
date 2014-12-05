package com.workable.honeybadger;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory generating daemon threads with a custom priority.
 */
public class DaemonThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int priority;

    public DaemonThreadFactory(int priority) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "raven-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (!t.isDaemon())
            t.setDaemon(true);
        if (t.getPriority() != priority)
            t.setPriority(priority);
        return t;
    }
}
