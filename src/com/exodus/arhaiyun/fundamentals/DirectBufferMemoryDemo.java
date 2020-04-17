package com.exodus.arhaiyun.fundamentals;

import java.nio.ByteBuffer;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 23:28
 * -Xms10m -Xmx10m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m
 */
public class DirectBufferMemoryDemo {
    public static void main(String[] args) {
//        System.out.println("maxDirectMemory:" + (sun.misc.VM.maxDirectMemory()));
//        -Xms10m -Xmx10m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m    -- 设置JVM参数
        ByteBuffer bf = ByteBuffer.allocateDirect(6 * 1024 * 1024);
//        java.lang.OutOfMemoryError: Direct buffer memory
    }
}
