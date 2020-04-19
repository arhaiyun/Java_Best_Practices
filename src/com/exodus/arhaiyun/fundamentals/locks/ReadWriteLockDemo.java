package com.exodus.arhaiyun.fundamentals.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 10:29
 */
public class ReadWriteLockDemo {
    public static void main(String[] args) {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        rwLock.writeLock().lock();
        rwLock.readLock().lock();
        rwLock.readLock().unlock();
        rwLock.writeLock().unlock();
    }
}
