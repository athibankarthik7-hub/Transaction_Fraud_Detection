package com.fraudengine.concurrency;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom ThreadFactory providing meaningful worker thread names
 * for compliance audit tracking and telemetry inspection.
 */
public class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger threadCount = new AtomicInteger(1);
    private final boolean daemon;

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, prefix + "-" + threadCount.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }
}
