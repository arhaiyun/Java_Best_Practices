package com.exodus.arhaiyun.fundamentals.locks;

public class ExodusLock {

    public boolean acquire() {
        Thread current = Thread.currentThread();
        return true;
    }
}
