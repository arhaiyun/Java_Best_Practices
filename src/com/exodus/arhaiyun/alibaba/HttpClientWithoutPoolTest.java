package com.exodus.arhaiyun.alibaba;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

/**
 * 不使用连接池测试
 *
 * @author 五月的仓颉https://www.cnblogs.com/xrq730/p/10963689.html
 */
public class HttpClientWithoutPoolTest extends BaseHttpClientTest {

    @Test
    public void test() throws Exception {
        startUpAllThreads(getRunThreads(new HttpThread()));
//        341ms   342ms   342ms   341ms   380ms
        // 等待线程运行
        for (;;);
    }

    private class HttpThread implements Runnable {

        @Override
        public void run() {
            /**
             * HttpClient是线程安全的，因此HttpClient正常使用应当做成全局变量，但是一旦全局共用一个，HttpClient内部构建的时候会new一个连接池
             * 出来，这样就体现不出使用连接池的效果，因此这里每次new一个HttpClient，保证每次都不通过连接池请求对端
             */
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpGet httpGet = new HttpGet("https://www.baidu.com/");

            long startTime = System.currentTimeMillis();
            try {
                CloseableHttpResponse response = httpClient.execute(httpGet);
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                addCost(System.currentTimeMillis() - startTime);

                if (NOW_COUNT.incrementAndGet() == REQUEST_COUNT) {
                    System.out.println(EVERY_REQ_COST.toString());
                }
            }
        }
    }
}
