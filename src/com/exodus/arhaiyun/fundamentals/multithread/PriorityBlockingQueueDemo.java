package com.exodus.arhaiyun.fundamentals.multithread;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/20 11:12
 */
public class PriorityBlockingQueueDemo {

    public static void main(String[] args) throws Exception {

        PriorityBlockingQueue<Task> q = new PriorityBlockingQueue<Task>();

        Task t1 = new Task();
        t1.setId(3);
        t1.setName("id为3");
        Task t2 = new Task();
        t2.setId(4);
        t2.setName("id为4");
        Task t3 = new Task();
        t3.setId(1);
        t3.setName("id为1");

        q.add(t1);    //3
        q.add(t2);    //4
        q.add(t3);  //1

        System.out.println("容器：" + q);
        System.out.println(q.take().getId());
        System.out.println("容器：" + q);

    }
}

class Task implements Comparable<Task> {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Task task) {
        return Integer.compare(task.id, this.id);
    }

    public String toString() {
        return this.id + "," + this.name;
    }
}