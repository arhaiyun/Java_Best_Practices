package com.exodus.arhaiyun.alibaba;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池基类
 *
 * @author 五月的仓颉https://www.cnblogs.com/xrq730/p/10963689.html
 */
public class BaseHttpClientTest {

    protected static final int REQUEST_COUNT = 5;

    protected static final String SEPERATOR = "   ";

    protected static final AtomicInteger NOW_COUNT = new AtomicInteger(0);

    protected static final StringBuilder EVERY_REQ_COST = new StringBuilder(200);

    /**
     * 获取待运行的线程
     */
    protected List<Thread> getRunThreads(Runnable runnable) {
        List<Thread> tList = new ArrayList<Thread>(REQUEST_COUNT);

        for (int i = 0; i < REQUEST_COUNT; i++) {
            tList.add(new Thread(runnable));
        }

        return tList;
    }

    /**
     * 启动所有线程
     */
    protected void startUpAllThreads(List<Thread> tList) {
        for (Thread t : tList) {
            t.start();
            // 这里需要加一点延迟，保证请求按顺序发出去
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected synchronized void addCost(long cost) {
        EVERY_REQ_COST.append(cost);
        EVERY_REQ_COST.append("ms");
        EVERY_REQ_COST.append(SEPERATOR);
    }

}