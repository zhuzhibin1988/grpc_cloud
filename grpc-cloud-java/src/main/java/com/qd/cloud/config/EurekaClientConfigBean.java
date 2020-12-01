package com.qd.cloud.config;

import com.netflix.appinfo.EurekaAccept;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.CommonConstants;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.internal.util.Archaius1Utils;
import com.netflix.discovery.shared.transport.DefaultEurekaTransportConfig;
import com.netflix.discovery.shared.transport.EurekaTransportConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/27 4:04 下午
 * @Description:
 */

public class EurekaClientConfigBean implements EurekaClientConfig {
    public static final String PREFIX = "eureka.client";

    public static final String DEFAULT_URL = "http://localhost:8761/eureka/";

    public static final String DEFAULT_ZONE = "defaultZone";

    private static final int MINUTES = 60;

    private final DynamicPropertyFactory configInstance;

    private final EurekaTransportConfig transportConfig;

    private boolean enabled;

    private int registryFetchIntervalSeconds;

    private int instanceInfoReplicationIntervalSeconds;

    private int initialInstanceInfoReplicationIntervalSeconds;

    private int eurekaServiceUrlPollIntervalSeconds;

    private String proxyPort;

    private String proxyHost;

    private String proxyUserName;

    private String proxyPassword;

    private int eurekaServerReadTimeoutSeconds;

    private int eurekaServerConnectTimeoutSeconds;

    private String backupRegistryImpl;

    private int eurekaServerTotalConnections;

    private int eurekaServerTotalConnectionsPerHost;

    private String eurekaServerURLContext;

    private String eurekaServerPort;

    private String eurekaServerDNSName;

    private String region;

    private int eurekaConnectionIdleTimeoutSeconds;

    private String registryRefreshSingleVipAddress;

    private int heartbeatExecutorThreadPoolSize;

    private int heartbeatExecutorExponentialBackOffBound;

    private int cacheRefreshExecutorThreadPoolSize;

    private int cacheRefreshExecutorExponentialBackOffBound;

    private Map<String, String> serviceUrl;

    private boolean gZipContent;

    private boolean useDnsForFetchingServiceUrls;

    private boolean registerWithEureka;

    private boolean preferSameZoneEureka;

    private boolean logDeltaDiff;

    private boolean disableDelta;

    private String fetchRemoteRegionsRegistry;

    private Map<String, String> availabilityZones;

    private boolean filterOnlyUpInstances;

    private boolean fetchRegistry;

    private String dollarReplacement;

    private String escapeCharReplacement;

    private boolean allowRedirects;

    private boolean onDemandUpdateStatusChange;

    private String encoderName;

    private String decoderName;

    private String clientDataAccept;

    private boolean shouldUnregisterOnShutdown;

    private boolean shouldEnforceRegistrationAtInit;

    public EurekaClientConfigBean() {
        this.enabled = true;
        configInstance = Archaius1Utils.initConfig(CommonConstants.CONFIG_FILE_NAME);
        this.transportConfig = new DefaultEurekaTransportConfig(null, configInstance);
        this.registryFetchIntervalSeconds = 30;
        this.instanceInfoReplicationIntervalSeconds = 30;
        this.initialInstanceInfoReplicationIntervalSeconds = 40;
        this.eurekaServiceUrlPollIntervalSeconds = 300;
        this.eurekaServerReadTimeoutSeconds = 8;
        this.eurekaServerConnectTimeoutSeconds = 5;
        this.eurekaServerTotalConnections = 200;
        this.eurekaServerTotalConnectionsPerHost = 50;
        this.region = "us-east-1";
        this.eurekaConnectionIdleTimeoutSeconds = 30;
        this.heartbeatExecutorThreadPoolSize = 2;
        this.heartbeatExecutorExponentialBackOffBound = 10;
        this.cacheRefreshExecutorThreadPoolSize = 2;
        this.cacheRefreshExecutorExponentialBackOffBound = 10;
        this.serviceUrl = new HashMap<>();
        this.serviceUrl.put(DEFAULT_ZONE, DEFAULT_URL);
        this.gZipContent = true;
        this.useDnsForFetchingServiceUrls = false;
        this.registerWithEureka = true;
        this.preferSameZoneEureka = true;
        this.availabilityZones = new HashMap<>();
        this.filterOnlyUpInstances = true;
        this.fetchRegistry = true;
        this.dollarReplacement = "_-";
        this.escapeCharReplacement = "__";
        this.allowRedirects = false;
        this.onDemandUpdateStatusChange = true;
        this.clientDataAccept = EurekaAccept.full.name();
        this.shouldUnregisterOnShutdown = true;
        this.shouldEnforceRegistrationAtInit = false;
    }

