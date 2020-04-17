package com.exodus.arhaiyun.fundamentals.locks;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 11:54
 */
public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
//        System.out.println(blockingQueue.add("a"));
//        System.out.println(blockingQueue.add("b"));
//        System.out.println(blockingQueue.add("c"));
        // System.out.println(blockingQueue.add("d"));
        // java.lang.IllegalStateException

//        System.out.println(blockingQueue.element());
//
//        System.out.println(blockingQueue.remove("a"));
//        System.out.println(blockingQueue.remove("b"));
//        System.out.println(blockingQueue.remove("c"));
//        System.out.println(blockingQueue.remove("d"));
//
//        System.out.println(blockingQueue.offer("a"));
//        System.out.println(blockingQueue.offer("b"));
//        System.out.println(blockingQueue.offer("c"));
//        System.out.println(blockingQueue.offer("d"));
//        true
//        true
//        true
//        false
//
//        System.out.println(blockingQueue.peek());
//
//        System.out.println(blockingQueue.poll());
//        System.out.println(blockingQueue.poll());
//        System.out.println(blockingQueue.poll());
//        System.out.println(blockingQueue.poll());

//        blockingQueue.put("a");
//        blockingQueue.put("b");
//        blockingQueue.put("c");

        // blockingQueue.put("d");
//        System.out.println("=========================");
//
//        blockingQueue.take();
//        blockingQueue.take();
//        blockingQueue.take();
//        blockingQueue.take();


        // 设置超时时间
        System.out.println(blockingQueue.offer("a", 2, TimeUnit.SECONDS));
        System.out.println(blockingQueue.offer("a", 2, TimeUnit.SECONDS));
        System.out.println(blockingQueue.offer("a", 2, TimeUnit.SECONDS));
        System.out.println(blockingQueue.offer("a", 2, TimeUnit.SECONDS));


    }
}
