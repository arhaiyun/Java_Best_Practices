package com.exodus.arhaiyun.fundamentals.locks;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 11:34
 */
public class CyclicBarrierDemo {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("==========> 召唤神龙");
        });

        for (int i = 1; i <= 7; i++) {
            final int tempInt = i;
            new Thread(()->{
                System.out.println("收集到第：" + tempInt + "龙珠");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();

        }
    }
}
