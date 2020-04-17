package com.exodus.arhaiyun.fundamentals.locks;

public class DeadLockDemo implements Runnable {

    public static Object obj1 = new Object();
    public static Object obj2 = new Object();

    public int flag = 1;    // 1 or 2

    @Override
    public void run() {

        if (flag == 1) {
            synchronized (obj1) {
                System.out.println("flag: " + flag + ", 锁住了资源obj1");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("flag: " + flag + ", 等待获取资源obj2");
                synchronized (obj2) {
                    System.out.println("flag: " + flag + ", 获得资源obj2");
                }
            }
        } else if (flag == 2) {
            synchronized (obj2) {
                System.out.println("flag: " + flag + ", 锁住了资源obj2");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("flag: " + flag + ", 等待获取资源obj1");
                synchronized (obj1) {
                    System.out.println("flag: " + flag + ", 获得资源obj1");
                }
            }
        }
    }

    public static void main(String[] args) {

        DeadLockDemo p1 = new DeadLockDemo();
        DeadLockDemo p2 = new DeadLockDemo();
        p1.flag = 1;
        p2.flag = 2;

        new Thread(p1).start();
        new Thread(p2).start();
    }
}
