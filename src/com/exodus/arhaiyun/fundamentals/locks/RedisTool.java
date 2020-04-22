package com.exodus.arhaiyun.fundamentals.locks;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.HashMap;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/21 18:47
 *
 * 加锁：采用支持同时设置过期时间的set方法
 * 解锁：使用lua脚本保持多个操作的原子性
 *
 * 对于线程执行时间过长，而过期时间设置太短导致锁过期后仍然会出现线程同时存在的问题，可以用守护线程给线程延长过期时间来解决
 *
 */
public class RedisTool {
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    /**
     * 尝试获取分布式锁
     *
     * @param jedis      Redis客户端
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {

        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        HashMap<String, Integer> hashMap = new HashMap<>();
        return false;
    }

    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 释放分布式锁
     *
     * @param jedis     Redis客户端
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
