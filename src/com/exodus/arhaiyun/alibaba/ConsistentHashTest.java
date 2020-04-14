package com.exodus.arhaiyun.alibaba;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ConsistentHashTest {

    public static void main(String[] args) {
        HashSet<String> serverNode = new HashSet<String>();
        serverNode.add("127.1.1.1#A");
        serverNode.add("127.2.2.2#B");
        serverNode.add("127.3.3.3#C");
        serverNode.add("127.4.4.4#D");

        Map<String, Integer> serverNodeMap = new HashMap<String, Integer>();

        ConsistentHash<String> consistentHash = new ConsistentHash<String>(
                new ConsistentHash.HashFunction(), 200, serverNode);

        int count = 50000;

        for (int i = 0; i < count; i++) {
            String serverNodeName = consistentHash.getNode(i);
            // System.out.println(i + " 映射到物理节点---" + serverNodeName);
            if (serverNodeMap.containsKey(serverNodeName)) {
                serverNodeMap.put(serverNodeName,
                        serverNodeMap.get(serverNodeName) + 1);
            } else {
                serverNodeMap.put(serverNodeName, 1);
            }
        }
        // System.out.println(serverNodeMap);

        showServer(serverNodeMap);
        serverNodeMap.clear();

        consistentHash.removeNode("127.1.1.1#A");
        System.out.println("-------------------- remove 127.1.1.1#A");

        for (int i = 0; i < count; i++) {
            String serverNodeName = consistentHash.getNode(i);
            // System.out.println(i + " 映射到物理节点---" + serverNodeName);
            if (serverNodeMap.containsKey(serverNodeName)) {
                serverNodeMap.put(serverNodeName,
                        serverNodeMap.get(serverNodeName) + 1);
            } else {
                serverNodeMap.put(serverNodeName, 1);
            }
        }

        showServer(serverNodeMap);
        serverNodeMap.clear();

        consistentHash.addNode("127.5.5.5#E");
        System.out.println("-------------------- add 127.5.5.5#E");

        for (int i = 0; i < count; i++) {
            String serverNodeName = consistentHash.getNode(i);
            // System.out.println(i + " 映射到物理节点---" + serverNodeName);
            if (serverNodeMap.containsKey(serverNodeName)) {
                serverNodeMap.put(serverNodeName,
                        serverNodeMap.get(serverNodeName) + 1);
            } else {
                serverNodeMap.put(serverNodeName, 1);
            }
        }

        showServer(serverNodeMap);
        serverNodeMap.clear();

        consistentHash.addNode("127.6.6.6#F");
        System.out.println("-------------------- add 127.6.6.6#F");
        count *= 2;
        System.out.println("-------------------- 业务量加倍");
        for (int i = 0; i < count; i++) {
            String serverNodeName = consistentHash.getNode(i);
            // System.out.println(i + " 映射到物理节点---" + serverNodeName);
            if (serverNodeMap.containsKey(serverNodeName)) {
                serverNodeMap.put(serverNodeName,
                        serverNodeMap.get(serverNodeName) + 1);
            } else {
                serverNodeMap.put(serverNodeName, 1);
            }
        }
        showServer(serverNodeMap);

    }

    /**
     * 服务器运行状态
     *
     * @param map
     */
    public static void showServer(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> m : map.entrySet()) {
            System.out.println(m.getKey() + ", 存储数据量 " + m.getValue());
        }
    }
}