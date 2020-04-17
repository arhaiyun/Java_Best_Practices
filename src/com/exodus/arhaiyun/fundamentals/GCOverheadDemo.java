package com.exodus.arhaiyun.fundamentals;

import java.util.ArrayList;
import java.util.List;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 23:12
 *
 * -Xms10m -Xmx10m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m
 *
 */
public class GCOverheadDemo {
    public static void main(String[] args) {
        int i = 0;
        List<String> list = new ArrayList<>();
        try {
            while (true) {
                list.add(String.valueOf(++i).intern()); // java.lang.OutOfMemoryError: Java heap space
            }
        } catch (Exception e) {
            System.out.println("=================== i = " + i);
            e.printStackTrace();
        }
    }
}
