package com.qd.cloud.config;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.EurekaClientConfig;
import com.qd.cloud.util.InetUtils;
import com.qd.cloud.util.InetUtilsProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/28 11:21 下午
 * @Description:
 */
public class EurekaConfigurationManager {
    private static EurekaInstanceConfigBean eurekaInstanceConfig = new EurekaInstanceConfigBean(new InetUtils(new InetUtilsProperties()));

    private static EurekaClientConfigBean eurekaClientConfig = new EurekaClientConfigBean();

    private static Properties properties = null;

    public static void loadProperties(Properties properties) {
        loadInstanceProperties(properties);
        loadClientProperties(properties);
    }

    private static void loadInstanceProperties(Properties properties) {
        String appName = getProperty(properties, "application.name", "grpc-cloud-java-consumer");
        eurekaInstanceConfig.setAppname(appName);
        eurekaInstanceConfig.setVirtualHostName(appName);
        String preferIpAddress = getProperty(properties, getFullProperty(EurekaInstanceConfigBean.PREFIX, "prefer-ip-address"));
        if (StringUtils.isNotEmpty(preferIpAddress)) {
            eurekaInstanceConfig.setPreferIpAddress(Boolean.parseBoolean(preferIpAddress));
        }
        String ipAddress = getProperty(properties, getFullProperty(EurekaInstanceConfigBean.PREFIX, "ip-address"));
        if (StringUtils.isNotEmpty(ipAddress)) {
            eurekaInstanceConfig.setIpAddress(ipAddress);
        }
        String port = getProperty(properties, getFullProperty(EurekaInstanceConfigBean.PREFIX, "port"));
        if (StringUtils.isNotEmpty(port)) {
            eurekaInstanceConfig.setNonSecurePort(Integer.parseInt(port));
        }
        String instanceId = getProperty(properties, getFullProperty(EurekaInstanceConfigBean.PREFIX, "instanceId"),
                eurekaInstanceConfig.getIpAddress() + ":" + eurekaInstanceConfig.getNonSecurePort());
        eurekaInstanceConfig.setInstanceId(instanceId);
        String statusPageUrlPath = getProperty(properties, getFullProperty(EurekaInstanceConfigBean.PREFIX, "status-page-url-path"));
        if (StringUtils.isNotEmpty(statusPageUrlPath)) {
            eurekaInstanceConfig.setStatusPageUrlPath(statusPageUrlPath);
        }
        String healthCheckUrlPath = getProperty(properties, getFullProperty(EurekaInstanceConfigBean.PREFIX, "health-check-url-path"));
        if (StringUtils.isNotEmpty(healthCheckUrlPath)) {
            eurekaInstanceConfig.setHealthCheckUrlPath(healthCheckUrlPath);
        }
//        eurekaInstanceConfig.setLeaseRenewalIntervalInSeconds(1);
//        eurekaInstanceConfig.setLeaseExpirationDurationInSeconds(5);
    }

    private static void loadClientProperties(Properties properties) {
        String registerWithEureka = getProperty(properties, getFullProperty(EurekaClientConfigBean.PREFIX, "register-with-eureka"));
        if (StringUtils.isNotEmpty(registerWithEureka)) {
            eurekaClientConfig.setRegisterWithEureka(Boolean.parseBoolean(registerWithEureka));
        }
        String fetchRegistry = getProperty(properties, getFullProperty(EurekaClientConfigBean.PREFIX, "fetch-registry"));
        if (StringUtils.isNotEmpty(fetchRegistry)) {
            eurekaClientConfig.setFetchRegistry(Boolean.parseBoolean(fetchRegistry));
        }
        String registryFetchIntervalSeconds = getProperty(properties, getFullProperty(EurekaClientConfigBean.PREFIX, "registry-fetch-interval-seconds"));
        if (StringUtils.isNotEmpty(registryFetchIntervalSeconds)) {
            eurekaClientConfig.setRegistryFetchIntervalSeconds(Integer.parseInt(registryFetchIntervalSeconds));
        }
        String serviceUrls = getProperty(properties, getFullProperty(EurekaClientConfigBean.PREFIX, "service-url.defaultZone"));
        if (StringUtils.isNotEmpty(serviceUrls)) {
            Map<String, String> zoneUrls = new HashMap<>();
            zoneUrls.put(EurekaClientConfigBean.DEFAULT_ZONE, serviceUrls);
            eurekaClientConfig.setServiceUrl(zoneUrls);
        }
    }

    private static String getFullProperty(String prefix, String property) {
        return prefix + "." + property;
    }

    private static String getProperty(Properties properties, String property, String defaultValue) {
        return properties.containsKey(property) ? properties.getProperty(property) : defaultValue;
    }

    private static String getProperty(Properties properties, String property) {
        return getProperty(properties, property, "");
    }

    public static EurekaInstanceConfig getEurekaInstanceConfig() {
        return eurekaInstanceConfig;
    }

    public static EurekaClientConfig getEurekaClientConfig() {
        return eurekaClientConfig;
    }
}
