package com.exodus.arhaiyun.fundamentals.multithread;

import java.util.Random;

public class ThreadLocalTest {
    public static class MyRunnable1 implements Runnable {

        private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();
        private int test = 1;

        @Override
        public void run() {
            threadLocal.set(new Random().nextInt(10));
            test++;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread() + " : " + threadLocal.get());
            System.out.println(test);
        }
    }

    public static void main(String[] args) {
        System.out.println("start");
        MyRunnable1 runnable = new MyRunnable1();
        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        thread1.start();
        thread2.start();
    }
}
