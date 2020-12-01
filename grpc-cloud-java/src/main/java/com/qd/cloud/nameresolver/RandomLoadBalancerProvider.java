package com.qd.cloud.nameresolver;

import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/26 4:06 下午
 * @Description:
 */

@Slf4j
public class RandomLoadBalancerProvider extends LoadBalancerProvider {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getPolicyName() {
        return "random";
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new RandomLoadBalancer(helper);
    }
}