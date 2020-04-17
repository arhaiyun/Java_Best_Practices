package com.exodus.arhaiyun.fundamentals;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 20:19
 *
 *       -XX:-PrintGCDetails
 *       jps -> jinfo -flag 配置项 进程编号
 *       jinfo -flag PrintGCDetails 102620 / jinfo -flags pid
 *       java -XX:+PrintFlagsInitial        = 初始 := 修改
 *       java -XX:+PrintFlagsFinal MetaspaceSize=512m
 *       java -XX:+PrintCommandLineFlags
 *
 *       -XX:+PrintGCDetails is deprecated. Will use -Xlog:gc* instead.
 *
 *
 *       -XX:+PrintGCDetails
 *       -XX:+UseSerialGC
 *       -XX:MetaspaceSize=1024m
 *       -XX:MaxTenuringThreshold=15
 *
 *       -Xms128m   等价于  -XX:InitialHeapSize=1073741824
 *       -Xmx128m   等价于  -XX:MaxHeapSize=1073741824
 *
 *
 *       -XX:NewSize=64m
 *       -XX:PermSize=64m
 *       -XX:+UseConcMarkSweepGC
 *       -XX:CMSInitiatingOccupancyFraction=78
 *       -XX:ThreadStackSize=128k
 *       -Xloggc:logs/gc.log
 *       -Dsun.rmi.dgc.server.gcInterval=3600000
 *       -Dsun.rmi.dgc.client.gcInterval=3600000
 *       -Dsun.rmi.server.exceptionTrace=true
 *
 *
 *
 *
 */
public class HelloGC {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello-GC");
        System.out.println(Runtime.getRuntime().availableProcessors());
        System.out.println(Runtime.getRuntime().totalMemory());
        System.out.println(Runtime.getRuntime().maxMemory());

        Thread.sleep(Long.MAX_VALUE);
    }
}