    @Override
    public boolean shouldGZipContent() {
        return this.gZipContent;
    }

    @Override
    public boolean shouldUseDnsForFetchingServiceUrls() {
        return this.useDnsForFetchingServiceUrls;
    }

    @Override
    public boolean shouldRegisterWithEureka() {
        return this.registerWithEureka;
    }

    @Override
    public boolean shouldPreferSameZoneEureka() {
        return this.preferSameZoneEureka;
    }

    @Override
    public boolean shouldLogDeltaDiff() {
        return this.logDeltaDiff;
    }

    @Override
    public boolean shouldDisableDelta() {
        return this.disableDelta;
    }

    @Override
    public boolean shouldUnregisterOnShutdown() {
        return this.shouldUnregisterOnShutdown;
    }

    @Override
    public boolean shouldEnforceRegistrationAtInit() {
        return this.shouldEnforceRegistrationAtInit;
    }

    @Override
    public String fetchRegistryForRemoteRegions() {
        return this.fetchRemoteRegionsRegistry;
    }

    @Override
    public String[] getAvailabilityZones(String region) {
        String value = this.availabilityZones.get(region);
        if (value == null)
            value = DEFAULT_ZONE;
        return value.split(",");
    }

    @Override
    public List<String> getEurekaServerServiceUrls(String myZone) {
        String serviceUrls = this.serviceUrl.get(myZone);
        if (serviceUrls == null || serviceUrls.isEmpty())
            serviceUrls = this.serviceUrl.get(DEFAULT_ZONE);
        if (!StringUtils.isEmpty(serviceUrls)) {
            String[] serviceUrlsSplit = StringUtils.split(serviceUrls, ",");
            List<String> eurekaServiceUrls = new ArrayList<>(serviceUrlsSplit.length);
            for (String eurekaServiceUrl : serviceUrlsSplit) {
                if (!endsWithSlash(eurekaServiceUrl))
                    eurekaServiceUrl = eurekaServiceUrl + "/";
                eurekaServiceUrls.add(eurekaServiceUrl.trim());
            }
            return eurekaServiceUrls;
        }
        return Collections.EMPTY_LIST;
    }

    private boolean endsWithSlash(String url) {
        return url.endsWith("/");
    }

    @Override
    public boolean shouldFilterOnlyUpInstances() {
        return this.filterOnlyUpInstances;
    }

    @Override
    public boolean shouldFetchRegistry() {
        return this.fetchRegistry;
    }

    @Override
    public boolean allowRedirects() {
        return this.allowRedirects;
    }

    @Override
    public boolean shouldOnDemandUpdateStatusChange() {
        return this.onDemandUpdateStatusChange;
    }

    @Override
    public String getExperimental(String name) {
        return null;
    }

    @Override
    public EurekaTransportConfig getTransportConfig() {
        return getTransport();
    }


    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EurekaTransportConfig getTransport() {
        return this.transportConfig;
    }

    @Override
    public int getRegistryFetchIntervalSeconds() {
        return this.registryFetchIntervalSeconds;
    }

    public void setRegistryFetchIntervalSeconds(int registryFetchIntervalSeconds) {
        this.registryFetchIntervalSeconds = registryFetchIntervalSeconds;
    }

    @Override
    public int getInstanceInfoReplicationIntervalSeconds() {
        return this.instanceInfoReplicationIntervalSeconds;
    }

    public void setInstanceInfoReplicationIntervalSeconds(int instanceInfoReplicationIntervalSeconds) {
        this.instanceInfoReplicationIntervalSeconds = instanceInfoReplicationIntervalSeconds;
    }

