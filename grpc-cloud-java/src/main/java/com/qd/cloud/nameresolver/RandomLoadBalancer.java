package com.qd.cloud.nameresolver;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.grpc.ConnectivityState.*;

/**
 * @Author: zhuzhibin@qdtech.ai
 * @Date: 2020/11/26 4:06 下午
 * @Description:
 */

@Slf4j
public class RandomLoadBalancer extends LoadBalancer {
    LoadBalancer.Helper helper;

    private final Map<EquivalentAddressGroup, Subchannel> subchannels = new ConcurrentHashMap<>();
    static final Attributes.Key<ConnectivityStateInfo> STATE_INFO = Attributes.Key.create("state-info");

    public RandomLoadBalancer(LoadBalancer.Helper helper) {
        this.helper = helper;
    }

    @Override
    public void handleResolvedAddresses(ResolvedAddresses resolvedAddresses) {
        List<EquivalentAddressGroup> addresses = resolvedAddresses.getAddresses();
        for (EquivalentAddressGroup address : addresses) {
            List<EquivalentAddressGroup> serverSingletonList = Collections.singletonList(address);
            Subchannel exists = subchannels.getOrDefault(address, null);
            if (null != exists) {
                exists.updateAddresses(serverSingletonList);
                continue;
            }
            Attributes.Builder subchannelAttrs = Attributes.newBuilder().set(STATE_INFO, ConnectivityStateInfo.forNonError(IDLE));
            final Subchannel subchannel = helper.createSubchannel(CreateSubchannelArgs.newBuilder()
                    .setAddresses(serverSingletonList)
                    .setAttributes(subchannelAttrs.build())
                    .build());
            subchannels.put(address, subchannel);
            subchannel.start(new SubchannelStateListener() {
                @Override
                public void onSubchannelState(ConnectivityStateInfo state) {
                    if (state.getState() == TRANSIENT_FAILURE) {
                        subchannel.shutdown();
                    }
                    if (state.getState() == SHUTDOWN) {
                        EquivalentAddressGroup address = subchannel.getAddresses();
                        if (subchannels.containsKey(address)) {
                            subchannels.remove(address);
                            EurekaServiceManager.getAddressUpInstance().remove(address);
                            log.info("remove subchannel[{}]", address);
                        }
                    }
                    if (state.getState() == IDLE) {
                        log.debug("reconnection subchannel[{}]", subchannel);
                        subchannel.requestConnection();
                    }
                    updateBalancingState();
                    return;

                }
            });
            subchannel.requestConnection();
        }
        updateBalancingState();
    }

    @Override
    public void handleNameResolutionError(Status error) {
        shutdown();
        helper.updateBalancingState(TRANSIENT_FAILURE, new SubchannelPicker() {
            @Override
            public PickResult pickSubchannel(PickSubchannelArgs args) {
                return PickResult.withError(error);
            }
        });
    }

    /**
     * 只要有subchannel是可用的，则负载均衡转为ready状态，开始获取channel
     */
    private void updateBalancingState() {
        helper.updateBalancingState(READY, new RandomSubchannelPick(subchannels.values()));
    }

    @Override
    public void shutdown() {
        for (Iterator<Map.Entry<EquivalentAddressGroup, Subchannel>> itr = subchannels.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry<EquivalentAddressGroup, Subchannel> e = itr.next();
            e.getValue().shutdown();
            itr.remove();
        }

    }

    class RandomSubchannelPick extends SubchannelPicker {
        Collection<Subchannel> subchannels;
        Random random = new Random(System.currentTimeMillis());

        public RandomSubchannelPick(Collection<Subchannel> subchannels) {
            this.subchannels = subchannels;
        }

        /**
         * 选择可用的channel
         *
         * @param args
         * @return
         */
        @Override
        public PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
            List<Subchannel> subchannels = new ArrayList<>(this.subchannels);
            subchannels = subchannels.stream().filter(subchannel -> {
                ConnectivityState state = subchannel.getAttributes().get(STATE_INFO).getState();
                return state != SHUTDOWN && state != TRANSIENT_FAILURE;
            }).collect(Collectors.toList());
            int channelSize = subchannels.size();
            if (channelSize == 0) {
                return PickResult.withError(Status.UNAVAILABLE);
            }
            int idx = random.nextInt(channelSize);
            return PickResult.withSubchannel(subchannels.get(idx));
        }
    }
}