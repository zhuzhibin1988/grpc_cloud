package com.qd.cloud.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/27 9:13 下午
 * @Description: 注册自身实例到eureka server端，并拉取其他服务的实例信息供服务调用
 */
public class EurekaDiscoveryClient {

    private DiscoveryClient discoveryClient;

    private ApplicationInfoManager applicationInfoManager;

    public EurekaDiscoveryClient(EurekaClientConfig eurekaClientConfig, EurekaInstanceConfig eurekaInstanceConfig) {
        initDiscoveryClientInstanceInfo(eurekaInstanceConfig);
        registerDiscoveryClient(eurekaClientConfig);
    }

    /**
     * 注册discovery client
     *
     * @param eurekaClientConfig
     */
    private void registerDiscoveryClient(EurekaClientConfig eurekaClientConfig) {
        this.discoveryClient = new DiscoveryClient(this.applicationInfoManager, eurekaClientConfig);
        waitForRegistration(this.discoveryClient, this.applicationInfoManager);
        //关闭客户端立刻unregister instance
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
    }

    /**
     * 初始化eureka client instance information
     *
     * @param eurekaInstanceConfig
     */
    private void initDiscoveryClientInstanceInfo(EurekaInstanceConfig eurekaInstanceConfig) {
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(eurekaInstanceConfig).get();
        this.applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
        changeInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }

    /**
     * 等待服务注册成功
     *
     * @param discoveryClient
     * @param applicationInfoManager
     */
    private void waitForRegistration(DiscoveryClient discoveryClient, ApplicationInfoManager applicationInfoManager) {
        // my vip address to listen on
        String vipAddress = applicationInfoManager.getInfo().getVIPAddress();
        List<InstanceInfo> appServerInfos;
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            appServerInfos = discoveryClient.getInstancesByVipAddress(vipAddress, false);
        } while (appServerInfos.size() == 0);
    }

    public void changeInstanceStatus(InstanceInfo.InstanceStatus status) {
        this.applicationInfoManager.setInstanceStatus(status);
    }

    public Map<String, List<InstanceInfo>> fetchAllInstances() {
        Map<String, List<InstanceInfo>> appInstanceInfos = new HashMap<>();
        Applications applications = this.discoveryClient.getApplications();
        for (Application application : applications.getRegisteredApplications()) {
            String appName = application.getName();
            List<InstanceInfo> instanceInfos = application.getInstances();
            appInstanceInfos.put(appName, instanceInfos.stream()
                    .filter(instanceInfo -> instanceInfo.getStatus() == InstanceInfo.InstanceStatus.UP)
                    .collect(Collectors.toList()));
        }
        return appInstanceInfos;
    }

    public List<InstanceInfo> fetchAppInstances(String appName) {
        Application application = this.discoveryClient.getApplication(appName);
        return application.getInstances().stream()
                .filter(instanceInfo -> instanceInfo.getStatus() == InstanceInfo.InstanceStatus.UP)
                .collect(Collectors.toList());
    }

    public String getAppName() {
        return this.applicationInfoManager.getInfo().getAppName();
    }

    public void shutdown() {
        changeInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
        this.discoveryClient.shutdown();
    }
}
