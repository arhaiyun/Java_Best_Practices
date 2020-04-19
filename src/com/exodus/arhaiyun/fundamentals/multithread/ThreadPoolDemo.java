package com.exodus.arhaiyun.fundamentals.multithread;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 17:37
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        ExecutorService threadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(4), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        try {
            for (int i = 0; i < 12; i++) {
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t办理业务");
                });
//                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

    }

    private static void executorsThreadPoolTest() {
        //ExecutorService threadPool = Executors.newFixedThreadPool(5);
        //ExecutorService threadPool = Executors.newSingleThreadExecutor();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            for (int i = 0; i < 10; i++) {
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t办理业务");
                });
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}
