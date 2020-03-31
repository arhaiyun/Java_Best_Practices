package com.exodus.arhaiyun.Fundamentals;

import java.util.concurrent.*;

public class DemoDrafts {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("start main thread ");
        ExecutorService exec = Executors.newFixedThreadPool(2);
        //新建一个Callable 任务，并将其提交到一个ExecutorService. 将返回一个描述任务情况的Future.
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("start new thread ");
                Thread.sleep(5000);
                System.out.println("end new thread ");
                return "我是返回的内容";
            }
        };
        Future<String> task = exec.submit(call);
        Thread.sleep(1000);
        String retn = task.get();
        //关闭线程池
        exec.shutdown();
        System.out.println(retn + "--end main thread");
    }
}
