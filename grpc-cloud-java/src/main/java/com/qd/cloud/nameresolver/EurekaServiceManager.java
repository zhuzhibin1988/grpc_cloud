package com.qd.cloud.nameresolver;

import com.netflix.appinfo.InstanceInfo;
import io.grpc.EquivalentAddressGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/12/1 2:18 下午
 * @Description:
 */
public class EurekaServiceManager {
    @Setter
    @Getter
    private final static Map<EquivalentAddressGroup, InstanceInfo> addressUpInstance = new ConcurrentHashMap<>();
}
