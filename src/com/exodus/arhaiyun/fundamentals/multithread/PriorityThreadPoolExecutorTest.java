package com.exodus.arhaiyun.fundamentals.multithread;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/20 10:41
 */

public class PriorityThreadPoolExecutorTest {

    @Test
    public void testDefault() throws InterruptedException, ExecutionException {
        PriorityThreadPoolExecutor pool = new PriorityThreadPoolExecutor(1, 1000, 1, TimeUnit.MINUTES);

        Future[] futures = new Future[20];
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < futures.length; i++) {
            final int index = i;
            futures[i] = pool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    Thread.sleep(10);
                    buffer.append(index + ", ");
                    return null;
                }
            });
        }
        // 等待所有任务结束
        for (int i = 0; i < futures.length; i++) {
            futures[i].get();
        }
        System.out.println(buffer);
        assertEquals("0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, ", buffer.toString());
    }

    @Test
    public void testSamePriority() throws InterruptedException, ExecutionException {
        PriorityThreadPoolExecutor pool = new PriorityThreadPoolExecutor(1, 1000, 1, TimeUnit.MINUTES);

        Future[] futures = new Future[10];
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < futures.length; i++) {
            futures[i] = pool.submit(new TenSecondTask(i, 1, buffer), 1);
        }
        // 等待所有任务结束
        for (int i = 0; i < futures.length; i++) {
            futures[i].get();
        }
        System.out.println(buffer);
        assertEquals("01@00, 01@01, 01@02, 01@03, 01@04, 01@05, 01@06, 01@07, 01@08, 01@09, ", buffer.toString());
    }

    @Test
    public void testRandomPriority() throws InterruptedException, ExecutionException {
        PriorityThreadPoolExecutor pool = new PriorityThreadPoolExecutor(1, 1000, 1, TimeUnit.MINUTES);

        Future[] futures = new Future[20];
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < futures.length; i++) {
            int r = (int) (Math.random() * 100);
            futures[i] = pool.submit(new TenSecondTask(i, r, buffer), r);
        }
        // 等待所有任务结束
        for (int i = 0; i < futures.length; i++) {
            futures[i].get();
        }

        buffer.append("01@00");
        System.out.println(buffer);
        String[] split = buffer.toString().split(", ");
        // 从 2 开始, 因为前面的任务可能已经开始
        for (int i = 2; i < split.length - 1; i++) {
            String s = split[i].split("@")[0];
            assertTrue(Integer.valueOf(s) >= Integer.valueOf(split[i + 1].split("@")[0]));
        }
    }

    public static class TenSecondTask<T> implements Callable<T> {
        private StringBuffer buffer;
        int index;
        int priority;

        public TenSecondTask(int index, int priority, StringBuffer buffer) {
            this.index = index;
            this.priority = priority;
            this.buffer = buffer;
        }

        @Override
        public T call() throws Exception {
            Thread.sleep(10);
            buffer.append(String.format("%02d@%02d", this.priority, index)).append(", ");
            return null;
        }
    }
}
