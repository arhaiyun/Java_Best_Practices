package com.exodus.arhaiyun.fundamentals;

import java.util.ArrayList;
import java.util.List;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 23:03
 */
public class StackOverflowErrorDemo {
    public static void main(String[] args) {
//        stackOverflowError(); // java.lang.StackOverflowError
//        oomError(); // java.lang.OutOfMemoryError: Java heap space
    }

    private static void stackOverflowError() {
        stackOverflowError();
    }

    private static void oomError() {
        List<byte[]> list = new ArrayList<>();
        for (; ; ) {
            byte[] bytes = new byte[1024 * 1024];
            list.add(bytes);
        }
    }
}
