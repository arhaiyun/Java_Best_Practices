package com.exodus.arhaiyun.fundamentals.locks;

import com.exodus.arhaiyun.fundamentals.binarytree.TreeNode;
import org.junit.experimental.theories.Theories;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 16:31
 */

class ShareResource2 {
    private volatile boolean FLAG = true;
    private AtomicInteger atomicInteger = new AtomicInteger();

    BlockingQueue<String> blockingQueue = null;

    public ShareResource2(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
        System.out.println(blockingQueue.getClass().getName());
    }

    public void myProducer() throws InterruptedException {
        String data = null;
        boolean retValue;
        while (FLAG) {
            data = "" + atomicInteger.incrementAndGet();
            retValue = blockingQueue.offer(data, 2, TimeUnit.SECONDS);
            if (retValue) {
                System.out.println(Thread.currentThread().getName() + "\t插入队列" + data + "成功！");
            } else {
                System.out.println(Thread.currentThread().getName() + "\t插入队列" + data + "失败！");
            }
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "\t停止生产！");
    }

    public void myConsumer() throws InterruptedException {
        String result = null;
        while (FLAG) {
            result = blockingQueue.poll(2, TimeUnit.SECONDS);
            if (result == null || result.equalsIgnoreCase("")) {
                FLAG = false;
                System.out.println(Thread.currentThread().getName() + "\t超过2秒钟没有取到蛋糕！");
            }
            System.out.println(Thread.currentThread().getName() + "\t消费队列蛋糕" + result + "成功！");

            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "\t停止消费！");
    }

    public void stop() {
        this.FLAG = false;
    }
}

public class BlockingQueueProdConsumer {
    public static void main(String[] args) {
        ShareResource2 resource = new ShareResource2(new ArrayBlockingQueue<>(10));
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t生产线程启动");
            try {
                resource.myProducer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Producer").start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t消费线程启动");
            try {
                resource.myConsumer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Consumer").start();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + "\t老板叫停生产");
        resource.stop();
    }
}
