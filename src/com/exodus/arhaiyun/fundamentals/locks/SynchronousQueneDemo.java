package com.exodus.arhaiyun.fundamentals.locks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 13:23
 */
public class SynchronousQueneDemo {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new SynchronousQueue<>();
        new Thread(()->{
            try {
                System.out.println(Thread.currentThread().getName()+"\t put a");
                blockingQueue.put("a");
                System.out.println(Thread.currentThread().getName()+"\t put b");
                blockingQueue.put("b");
                System.out.println(Thread.currentThread().getName()+"\t put c");
                blockingQueue.put("c");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"thread name").start();

        new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
                System.out.println(blockingQueue.take());

                TimeUnit.SECONDS.sleep(3);
                System.out.println(blockingQueue.take());

                TimeUnit.SECONDS.sleep(3);
                System.out.println(blockingQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        },"thread name").start();
    }
}
