package com.exodus.arhaiyun.Fundamentals.multithread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        int numProducers = 4;
        int numConsumers = 3;

        BlockingQueue<Object> myQueue = new LinkedBlockingQueue<Object>(5);
        for (int i = 0; i < numProducers; i++) {
            new Thread(new BlockingQueueProducer(myQueue)).start();
        }
        for (int i = 0; i < numConsumers; i++) {
            new Thread(new BlockingQueueConsumer(myQueue)).start();
        }

        Thread.sleep(1000);
        System.exit(0);
    }
}
