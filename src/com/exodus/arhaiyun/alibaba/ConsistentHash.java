package com.exodus.arhaiyun.alibaba;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash<T> {

    /**
     * 哈希函数
     */
    private final HashFunction hashFunction;

    /**
     * 虚拟节点数 ， 越大分布越均衡，但越大，在初始化和变更的时候效率差一点。 测试中，设置200基本就均衡了。
     */
    private final int numberOfReplicas;

    /**
     * 环形Hash空间
     */
    private final SortedMap<Integer, T> circle = new TreeMap<Integer, T>();

    /**
     * @param hashFunction     ，哈希函数
     * @param numberOfReplicas ，虚拟服务器系数
     * @param nodes            ，服务器节点
     */
    public ConsistentHash(HashFunction hashFunction, int numberOfReplicas,
                          Collection<T> nodes) {
        this.hashFunction = hashFunction;
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            this.addNode(node);
        }
    }

    /**
     * 添加物理节点，每个node 会产生numberOfReplicas个虚拟节点，这些虚拟节点对应的实际节点是node
     */
    public void addNode(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hashValue = hashFunction.hash(node.toString() + i);
            circle.put(hashValue, node);
        }
    }

    /**
     * 移除物理节点，将node产生的numberOfReplicas个虚拟节点全部移除
     *
     * @param node
     */
    public void removeNode(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hashValue = hashFunction.hash(node.toString() + i);
            circle.remove(hashValue);
        }
    }

    /**
     * 得到映射的物理节点
     *
     * @param key
     * @return
     */
    public T getNode(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hashValue = hashFunction.hash(key);
//		System.out.println("key---" + key + " : hash---" + hash);
        if (!circle.containsKey(hashValue)) {
            // 返回键大于或等于hash的node，即沿环的顺时针找到一个虚拟节点
            SortedMap<Integer, T> tailMap = circle.tailMap(hashValue);
            // System.out.println(tailMap);
            // System.out.println(circle.firstKey());
            hashValue = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
//		System.out.println("hash---: " + hash);
        return circle.get(hashValue);
    }

    static class HashFunction {
        /**
         * MurMurHash算法，是非加密HASH算法，性能很高，
         * 比传统的CRC32,MD5，SHA-1（这两个算法都是加密HASH算法，复杂度本身就很高，带来的性能上的损害也不可避免）
         * 等HASH算法要快很多，而且据说这个算法的碰撞率很低. http://murmurhash.googlepages.com/
         */
        int hash(Object key) {
            ByteBuffer buf = ByteBuffer.wrap(key.toString().getBytes());
            int seed = 0x1234ABCD;

            ByteOrder byteOrder = buf.order();
            buf.order(ByteOrder.LITTLE_ENDIAN);

            long m = 0xc6a4a7935bd1e995L;
            int r = 47;

            long h = seed ^ (buf.remaining() * m);

            long k;
            while (buf.remaining() >= 8) {
                k = buf.getLong();

                k *= m;
                k ^= k >>> r;
                k *= m;

                h ^= k;
                h *= m;
            }

            if (buf.remaining() > 0) {
                ByteBuffer finish = ByteBuffer.allocate(8).order(
                        ByteOrder.LITTLE_ENDIAN);
                finish.put(buf).rewind();
                h ^= finish.getLong();
                h *= m;
            }

            h ^= h >>> r;
            h *= m;
            h ^= h >>> r;
            buf.order(byteOrder);
            return (int) h;
        }
    }
}
