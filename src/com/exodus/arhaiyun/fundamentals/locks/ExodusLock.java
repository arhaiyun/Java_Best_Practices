package com.exodus.arhaiyun.fundamentals.locks;

import sun.misc.Unsafe;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class ExodusLock {
    /**
     * 当前锁状态
     */
    private volatile int state = 0;
    /**
     * 当前持有锁的线程
     */
    private Thread lockHolder;

    private ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Thread getLockHolder() {
        return lockHolder;
    }

    public void setLockHolder(Thread lockHolder) {
        this.lockHolder = lockHolder;
    }

    public boolean acquire() {
        Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if ((waiters.size() == 0 || waiters.peek() == current) && compareAndSwapState(0, 1)) {
                setLockHolder(current);
                waiters.poll();
                return true;
            }
        }
        return false;
    }

    public void lock() {
        if (acquire()) {
            return;
        }

        Thread current = Thread.currentThread();
        waiters.add(current);

        for (; ; ) {
            if (acquire()) {
                return;
            }
            LockSupport.park(current);
        }
    }

    public void unlock() {
        if (Thread.currentThread() != lockHolder) {
            throw new RuntimeException("Lockholder is not current thread");
        }
        int state = getState();
        if (compareAndSwapState(state, 0)) {
            setLockHolder(null);
            Thread first = waiters.peek();
            if (first != null) {
                LockSupport.unpark(first);
            }
        }
    }

    private static final Unsafe unsafe = UnsafeInstance.reflectGetUnsafe();
    private static long stateOffset = 0;

    static {
        try {
            assert unsafe != null;
            stateOffset = unsafe.objectFieldOffset(ExodusLock.class.getDeclaredField("state"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public final boolean compareAndSwapState(int except, int update) {
        assert unsafe != null;
        return unsafe.compareAndSwapInt(this, stateOffset, except, update);
    }

}
