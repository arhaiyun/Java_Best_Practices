package com.exodus.arhaiyun.fundamentals;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author arhaiyun
 * @version 1.0
 * @date 2020/4/17 23:53
 * <p>
 * -XX:MetaspaceSize=20m -XX:MaxMetaspaceSize=20m
 */
public class MetaSpaceOOMTest {
    static class OOMTest {

    }

    public static void main(final String[] args) {
        int i = 0;
        try {
            while (true) {
                i++;
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(OOMTest.class);
                enhancer.setUseCache(false);
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                        return methodProxy.invokeSuper(o, args);
                    }
                });
                enhancer.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            java.lang.OutOfMemoryError: Metaspace
        }
    }
}
