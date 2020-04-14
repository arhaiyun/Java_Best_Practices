package com.exodus.arhaiyun.alibaba;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池测试
 *
 * @author 五月的仓颉https://www.cnblogs.com/xrq730/p/10963689.html
 */
public class ThreadPoolTest {

    private static final AtomicInteger FINISH_COUNT = new AtomicInteger(0);

    private static final AtomicLong COST = new AtomicLong(0);

    private static final Integer INCREASE_COUNT = 1000000;

    private static final Integer TASK_COUNT = 1000;

    public static void testRunWithoutThreadPool() {
        List<Thread> tList = new ArrayList<Thread>(TASK_COUNT);

        for (int i = 0; i < TASK_COUNT; i++) {
            tList.add(new Thread(new IncreaseThread()));
        }

        for (Thread t : tList) {
            t.start();
        }

        for (;;);
    }

    public static void testRunWithThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        for (int i = 0; i < TASK_COUNT; i++) {
            executor.submit(new IncreaseThread());
        }

        for (;;);
    }

    private static class IncreaseThread implements Runnable {

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            AtomicInteger counter = new AtomicInteger(0);
            for (int i = 0; i < INCREASE_COUNT; i++) {
                counter.incrementAndGet();
            }
            // 累加执行时间
            COST.addAndGet(System.currentTimeMillis() - startTime);
            if (FINISH_COUNT.incrementAndGet() == TASK_COUNT) {
                System.out.println("cost: " + COST.get() + "ms");
            }
        }
    }

    public static void main(String[] args) {
//        testRunWithoutThreadPool();
//        cost: 96633ms
         testRunWithThreadPool();
//        cost: 13116ms
    }
}