package com.exodus.arhaiyun.fundamentals;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 21:32
 */
public class WeakHashMapDemo {
    public static void main(String[] args) {
        myHashMap();
        System.out.println("=====================");
        myWeakHashMap();
    }

    private static void myHashMap() {
        HashMap<Integer, String> map = new HashMap<>();
        Integer key = 1;
        String value = "HashMap";

        map.put(key, value);
        System.out.println(map);

        key = null;
        System.gc();

        System.out.println(map);
        System.out.println(map.size());

    }

    private static void myWeakHashMap() {
        WeakHashMap<Integer, String> weakHashMap = new WeakHashMap<>();
//        Integer key = 2; // 这种情况下不会被回收
        Integer key = new Integer(2);
        String value = "WeakHashMap";

        weakHashMap.put(key, value);
        System.out.println(weakHashMap);

        key = null;
        System.gc();

        System.out.println(weakHashMap);
        System.out.println(weakHashMap.size());
    }
}
