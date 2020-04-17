package com.exodus.arhaiyun.fundamentals;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 21:08
 */
public class ReferencesDemo {
    public static void main(String[] args) {

        // 用SoftReference做缓存，可以有效的避免OOM问题   浏览器缓存，页面回退这样的场景
        Map<String, SoftReference<Object>> imageCache = new HashMap<String, SoftReference<Object>>();

        Object o = new Object();
//        SoftReference<Object> softReference = new SoftReference<>(o);
        WeakReference<Object> weakReference = new WeakReference<>(o);
        System.out.println(o);
//        System.out.println(softReference.get());
        System.out.println(weakReference.get());

        o = null;
        System.gc();

        System.out.println(o);
//        System.out.println(softReference.get());
        System.out.println(weakReference.get());

        try {
            byte[] bytes = new byte[30 * 1024 * 1024];
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(o);
//            System.out.println(softReference.get());
            System.out.println(weakReference.get());
        }
        /**
         java.lang.Object@34a245ab
         java.lang.Object@34a245ab
         null
         null
         Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
         at com.exodus.arhaiyun.fundamentals.ReferencesDemo.main(ReferencesDemo.java:21)
         */

    }
}
