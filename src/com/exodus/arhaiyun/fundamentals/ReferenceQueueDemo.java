package com.exodus.arhaiyun.fundamentals;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 21:45
 */
public class ReferenceQueueDemo {

    public static void main(String[] args) {
        myWeakReference();
        System.out.println("=================================");
        myPhantomReference();
    }

    private static void myWeakReference() {
        Object o = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        WeakReference<Object> weakReference = new WeakReference<>(o, referenceQueue);

        System.out.println(o);
        System.out.println(weakReference.get());
        System.out.println(referenceQueue.poll());

        System.out.println("--------------------------------");

        o = null;
        System.gc();

        System.out.println(o);
        System.out.println(weakReference.get());
        System.out.println(referenceQueue.poll());


//        java.lang.Object@34a245ab
//        java.lang.Object@34a245ab
//        null
//                --------------------------------
//        null
//        null
//        java.lang.ref.WeakReference@7cc355be
    }

    private static void myPhantomReference() {
        Object o = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        PhantomReference<Object> phantomReference = new PhantomReference<>(o, referenceQueue);

        System.out.println(o);
        System.out.println(phantomReference.get());
        System.out.println(referenceQueue.poll());

        System.out.println("--------------------------------");

        o = null;
        System.gc();

        System.out.println(o);
        System.out.println(phantomReference.get());
        System.out.println(referenceQueue.poll());

//        java.lang.Object@6e8cf4c6
//        null
//        null
//                --------------------------------
//        null
//        null
//        java.lang.ref.PhantomReference@12edcd21
    }
}