    @Override
    public int getInitialInstanceInfoReplicationIntervalSeconds() {
        return this.initialInstanceInfoReplicationIntervalSeconds;
    }

    public void setInitialInstanceInfoReplicationIntervalSeconds(int initialInstanceInfoReplicationIntervalSeconds) {
        this.initialInstanceInfoReplicationIntervalSeconds = initialInstanceInfoReplicationIntervalSeconds;
    }

    @Override
    public int getEurekaServiceUrlPollIntervalSeconds() {
        return this.eurekaServiceUrlPollIntervalSeconds;
    }

    public void setEurekaServiceUrlPollIntervalSeconds(int eurekaServiceUrlPollIntervalSeconds) {
        this.eurekaServiceUrlPollIntervalSeconds = eurekaServiceUrlPollIntervalSeconds;
    }

    @Override
    public String getProxyPort() {
        return this.proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Override
    public String getProxyHost() {
        return this.proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    @Override
    public String getProxyUserName() {
        return this.proxyUserName;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    @Override
    public String getProxyPassword() {
        return this.proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public int getEurekaServerReadTimeoutSeconds() {
        return this.eurekaServerReadTimeoutSeconds;
    }

    public void setEurekaServerReadTimeoutSeconds(int eurekaServerReadTimeoutSeconds) {
        this.eurekaServerReadTimeoutSeconds = eurekaServerReadTimeoutSeconds;
    }

    @Override
    public int getEurekaServerConnectTimeoutSeconds() {
        return this.eurekaServerConnectTimeoutSeconds;
    }

    public void setEurekaServerConnectTimeoutSeconds(int eurekaServerConnectTimeoutSeconds) {
        this.eurekaServerConnectTimeoutSeconds = eurekaServerConnectTimeoutSeconds;
    }

    @Override
    public String getBackupRegistryImpl() {
        return this.backupRegistryImpl;
    }

    public void setBackupRegistryImpl(String backupRegistryImpl) {
        this.backupRegistryImpl = backupRegistryImpl;
    }

    @Override
    public int getEurekaServerTotalConnections() {
        return this.eurekaServerTotalConnections;
    }

    public void setEurekaServerTotalConnections(int eurekaServerTotalConnections) {
        this.eurekaServerTotalConnections = eurekaServerTotalConnections;
    }

    @Override
    public int getEurekaServerTotalConnectionsPerHost() {
        return this.eurekaServerTotalConnectionsPerHost;
    }

    public void setEurekaServerTotalConnectionsPerHost(int eurekaServerTotalConnectionsPerHost) {
        this.eurekaServerTotalConnectionsPerHost = eurekaServerTotalConnectionsPerHost;
    }

    @Override
    public String getEurekaServerURLContext() {
        return this.eurekaServerURLContext;
    }

    public void setEurekaServerURLContext(String eurekaServerURLContext) {
        this.eurekaServerURLContext = eurekaServerURLContext;
    }

    @Override
    public String getEurekaServerPort() {
        return this.eurekaServerPort;
    }

    public void setEurekaServerPort(String eurekaServerPort) {
        this.eurekaServerPort = eurekaServerPort;
    }

    @Override
    public String getEurekaServerDNSName() {
        return this.eurekaServerDNSName;
    }

    public void setEurekaServerDNSName(String eurekaServerDNSName) {
        this.eurekaServerDNSName = eurekaServerDNSName;
    }

    @Override
    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public int getEurekaConnectionIdleTimeoutSeconds() {
        return this.eurekaConnectionIdleTimeoutSeconds;
    }

    public void setEurekaConnectionIdleTimeoutSeconds(int eurekaConnectionIdleTimeoutSeconds) {
        this.eurekaConnectionIdleTimeoutSeconds = eurekaConnectionIdleTimeoutSeconds;
    }

    @Override
    public String getRegistryRefreshSingleVipAddress() {
        return this.registryRefreshSingleVipAddress;
    }

    public void setRegistryRefreshSingleVipAddress(String registryRefreshSingleVipAddress) {
        this.registryRefreshSingleVipAddress = registryRefreshSingleVipAddress;
    }

    @Override
    public int getHeartbeatExecutorThreadPoolSize() {
        return this.heartbeatExecutorThreadPoolSize;
    }

    public void setHeartbeatExecutorThreadPoolSize(int heartbeatExecutorThreadPoolSize) {
        this.heartbeatExecutorThreadPoolSize = heartbeatExecutorThreadPoolSize;
    }

    @Override
    public int getHeartbeatExecutorExponentialBackOffBound() {
        return this.heartbeatExecutorExponentialBackOffBound;
    }

    public void setHeartbeatExecutorExponentialBackOffBound(int heartbeatExecutorExponentialBackOffBound) {
        this.heartbeatExecutorExponentialBackOffBound = heartbeatExecutorExponentialBackOffBound;
    }

    @Override
    public int getCacheRefreshExecutorThreadPoolSize() {
        return this.cacheRefreshExecutorThreadPoolSize;
    }

    public void setCacheRefreshExecutorThreadPoolSize(int cacheRefreshExecutorThreadPoolSize) {
        this.cacheRefreshExecutorThreadPoolSize = cacheRefreshExecutorThreadPoolSize;
    }

    @Override
    public int getCacheRefreshExecutorExponentialBackOffBound() {
        return this.cacheRefreshExecutorExponentialBackOffBound;
    }

    public void setCacheRefreshExecutorExponentialBackOffBound(int cacheRefreshExecutorExponentialBackOffBound) {
        this.cacheRefreshExecutorExponentialBackOffBound = cacheRefreshExecutorExponentialBackOffBound;
    }

    public Map<String, String> getServiceUrl() {
        return this.serviceUrl;
    }

    public void setServiceUrl(Map<String, String> serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public boolean isgZipContent() {
        return this.gZipContent;
    }

    public void setgZipContent(boolean gZipContent) {
        this.gZipContent = gZipContent;
    }

    public boolean isUseDnsForFetchingServiceUrls() {
        return this.useDnsForFetchingServiceUrls;
    }

    public void setUseDnsForFetchingServiceUrls(boolean useDnsForFetchingServiceUrls) {
        this.useDnsForFetchingServiceUrls = useDnsForFetchingServiceUrls;
    }

    public boolean isRegisterWithEureka() {
        return this.registerWithEureka;
    }

    public void setRegisterWithEureka(boolean registerWithEureka) {
        this.registerWithEureka = registerWithEureka;
    }

    public boolean isPreferSameZoneEureka() {
        return this.preferSameZoneEureka;
    }

    public void setPreferSameZoneEureka(boolean preferSameZoneEureka) {
        this.preferSameZoneEureka = preferSameZoneEureka;
    }

    public boolean isLogDeltaDiff() {
        return this.logDeltaDiff;
    }

    public void setLogDeltaDiff(boolean logDeltaDiff) {
        this.logDeltaDiff = logDeltaDiff;
    }

    public boolean isDisableDelta() {
        return this.disableDelta;
    }

    public void setDisableDelta(boolean disableDelta) {
        this.disableDelta = disableDelta;
    }

    public String getFetchRemoteRegionsRegistry() {
        return this.fetchRemoteRegionsRegistry;
    }

    public void setFetchRemoteRegionsRegistry(String fetchRemoteRegionsRegistry) {
        this.fetchRemoteRegionsRegistry = fetchRemoteRegionsRegistry;
    }

    public Map<String, String> getAvailabilityZones() {
        return this.availabilityZones;
    }

    public void setAvailabilityZones(Map<String, String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    public boolean isFilterOnlyUpInstances() {
        return this.filterOnlyUpInstances;
    }

    public void setFilterOnlyUpInstances(boolean filterOnlyUpInstances) {
        this.filterOnlyUpInstances = filterOnlyUpInstances;
    }

    public boolean isFetchRegistry() {
        return this.fetchRegistry;
    }

    public void setFetchRegistry(boolean fetchRegistry) {
        this.fetchRegistry = fetchRegistry;
    }

    @Override
    public String getDollarReplacement() {
        return this.dollarReplacement;
    }

    public void setDollarReplacement(String dollarReplacement) {
        this.dollarReplacement = dollarReplacement;
    }

    @Override
    public String getEscapeCharReplacement() {
        return this.escapeCharReplacement;
    }

    public void setEscapeCharReplacement(String escapeCharReplacement) {
        this.escapeCharReplacement = escapeCharReplacement;
    }

    public boolean isAllowRedirects() {
        return this.allowRedirects;
    }

    public void setAllowRedirects(boolean allowRedirects) {
        this.allowRedirects = allowRedirects;
    }

    public boolean isOnDemandUpdateStatusChange() {
        return this.onDemandUpdateStatusChange;
    }

    public void setOnDemandUpdateStatusChange(boolean onDemandUpdateStatusChange) {
        this.onDemandUpdateStatusChange = onDemandUpdateStatusChange;
    }

    @Override
    public String getEncoderName() {
        return this.encoderName;
    }

    public void setEncoderName(String encoderName) {
        this.encoderName = encoderName;
    }

    @Override
    public String getDecoderName() {
        return this.decoderName;
    }

    public void setDecoderName(String decoderName) {
        this.decoderName = decoderName;
    }

    @Override
    public String getClientDataAccept() {
        return this.clientDataAccept;
    }

    public void setClientDataAccept(String clientDataAccept) {
        this.clientDataAccept = clientDataAccept;
    }

    public boolean isShouldUnregisterOnShutdown() {
        return this.shouldUnregisterOnShutdown;
    }

    public void setShouldUnregisterOnShutdown(boolean shouldUnregisterOnShutdown) {
        this.shouldUnregisterOnShutdown = shouldUnregisterOnShutdown;
    }

    public boolean isShouldEnforceRegistrationAtInit() {
        return this.shouldEnforceRegistrationAtInit;
    }

    public void setShouldEnforceRegistrationAtInit(boolean shouldEnforceRegistrationAtInit) {
        this.shouldEnforceRegistrationAtInit = shouldEnforceRegistrationAtInit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EurekaClientConfigBean that = (EurekaClientConfigBean) o;
        return (this.enabled == that.enabled &&
                Objects.equals(this.transportConfig, that.transportConfig) &&
                this.registryFetchIntervalSeconds == that.registryFetchIntervalSeconds &&
                this.instanceInfoReplicationIntervalSeconds == that.instanceInfoReplicationIntervalSeconds &&
                this.initialInstanceInfoReplicationIntervalSeconds == that.initialInstanceInfoReplicationIntervalSeconds &&
                this.eurekaServiceUrlPollIntervalSeconds == that.eurekaServiceUrlPollIntervalSeconds &&
                this.eurekaServerReadTimeoutSeconds == that.eurekaServerReadTimeoutSeconds &&
                this.eurekaServerConnectTimeoutSeconds == that.eurekaServerConnectTimeoutSeconds &&
                this.eurekaServerTotalConnections == that.eurekaServerTotalConnections &&
                this.eurekaServerTotalConnectionsPerHost == that.eurekaServerTotalConnectionsPerHost &&
                this.eurekaConnectionIdleTimeoutSeconds == that.eurekaConnectionIdleTimeoutSeconds &&
                this.heartbeatExecutorThreadPoolSize == that.heartbeatExecutorThreadPoolSize &&
                this.heartbeatExecutorExponentialBackOffBound == that.heartbeatExecutorExponentialBackOffBound &&
                this.cacheRefreshExecutorThreadPoolSize == that.cacheRefreshExecutorThreadPoolSize &&
                this.cacheRefreshExecutorExponentialBackOffBound == that.cacheRefreshExecutorExponentialBackOffBound &&
                this.gZipContent == that.gZipContent &&
                this.useDnsForFetchingServiceUrls == that.useDnsForFetchingServiceUrls &&
                this.registerWithEureka == that.registerWithEureka &&
                this.preferSameZoneEureka == that.preferSameZoneEureka &&
                this.logDeltaDiff == that.logDeltaDiff &&
                this.disableDelta == that.disableDelta &&
                this.filterOnlyUpInstances == that.filterOnlyUpInstances &&
                this.fetchRegistry == that.fetchRegistry &&
                this.allowRedirects == that.allowRedirects &&
                this.onDemandUpdateStatusChange == that.onDemandUpdateStatusChange &&
                this.shouldUnregisterOnShutdown == that.shouldUnregisterOnShutdown &&
                this.shouldEnforceRegistrationAtInit == that.shouldEnforceRegistrationAtInit &&
                Objects.equals(this.proxyPort, that.proxyPort) && Objects.equals(this.proxyHost, that.proxyHost) &&
                Objects.equals(this.proxyUserName, that.proxyUserName) &&
                Objects.equals(this.proxyPassword, that.proxyPassword) &&
                Objects.equals(this.backupRegistryImpl, that.backupRegistryImpl) &&
                Objects.equals(this.eurekaServerURLContext, that.eurekaServerURLContext) &&
                Objects.equals(this.eurekaServerPort, that.eurekaServerPort) &&
                Objects.equals(this.eurekaServerDNSName, that.eurekaServerDNSName) && Objects.equals(this.region, that.region) &&
                Objects.equals(this.registryRefreshSingleVipAddress, that.registryRefreshSingleVipAddress) &&
                Objects.equals(this.serviceUrl, that.serviceUrl) &&
                Objects.equals(this.fetchRemoteRegionsRegistry, that.fetchRemoteRegionsRegistry) &&
                Objects.equals(this.availabilityZones, that.availabilityZones) &&
                Objects.equals(this.dollarReplacement, that.dollarReplacement) &&
                Objects.equals(this.escapeCharReplacement, that.escapeCharReplacement) &&
                Objects.equals(this.encoderName, that.encoderName) && Objects.equals(this.decoderName, that.decoderName) &&
                Objects.equals(this.clientDataAccept, that.clientDataAccept));
    }

    @Override
    public int hashCode() {
        return Objects.hash(new Object[]{
                Boolean.valueOf(this.enabled), this.transportConfig, Integer.valueOf(this.registryFetchIntervalSeconds),
                Integer.valueOf(this.instanceInfoReplicationIntervalSeconds), Integer.valueOf(this.initialInstanceInfoReplicationIntervalSeconds),
                Integer.valueOf(this.eurekaServiceUrlPollIntervalSeconds), this.proxyPort, this.proxyHost, this.proxyUserName,
                this.proxyPassword,
                Integer.valueOf(this.eurekaServerReadTimeoutSeconds), Integer.valueOf(this.eurekaServerConnectTimeoutSeconds), this.backupRegistryImpl,
                Integer.valueOf(this.eurekaServerTotalConnections), Integer.valueOf(this.eurekaServerTotalConnectionsPerHost), this.eurekaServerURLContext, this.eurekaServerPort, this.eurekaServerDNSName, this.region,
                Integer.valueOf(this.eurekaConnectionIdleTimeoutSeconds), this.registryRefreshSingleVipAddress,
                Integer.valueOf(this.heartbeatExecutorThreadPoolSize),
                Integer.valueOf(this.heartbeatExecutorExponentialBackOffBound), Integer.valueOf(this.cacheRefreshExecutorThreadPoolSize),
                Integer.valueOf(this.cacheRefreshExecutorExponentialBackOffBound), this.serviceUrl, Boolean.valueOf(this.gZipContent), Boolean.valueOf(this.useDnsForFetchingServiceUrls),
                Boolean.valueOf(this.registerWithEureka),
                Boolean.valueOf(this.preferSameZoneEureka), Boolean.valueOf(this.logDeltaDiff), Boolean.valueOf(this.disableDelta), this.fetchRemoteRegionsRegistry, this.availabilityZones,
                Boolean.valueOf(this.filterOnlyUpInstances), Boolean.valueOf(this.fetchRegistry), this.dollarReplacement, this.escapeCharReplacement,
                Boolean.valueOf(this.allowRedirects),
                Boolean.valueOf(this.onDemandUpdateStatusChange), this.encoderName, this.decoderName, this.clientDataAccept,
                Boolean.valueOf(this.shouldUnregisterOnShutdown), Boolean.valueOf(this.shouldEnforceRegistrationAtInit)});
    }

    @Override
    public String toString() {
        return "EurekaClientConfigBean{" +
                "enabled=" + this.enabled + ", " + "transportConfig=" + this.transportConfig + ", " +
                "registryFetchIntervalSeconds=" + this.registryFetchIntervalSeconds + ", " + "instanceInfoReplicationIntervalSeconds=" +
                this.instanceInfoReplicationIntervalSeconds + ", " +
                "initialInstanceInfoReplicationIntervalSeconds=" + this.initialInstanceInfoReplicationIntervalSeconds +
                ", " + "eurekaServiceUrlPollIntervalSeconds=" +
                this.eurekaServiceUrlPollIntervalSeconds + ", " + "proxyPort='" +
                this.proxyPort + "', " + "proxyHost='" + this.proxyHost + "', " +
                "proxyUserName='" + this.proxyUserName + "', " + "proxyPassword='" + this.proxyPassword +
                "', " + "eurekaServerReadTimeoutSeconds=" + this.eurekaServerReadTimeoutSeconds +
                ", " + "eurekaServerConnectTimeoutSeconds=" + this.eurekaServerConnectTimeoutSeconds +
                ", " + "backupRegistryImpl='" + this.backupRegistryImpl +
                "', " + "eurekaServerTotalConnections=" + this.eurekaServerTotalConnections +
                ", " + "eurekaServerTotalConnectionsPerHost=" + this.eurekaServerTotalConnectionsPerHost +
                ", " + "eurekaServerURLContext='" + this.eurekaServerURLContext +
                "', " + "eurekaServerPort='" + this.eurekaServerPort + "', " +
                "eurekaServerDNSName='" + this.eurekaServerDNSName + "', " + "region='" +
                this.region + "', " + "eurekaConnectionIdleTimeoutSeconds=" + this.eurekaConnectionIdleTimeoutSeconds +
                ", " + "registryRefreshSingleVipAddress='" + this.registryRefreshSingleVipAddress +
                "', " + "heartbeatExecutorThreadPoolSize=" + this.heartbeatExecutorThreadPoolSize +
                ", " + "heartbeatExecutorExponentialBackOffBound=" +
                this.heartbeatExecutorExponentialBackOffBound + ", " +
                "cacheRefreshExecutorThreadPoolSize=" + this.cacheRefreshExecutorThreadPoolSize + ", " +
                "cacheRefreshExecutorExponentialBackOffBound=" + this.cacheRefreshExecutorExponentialBackOffBound +
                ", " + "serviceUrl=" + this.serviceUrl +
                ", " + "gZipContent=" + this.gZipContent + ", " + "useDnsForFetchingServiceUrls=" +
                this.useDnsForFetchingServiceUrls + ", " + "registerWithEureka=" +
                this.registerWithEureka + ", " + "preferSameZoneEureka=" + this.preferSameZoneEureka +
                ", " + "logDeltaDiff=" + this.logDeltaDiff + ", " + "disableDelta=" +
                this.disableDelta + ", " + "fetchRemoteRegionsRegistry='" + this.fetchRemoteRegionsRegistry +
                "', " + "availabilityZones=" + this.availabilityZones + ", " +
                "filterOnlyUpInstances=" + this.filterOnlyUpInstances + ", " + "fetchRegistry=" +
                this.fetchRegistry + ", " + "dollarReplacement='" + this.dollarReplacement +
                "', " + "escapeCharReplacement='" + this.escapeCharReplacement + "', " +
                "allowRedirects=" + this.allowRedirects + ", " + "onDemandUpdateStatusChange=" +
                this.onDemandUpdateStatusChange + ", " + "encoderName='" +
                this.encoderName + "', " + "decoderName='" + this.decoderName + "', " +
                "clientDataAccept='" + this.clientDataAccept + "', " + "shouldUnregisterOnShutdown='" +
                this.shouldUnregisterOnShutdown + "shouldEnforceRegistrationAtInit='" +
                this.shouldEnforceRegistrationAtInit + "'}";
    }
}
