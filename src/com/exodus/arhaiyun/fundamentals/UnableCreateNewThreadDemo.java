package com.exodus.arhaiyun.fundamentals;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 23:43
 */
public class UnableCreateNewThreadDemo {
    public static void main(String[] args) {
        for (int i = 0; ; i++) {
            int finalI = i;
            new Thread(() -> {
                System.out.println("============ i:" + finalI);
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "" + i).start();
        }
    }
}
