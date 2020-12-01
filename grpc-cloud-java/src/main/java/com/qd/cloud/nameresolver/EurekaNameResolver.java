package com.qd.cloud.nameresolver;

import com.amazonaws.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.qd.cloud.eureka.EurekaDiscoveryClient;
import io.grpc.*;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/26 2:45 下午
 * @Description:
 */
@Slf4j
public class EurekaNameResolver extends NameResolver {
    private static final List<InstanceInfo> KEEP_PREVIOUS = null;

    private final String discoveryServiceName;
    private final EurekaDiscoveryClient client;
    private final SynchronizationContext syncContext;
    private final Runnable externalCleaner;
    private final SharedResourceHolder.Resource<Executor> executorResource;
    private final boolean usingExecutorResource;

    private Listener2 listener;
    private Executor executor;
    private AtomicBoolean resolving = new AtomicBoolean(false);
//    private List<InstanceInfo> instanceList = Lists.newArrayList();


    /**
     * springcloud port key in metadata is 'gRPC.port'
     */
    private final String GRPC_PORT_CONFIG_KEY = "gRPC.port";

    /**
     * @param discoveryServiceName
     * @param client
     * @param args
     * @param executorResource
     * @param externalCleaner
     */
    public EurekaNameResolver(String discoveryServiceName, EurekaDiscoveryClient client, final Args args,
                              final SharedResourceHolder.Resource<Executor> executorResource, final Runnable externalCleaner) {
        this.discoveryServiceName = discoveryServiceName;
        this.client = client;
        this.syncContext = requireNonNull(args.getSynchronizationContext(), "syncContext");
        this.externalCleaner = externalCleaner;
        this.executor = args.getOffloadExecutor();
        this.usingExecutorResource = this.executor == null;
        this.executorResource = executorResource;
        log.info("Service started and ready to process requests..");
    }


    @Override
    public String getServiceAuthority() {
        return this.discoveryServiceName;
    }

    @Override
    public void start(final Listener2 listener) {
        checkState(this.listener == null, "not already started");
        if (this.usingExecutorResource) {
            this.executor = SharedResourceHolder.get(this.executorResource);
        }
        this.listener = checkNotNull(listener, "not listener");
        resolve();
    }

    @Override
    public void refresh() {
        checkState(this.listener != null, "not started");
        resolve();
    }

    /**
     * Triggers a refresh on the listener from non-grpc threads. This method can safely be called, even if the listener
     * hasn't been started yet.
     *
     * @see #refresh()
     */
    public void refreshFromExternal() {
        this.syncContext.execute(() -> {
            if (this.listener != null) {
                resolve();
            }
        });
    }

    private void resolve() {
        log.debug("Scheduled resolve for {}", this.discoveryServiceName);
        if (this.resolving.get()) {
            return;
        }
        this.resolving.set(true);
        this.executor.execute(new Resolve(this.listener));
    }

    @Override
    public void shutdown() {
        this.listener = null;
        if (this.executor != null && this.usingExecutorResource) {
            this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
        }
//        this.instanceList = Lists.newArrayList();
        if (this.externalCleaner != null) {
            this.externalCleaner.run();
        }
    }

    @Override
    public String toString() {
        return "DiscoveryClientNameResolver [name=" + this.discoveryServiceName + ", discoveryClient=" + this.client + "]";
    }


    private final class Resolve implements Runnable {

        private final Listener2 savedListener;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param listener The listener to send the results to.
         *                 //         * @param instanceList The current server instance list.
         */
        Resolve(final Listener2 listener) {
            this.savedListener = requireNonNull(listener, "listener");
        }

