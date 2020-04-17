package com.exodus.arhaiyun.fundamentals.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 13:33
 */

class ShareData {
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void increment() {
        try {
            lock.lock();

//            if (number != 0) {
            while (number != 0) {
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void decrement() {
        try {
            lock.lock();

//            if (number == 0) {
            while (number == 0) {
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}

public class TraditionProducer {
    public static void main(String[] args) {
        ShareData shareData = new ShareData();
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                shareData.increment();
            }
        }, "Producer").start();

        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                shareData.decrement();
            }
        }, "Consumer").start();
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                shareData.increment();
            }
        }, "Producer-2").start();

        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                shareData.decrement();
            }
        }, "Consumer-2").start();
    }
}
