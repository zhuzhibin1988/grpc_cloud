package com.qd.cloud.nameresolver;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.EurekaClientConfig;
import com.qd.cloud.config.EurekaConfigurationManager;
import com.qd.cloud.eureka.EurekaDiscoveryClient;
import com.qd.nlp.springcloud.proto.demo.MessageServiceGrpc;
import com.qd.nlp.springcloud.proto.demo.ReceiveMessage;
import com.qd.nlp.springcloud.proto.demo.SendMessage;
import io.grpc.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/26 4:02 下午
 * @Description:
 */
public class GrpcNameResolverTest {
    private MessageServiceGrpc.MessageServiceBlockingStub messageServiceBlockingStub;

    @Before
    public void setup() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("eureka.properties");
        properties.load(inputStream);
//        ConfigurationManager.loadProperties(properties);
        EurekaConfigurationManager.loadProperties(properties);
        String appServiceName = "helloworld-provider";
        EurekaClientConfig eurekaClientConfig = EurekaConfigurationManager.getEurekaClientConfig();
        EurekaInstanceConfig eurekaInstanceConfig = EurekaConfigurationManager.getEurekaInstanceConfig();
        System.out.println(eurekaClientConfig.getEurekaServerServiceUrls("defaultZone"));

        EurekaDiscoveryClient client = new EurekaDiscoveryClient(eurekaClientConfig, eurekaInstanceConfig);

        LoadBalancerRegistry.getDefaultRegistry().register(new RandomLoadBalancerProvider());
        NameResolverRegistry.getDefaultRegistry().register(new EurekaNameResolverProvider(client));

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forTarget("eureka:///" + appServiceName)
                .defaultLoadBalancingPolicy("random")
                .enableRetry()
//                .disableRetry()
                .usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        this.messageServiceBlockingStub = MessageServiceGrpc.newBlockingStub(channel);
    }

    @Test
    public void testEurekaNameResolverClientForever() {
        long count = 0;
        while (true) {
            try {
                ReceiveMessage response = this.messageServiceBlockingStub.message(SendMessage.newBuilder().setMessage(count++ + ":hello world").build());
                if (count % 1000 == 0) {
                    System.out.println(response);
                }
            } catch (StatusRuntimeException sre) {

            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testEurekaNameResolverClientQPS() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(30, 50, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
        int size = 10000;
        long start = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < size; i++) {
            try {
                int finalI = i;
                pool.submit(() -> {
                    ReceiveMessage response = this.messageServiceBlockingStub.message(SendMessage.newBuilder().setMessage(finalI + ":hello world").build());
                    System.out.println(response);
                    count.incrementAndGet();
                });
            } catch (StatusRuntimeException sre) {

            }
        }
        while (count.get() < size) {

        }
        long end = System.currentTimeMillis();
        System.out.println("QPS: " + size / ((end - start) / 1000));
    }
}