        @Override
        public void run() {
//            final AtomicReference<List<InstanceInfo>> resultContainer = new AtomicReference<>();
            try {
                resolveInternal();
            } catch (final Exception e) {
                this.savedListener.onError(Status.UNAVAILABLE.withCause(e)
                        .withDescription("Failed to update server list for " + EurekaNameResolver.this.discoveryServiceName));
//                resultContainer.set(Lists.newArrayList());
            } finally {
                EurekaNameResolver.this.resolving.set(false);
//                EurekaNameResolver.this.syncContext.execute(() -> {
//                    final List<InstanceInfo> result = resultContainer.get();
//                    if (result != KEEP_PREVIOUS && EurekaNameResolver.this.listener != null) {
//                        EurekaNameResolver.this.instanceList = result;
//                    }
//                });
            }
        }

        /**
         * Do the actual update checks and resolving logic.
         *
         * @return The new service instance list that is used to connect to the gRPC server or null if the old ones
         * should be used.
         */
        private void resolveInternal() {
            final String name = EurekaNameResolver.this.discoveryServiceName;
            final List<InstanceInfo> newInstanceList = EurekaNameResolver.this.client.fetchAppInstances(name);
            log.debug("Got {} candidate servers for {}", newInstanceList.size(), name);
            if (CollectionUtils.isNullOrEmpty(newInstanceList)) {
                log.error("No servers found for {}", name);
                this.savedListener.onError(Status.UNAVAILABLE.withDescription("No servers found for " + name));
                return;
            }
            if (!needsToUpdateConnections(newInstanceList)) {
                log.debug("Nothing has changed... skipping update for {}", name);
                return;
            }
            log.debug("Ready to update server list for {}", name);
            final List<EquivalentAddressGroup> targets = Lists.newArrayList();
            for (final InstanceInfo instance : newInstanceList) {
                final int port = getGRPCPort(instance);
                log.info("Found gRPC server {}:{} for {}", instance.getHostName(), port, name);
                EquivalentAddressGroup address = new EquivalentAddressGroup(new InetSocketAddress(instance.getIPAddr(), port), Attributes.EMPTY);
                targets.add(address);
                EurekaServiceManager.getAddressUpInstance().put(address, instance);
            }
            if (targets.isEmpty()) {
                log.error("None of the servers for {} specified a gRPC port", name);
                this.savedListener.onError(Status.UNAVAILABLE
                        .withDescription("None of the servers for " + name + " specified a gRPC port"));
            } else {
                this.savedListener.onResult(ResolutionResult.newBuilder()
                        .setAddresses(targets)
                        .build());
                log.info("Done updating server list for {}", name);
            }
        }

        /**
         * Extracts the gRPC server port from the given service instance.
         *
         * @param instance The instance to extract the port from.
         * @return The gRPC server port.
         * @throws IllegalArgumentException If the specified port definition couldn't be parsed.
         */
        private int getGRPCPort(final InstanceInfo instance) {
            final Map<String, String> metadata = instance.getMetadata();
            if (metadata == null) {
                return instance.getPort();
            }
            final String portString = metadata.get(GRPC_PORT_CONFIG_KEY);
            if (portString == null) {
                return instance.getPort();
            }
            try {
                return Integer.parseInt(portString);
            } catch (final NumberFormatException e) {
                // TODO: How to handle this case?
                throw new IllegalArgumentException("Failed to parse gRPC port information from: " + instance, e);
            }
        }

        /**
         * Checks whether this instance should update its connections.
         *
         * @param newInstanceList The new instances that should be compared to the stored ones.
         * @return True, if the given instance list contains different entries than the stored ones.
         */
        private boolean needsToUpdateConnections(final List<InstanceInfo> newInstanceList) {
            if (EurekaServiceManager.getAddressUpInstance().size() != newInstanceList.size()) {
                return true;
            }
            for (final InstanceInfo instance : EurekaServiceManager.getAddressUpInstance().values()) {
                final int port = getGRPCPort(instance);
                boolean isSame = false;
                for (final InstanceInfo newInstance : newInstanceList) {
                    final int newPort = getGRPCPort(newInstance);
                    if (newInstance.getHostName().equals(instance.getHostName()) && port == newPort) {
                        isSame = true;
                        break;
                    }
                }
                if (!isSame) {
                    return true;
                }
            }
            return false;
        }

    }
}
