package com.exodus.arhaiyun.fundamentals;

public class WaitNotifyDemo {
    MonitorObject myMonitorObject = new MonitorObject();
    boolean wasSignalled = false;

    public static class MonitorObject {
    }

    public void doWait() {
        synchronized (myMonitorObject) {
            while (!wasSignalled) {
                try {
                    myMonitorObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //clear signal and continue running.
            wasSignalled = false;
        }
    }

    public void doNotify() {
        synchronized (myMonitorObject) {
            wasSignalled = true;
            myMonitorObject.notify();
        }
    }
}
