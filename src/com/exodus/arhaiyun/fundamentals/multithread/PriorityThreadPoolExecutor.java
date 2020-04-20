package com.exodus.arhaiyun.fundamentals.multithread;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/20 10:36
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger log = LoggerFactory.getLogger(PriorityThreadPoolExecutor.class);

    private ThreadLocal<Integer> local = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue());
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), threadFactory);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), handler);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), threadFactory, handler);
    }

    protected static PriorityBlockingQueue getWorkQueue() {
        return new PriorityBlockingQueue();
    }

    @Override
    public void execute(Runnable command) {
        int priority = local.get();
        try {
            this.execute(command, priority);
        } finally {
            local.set(0);
        }
    }

    public void execute(Runnable command, int priority) {
        super.execute(new PriorityRunnable(command, priority));
    }

    public <T> Future<T> submit(Callable<T> task, int priority) {
        local.set(priority);
        return super.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result, int priority) {
        local.set(priority);
        return super.submit(task, result);
    }

    public Future<?> submit(Runnable task, int priority) {
        local.set(priority);
        return super.submit(task);
    }

    protected static class PriorityRunnable<E extends Comparable<? super E>> implements Runnable, Comparable<PriorityRunnable<E>> {
        private final static AtomicLong seq = new AtomicLong();
        private final long seqNum;
        Runnable run;
        private int priority;

        public PriorityRunnable(Runnable run, int priority) {
            seqNum = seq.getAndIncrement();
            this.run = run;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public Runnable getRun() {
            return run;
        }

        @Override
        public void run() {
            this.run.run();
        }

        @Override
        public int compareTo(PriorityRunnable<E> other) {
            int res = 0;
            if (this.priority == other.priority) {
                if (other.run != this.run) {// ASC
                    res = (seqNum < other.seqNum ? -1 : 1);
                }
            } else {// DESC
                res = this.priority > other.priority ? -1 : 1;
            }
            return res;
        }
    }
}
