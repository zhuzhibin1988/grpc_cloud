package com.qd.cloud.nameresolver;

import com.qd.cloud.eureka.EurekaDiscoveryClient;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/26 3:01 下午
 * @Description:
 */

@Slf4j
public class EurekaNameResolverProvider extends NameResolverProvider {
    /**
     * The constant containing the scheme that will be used by this factory.
     */
    public static final String DISCOVERY_SCHEME = "eureka";
    private final Set<EurekaNameResolver> discoveryClientNameResolvers = ConcurrentHashMap.newKeySet();
    private final EurekaDiscoveryClient client;
    private ScheduledExecutorService scheduler;

    /**
     * Creates a new discovery client based name resolver factory.
     *
     * @param client The client to use for the address discovery.
     */
    public EurekaNameResolverProvider(final EurekaDiscoveryClient client) {
        this.client = requireNonNull(client, "client not init");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("nameresolver refresh");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleWithFixedDelay(new ServiceRefresh(), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 6;
    }


    @Override
    public String getDefaultScheme() {
        return DISCOVERY_SCHEME;
    }


    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final NameResolver.Args args) {
        if (DISCOVERY_SCHEME.equals(targetUri.getScheme())) {
            final String serviceName = targetUri.getPath();
            if (serviceName == null || serviceName.length() <= 1 || !serviceName.startsWith("/")) {
                throw new IllegalArgumentException("Incorrectly formatted target uri; "
                        + "expected: '" + DISCOVERY_SCHEME + ":[//]/<service-name>'; "
                        + "but was '" + targetUri.toString() + "'");
            }
            final AtomicReference<EurekaNameResolver> reference = new AtomicReference<>();
            final EurekaNameResolver eurekaNameResolver = new EurekaNameResolver(serviceName.substring(1), this.client, args,
                    GrpcUtil.SHARED_CHANNEL_EXECUTOR,
                    () -> this.discoveryClientNameResolvers.remove(reference.get()));
            reference.set(eurekaNameResolver);
            this.discoveryClientNameResolvers.add(eurekaNameResolver);
            return eurekaNameResolver;
        }
        return null;
    }

    /**
     * Cleans up the name resolvers.
     */
    @PreDestroy
    public void destroy() {
        this.scheduler.shutdown();
        this.discoveryClientNameResolvers.clear();
    }

    @Override
    public String toString() {
        return "DiscoveryClientResolverFactory [scheme=" + getDefaultScheme() +
                ", discoveryClient=" + this.client + "]";
    }

    private class ServiceRefresh implements Runnable {

        @Override
        public void run() {
            for (EurekaNameResolver resolver : EurekaNameResolverProvider.this.discoveryClientNameResolvers) {
                resolver.refreshFromExternal();
            }
        }
    }
}
