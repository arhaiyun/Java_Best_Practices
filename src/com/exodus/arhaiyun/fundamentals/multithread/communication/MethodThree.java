package com.exodus.arhaiyun.fundamentals.multithread.communication;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/20 11:28
 */
// 利用volatile
public class MethodThree {
    private volatile ThreadToGo threadToGo = new ThreadToGo();

    static class ThreadToGo {
        int value = 1;
    }

    public Runnable newThreadOne() {
        final String[] inputArr = Helper.buildNoArr(52);
        return new Runnable() {
            private String[] arr = inputArr;

            public void run() {
                for (int i = 0; i < arr.length; i = i + 2) {
                    while (threadToGo.value == 2) {
                    }
                    Helper.print(arr[i], arr[i + 1]);
                    threadToGo.value = 2;
                }
            }
        };
    }

    public Runnable newThreadTwo() {
        final String[] inputArr = Helper.buildCharArr(26);
        return new Runnable() {
            private String[] arr = inputArr;

            public void run() {
                for (String s : arr) {
                    while (threadToGo.value == 1) {
                    }
                    Helper.print(s);
                    threadToGo.value = 1;
                }
            }
        };
    }

    public static void main(String args[]) throws InterruptedException {
        MethodThree three = new MethodThree();
        Helper.instance.run(three.newThreadOne());
        Helper.instance.run(three.newThreadTwo());
        Helper.instance.shutdown();
    }
}